package flink_sql.udf;

import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.table.api.bridge.java.StreamTableEnvironment;
import org.apache.flink.table.functions.AggregateFunction;

import java.time.ZoneId;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

/**
 * @author lzx
 * @date 2023/6/16 15:23
 * @description: TODO TP90测试案例 基于flink 1.14
 */
public class TP90 {
    public static void main(String[] args) throws Exception {
        Configuration conf = new Configuration();
        conf.setInteger("rest.port", 8085);

        // 可以基于现有的 StreamExecutionEnvironment 创建 StreamTableEnvironment 来与 DataStream API 进行相互转换
        StreamExecutionEnvironment env = StreamExecutionEnvironment.createLocalEnvironmentWithWebUI(conf);
        StreamTableEnvironment tEnv = StreamTableEnvironment.create(env);

        // 指定国内时区
        tEnv.getConfig().setLocalTimeZone(ZoneId.of("Asia/Shanghai"));

        // 创建输入表
        String sql = " CREATE TABLE source ( " +
                "  response_time INT, " +
                "  ts AS localtimestamp, " +
                "  WATERMARK FOR ts AS ts," +
                "  proctime as proctime() " +
                " ) WITH ( " +
                "  'connector' = 'datagen', " +
                "  'rows-per-second'='1000', " +
                "  'fields.response_time.min'='1', " +
                "  'fields.response_time.max'='1000' " +
                " ) ";

        tEnv.executeSql(sql);

        // tEnv.executeSql("select * from source").print();

        tEnv.createTemporaryFunction("mytp", CustomTpFunc.class);

        String selectSql = "   select    " +
                "           TUMBLE_START(proctime,INTERVAL '1' MINUTE)  as starttime, " +
                "           mytp(response_time,90) as tp90    " +
                "   from source   " +
                "   group by TUMBLE(proctime,INTERVAL '1' MINUTE) ";

        tEnv.executeSql(selectSql).print();
        // env.execute();

    }

   public static class TpAccu {
        public Integer tp;
        public Map<Integer, Integer> map = new HashMap<>();
    }

   public static class CustomTpFunc extends AggregateFunction<Integer, TpAccu> {

        @Override
        public TpAccu createAccumulator() {
            return new TpAccu();
        }

        @Override
        public Integer getValue(TpAccu tpAccu) {
            if (tpAccu.map.size() == 0) {
                return null;
            } else {
                TreeMap<Integer, Integer> treeMap = new TreeMap<>(tpAccu.map); //排序
                Integer sum = treeMap.values().stream().reduce(0, Integer::sum);
                int tp = tpAccu.tp;
                int responseTime = 0;
                int p = 0; //位置
                int pos = (int) Math.ceil(sum * (tp / 100D));
                for (Map.Entry<Integer, Integer> entry : treeMap.entrySet()) {
                    p += entry.getValue();
                    if (p >= pos) {
                        responseTime = entry.getKey();
                        break;
                    }
                }
                return responseTime;
            }
        }

        public void accumulate(TpAccu acc,Integer iValue,Integer tp){
            acc.tp = tp;
            if (acc.map.containsKey(iValue)) {
                acc.map.put(iValue,acc.map.get(iValue) + 1);
            }else {
                acc.map.put(iValue,1);
            }
        }
    }
}
