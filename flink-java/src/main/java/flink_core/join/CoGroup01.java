package flink_core.join;

import com.google.common.collect.Maps;
import org.apache.flink.api.common.RuntimeExecutionMode;
import org.apache.flink.api.common.functions.CoGroupFunction;
import org.apache.flink.api.java.functions.KeySelector;
import org.apache.flink.api.java.tuple.Tuple3;
import org.apache.flink.streaming.api.CheckpointingMode;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.datastream.KeyedStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.co.ProcessJoinFunction;
import org.apache.flink.streaming.api.windowing.assigners.TumblingProcessingTimeWindows;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.util.Collector;


import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * @author lzx
 * @date 2023/7/5 11:38
 * @description: TODO
 */
public class CoGroup01 {
    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(1);
        env.enableCheckpointing(1000, CheckpointingMode.EXACTLY_ONCE);
        env.getCheckpointConfig().setCheckpointStorage("file:///D:\\WorkPlace\\flink-study\\flink-java\\ck");
        env.setRuntimeMode(RuntimeExecutionMode.BATCH);

        Map<Integer, String> hashMap1 = new HashMap<>();
        hashMap1.put(1, "a");
        hashMap1.put(2, "b");
        hashMap1.put(3, "c");
        hashMap1.put(5, "u");

        Map<Integer, String> hashMap2 = new HashMap<>();
        hashMap2.put(1, "d");
        hashMap2.put(2, "e");
        hashMap2.put(3, "f");
        hashMap2.put(4, "g");

        DataStreamSource<Map.Entry<Integer, String>> s1 = env.fromCollection(hashMap1.entrySet());
        DataStreamSource<Map.Entry<Integer, String>> s2 = env.fromCollection(hashMap2.entrySet());

        KeyedStream<Map.Entry<Integer, String>, Integer> keyS1 = s1.keyBy(new KeySelector<Map.Entry<Integer, String>, Integer>() {
            @Override
            public Integer getKey(Map.Entry<Integer, String> value) throws Exception {
                return value.getKey();
            }
        });
        KeyedStream<Map.Entry<Integer, String>, Integer> keyS2 = s2.keyBy(new KeySelector<Map.Entry<Integer, String>, Integer>() {
            @Override
            public Integer getKey(Map.Entry<Integer, String> value) throws Exception {
                return value.getKey();
            }
        });

        DataStream<Tuple3<Integer, String, String>> cogroupDS = keyS1.coGroup(keyS2)
                .where(t -> t.getKey())
                .equalTo(t -> t.getKey())
                .window(TumblingProcessingTimeWindows.of(Time.seconds(20)))
                .apply(new CoGroupFunction<Map.Entry<Integer, String>, Map.Entry<Integer, String>, Tuple3<Integer, String, String>>() {
                    @Override
                    public void coGroup(Iterable<Map.Entry<Integer, String>> first, Iterable<Map.Entry<Integer, String>> second, Collector<Tuple3<Integer, String, String>> out) throws Exception {
                        boolean flag = false;
                        for (Map.Entry<Integer, String> entry : first) {
                            for (Map.Entry<Integer, String> entry2 : second) {
                                flag = true;
                                out.collect(new Tuple3<>(entry.getKey(), entry.getValue(), entry2.getValue()));
                            }
                            if (!flag) {
                                out.collect(new Tuple3<>(entry.getKey(), entry.getValue(), null));
                            }
                        }
                    }
                });

        cogroupDS.print();

        env.execute();
    }
}
