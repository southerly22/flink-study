import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.ververica.cdc.connectors.mysql.source.MySqlSource;
import com.ververica.cdc.connectors.mysql.table.StartupOptions;
import com.ververica.cdc.debezium.JsonDebeziumDeserializationSchema;
import entity.CdcDemo;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.CheckpointingMode;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.windowing.RichWindowFunction;
import org.apache.flink.streaming.api.functions.windowing.WindowFunction;
import org.apache.flink.streaming.api.windowing.assigners.TumblingProcessingTimeWindows;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.util.Collector;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Random;

/**
 * @author lzx
 * @date 2023/7/14 13:19
 * @description: TODO
 */
public class Mysql_Cdc_Vertica {
    public static void main(String[] args) throws Exception {
        Configuration conf = new Configuration();
        conf.setInteger("rest.port",8085);
        StreamExecutionEnvironment env = StreamExecutionEnvironment.createLocalEnvironmentWithWebUI(conf);
        env.setParallelism(3);

        env.enableCheckpointing(3000, CheckpointingMode.EXACTLY_ONCE);
        env.getCheckpointConfig().setCheckpointStorage("file:///D:\\CodePlace_JH\\flink-study\\flink-cdc\\src\\ck");
//        env.getCheckpointConfig().setTolerableCheckpointFailureNumber(3);//容忍 ck 失败最大次数
//        env.setRestartStrategy(RestartStrategies.fixedDelayRestart(3,3000));

        Properties prop = new Properties();
        prop.setProperty("useSSL","false");

        // 创建 flink-mysql-cdc
        MySqlSource<String> mysqlSource = MySqlSource.<String>builder()
                .hostname("localhost")
                .port(3306)
//                 .serverTimeZone("UTC")
                 .scanNewlyAddedTableEnabled(true) // 启用扫描新添加的表功能
                .username("root")
                .password("123456")
                .jdbcProperties(prop)
                .databaseList("lzxtest")
                .tableList("lzxtest.test_cdc") //使用"db.table"的方式
                .startupOptions(StartupOptions.initial())
//                .startupOptions(StartupOptions.earliest())
//                .startupOptions(StartupOptions.latest())
                .deserializer(new JsonDebeziumDeserializationSchema())
                .build();

        SingleOutputStreamOperator<JSONObject> mysqlDs = env.fromSource(mysqlSource, WatermarkStrategy.noWatermarks(), "mysql-Cdc")
                .map(JSON::parseObject);
        mysqlDs.print();

//        mysqlDs.map(
//                jsonObj -> {
//                    String op = jsonObj.getString("op");
//                    if ("c".equals(op)) { //插入
//                        jsonObj.get("after");
//                    } else if ("u".equals(op)) { //更新
////                jsonObj.getString("before")+","+jsonObj.getString("after");
//                    } else if ("d".equals(op)) { //删除
//                        jsonObj.getString("before");
//                    }
//                    return "";
//                });
//        SingleOutputStreamOperator<CdcDemo> cdcDemoDs = mysqlDs.filter(jsonObj -> {
//                    return "c".equals(jsonObj.getString("op"));
//                })
//                .map(jsonObj -> {
//                    String after = jsonObj.getString("after");
//                    return JSON.parseObject(after, CdcDemo.class);
//                });
//
//        SingleOutputStreamOperator<List<CdcDemo>> cdcListDs = cdcDemoDs.keyBy(s -> {
//                    return new Random().nextInt(3);
//                }).window(TumblingProcessingTimeWindows.of(Time.seconds(5)))
//                .apply(new RichWindowFunction<CdcDemo, List<CdcDemo>, Integer, TimeWindow>() {
//                    @Override
//                    public void apply(Integer integer, TimeWindow window, Iterable<CdcDemo> input, Collector<List<CdcDemo>> out) throws Exception {
//                        ArrayList<CdcDemo> arrayList = new ArrayList<>();
//                        for (CdcDemo cdcDemo : input) {
//                            arrayList.add(cdcDemo);
//                        }
//                        arrayList.stream().forEach(c-> System.out.println(c.toString()));
//                        out.collect(arrayList);
//                    }
//                });
//
//        cdcListDs.addSink(new VerticaSink("lzxtest","test_cdc_output"));

        env.execute();
    }
}
