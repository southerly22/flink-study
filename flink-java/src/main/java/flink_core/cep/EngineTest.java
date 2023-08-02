package flink_core.cep;

import org.apache.flink.api.common.RuntimeExecutionMode;
import org.apache.flink.api.common.typeinfo.TypeHint;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.cep.pattern.Pattern;
import org.apache.flink.cep.pattern.conditions.SimpleCondition;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.table.api.DataTypes;
import org.apache.flink.table.api.Schema;
import org.apache.flink.table.api.Table;
import org.apache.flink.table.api.bridge.java.StreamTableEnvironment;
import org.apache.flink.types.Row;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * @author lzx
 * @date 2023/7/10 17:20
 * @description: TODO
 */
public class EngineTest {
    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setRuntimeMode(RuntimeExecutionMode.BATCH);
        StreamTableEnvironment tEnv = StreamTableEnvironment.create(env);

        HashMap<String, String> hashMap = new HashMap<>();
        hashMap.put("hr_scan",null);
        hashMap.put("ms_scan","ms");
        hashMap.put("avp_scan","avp");
        hashMap.put("eset_scan","eset");

        // SingleOutputStreamOperator<Tuple2<String, String>> source = env.fromCollection(hashMap.entrySet()).map(entry -> Tuple2.of(entry.getKey(), entry.getValue()));
        DataStreamSource<String> source1 = env.fromCollection(Arrays.asList("xx", "ms", "avp", "eset"));

        Schema schema = Schema.newBuilder()
                .column("hr_scan", DataTypes.STRING())
                .column("ms_scan", DataTypes.STRING())
                .column("avp_scan", DataTypes.STRING())
                .column("eset_scan", DataTypes.STRING())
                .build();
        Table table = tEnv.fromDataStream(source1, schema);
        DataStream<Row> rowDataStream = tEnv.toChangelogStream(table, schema);
        rowDataStream.print();

        // Pattern.<Tuple2<String,String>>begin("start")
        //                 .where(new SimpleCondition<Tuple2<String, String>>() {
        //                     @Override
        //                     public boolean filter(Tuple2<String, String> value) throws Exception {
        //                         return value.f1 == null || value.f1.isEmpty();
        //                     }
        //                 }).timesOrMore(3)
        //                 .greedy()
        //                         .where(new SimpleCondition<Tuple2<String, String>>() {
        //                             @Override
        //                             public boolean filter(Tuple2<String, String> value) throws Exception {
        //                                 return false;
        //                             }
        //                         })

        // source.print();
        env.execute();
    }
}
