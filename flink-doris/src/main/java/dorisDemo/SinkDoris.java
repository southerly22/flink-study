package dorisDemo;

import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.common.functions.RichMapFunction;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

import java.util.ArrayList;

/**
 * @author lzx
 * @date 2023/6/21 18:09
 * @description: TODO
 */
public class SinkDoris {
    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        ArrayList<Tuple2<String, Integer>> data = new ArrayList<>();
        data.add(new Tuple2<>("doris", 1));

        env.fromCollection(data).map(new MapFunction<Tuple2<String, Integer>, String>() {
            @Override
            public String map(Tuple2<String, Integer> value) throws Exception {
                return value.f0 + "\t" + value.f1;
            }
        }).print();

        env.execute();
    }
}
