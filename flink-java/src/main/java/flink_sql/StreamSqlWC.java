package flink_sql;

import jdk.nashorn.internal.runtime.regexp.joni.constants.StringType;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.table.api.DataTypes;
import org.apache.flink.table.api.Schema;
import org.apache.flink.table.api.Table;
import org.apache.flink.table.api.TableResult;
import org.apache.flink.table.api.bridge.java.StreamTableEnvironment;
import org.apache.flink.table.types.DataType;

import java.time.ZoneId;

import static org.apache.flink.table.api.Expressions.$;


/**
 * @author lzx
 * @date 2023/05/24 11:03
 **/
public class StreamSqlWC {
    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        // 使用StreamTableEnvironment 实现 dataStream 和 sql的互换
        StreamTableEnvironment tenv = StreamTableEnvironment.create(env);
        // 指定国内时区
        tenv.getConfig().setLocalTimeZone(ZoneId.of("Asia/Shanghai"));

        DataStreamSource<String> lines = env.socketTextStream("localhost", 9999);

        SingleOutputStreamOperator<Tuple2<String, Integer>> mapDS = lines.map(new MapFunction<String, Tuple2<String, Integer>>() {
            @Override
            public Tuple2<String, Integer> map(String value) throws Exception {
                String[] split = value.split(",");
                return Tuple2.of(split[0], Integer.valueOf(split[1]));
            }
        });

        // 第一种方式
        tenv.createTemporaryView("v_wc",mapDS);
        tenv.executeSql("desc v_wc").print();
        /**
         * +------+--------+-------+-----+--------+-----------+
         * | name |   type |  null | key | extras | watermark |
         * +------+--------+-------+-----+--------+-----------+
         * |   f0 | STRING |  true |     |        |           |
         * |   f1 |    INT | false |     |        |           |
         * +------+--------+-------+-----+--------+-----------+
         */
        tenv.executeSql("select f0,sum(f1) from v_wc group by f0").print();

        // 2. 带Schema的方式
        Schema.newBuilder()
                .columnByExpression("word",$("f0"))
                .columnByExpression("cnt",$("f1"))
                .columnByMetadata("tt",DataTypes.TIMESTAMP_LTZ(3),"proctime")
                .build();


        // 2,过时api
        //tenv.createTemporaryView("v_wc1",mapDS,"word,cnt");
        //tenv.executeSql("select word,sum(cnt) from v_wc1 group by word").print();

        // 3, 过时api
        //tenv.createTemporaryView("v_wc2",mapDS,$("word"),$("cnt"));
        //tenv.executeSql("select word,sum(cnt) from v_wc2 group by word").print();

        env.execute();
    }
}
