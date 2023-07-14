package flink_core.join;

import org.apache.flink.api.common.RuntimeExecutionMode;
import org.apache.flink.api.common.functions.CoGroupFunction;
import org.apache.flink.api.common.functions.JoinFunction;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.api.CheckpointingMode;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.windowing.assigners.TumblingEventTimeWindows;
import org.apache.flink.streaming.api.windowing.assigners.TumblingProcessingTimeWindows;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.util.Collector;

import java.util.ArrayList;
import java.util.List;

/**
 * @author lzx
 * @date 2023/7/5 11:38
 * @description: TODO
 */
public class CoGroup02 {
    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(1);
        env.enableCheckpointing(1000, CheckpointingMode.EXACTLY_ONCE);
        env.getCheckpointConfig().setCheckpointStorage("file:///D:\\WorkPlace\\flink-study\\flink-java\\ck");
        env.setRuntimeMode(RuntimeExecutionMode.BATCH);

        // 创建第一个输入流，假设为事件流1
        List<Tuple2<String, Integer>> events1 = new ArrayList<>();
        events1.add(new Tuple2<>("key1", 10));
        events1.add(new Tuple2<>("key2", 20));
        events1.add(new Tuple2<>("key3", 30));

        // 创建第二个输入流，假设为事件流2
        List<Tuple2<String, String>> events2 = new ArrayList<>();
        events2.add(new Tuple2<>("key1", "value1"));
        events2.add(new Tuple2<>("key3", "value3"));
        events2.add(new Tuple2<>("key4", "value4"));

        // 将事件流1和事件流2转化为DataStream
        DataStream<Tuple2<String, Integer>> eventStream1 = env.fromCollection(events1);
        DataStream<Tuple2<String, String>> eventStream2 = env.fromCollection(events2);

        // 使用CoGroupFunction进行关联操作
        DataStream<Tuple2<String, String>> coGroupResult = eventStream1
                .coGroup(eventStream2)
                .where(t -> t.f0)
                .equalTo(t -> t.f0)
                .window(TumblingProcessingTimeWindows.of(Time.seconds(5)))
                .apply(new CoGroupFunction<Tuple2<String, Integer>, Tuple2<String, String>, Tuple2<String, String>>() {
                    @Override
                    public void coGroup(Iterable<Tuple2<String, Integer>> first, Iterable<Tuple2<String, String>> second, Collector<Tuple2<String, String>> out) {
                        for (Tuple2<String, Integer> e1 : first) {
                            boolean matched = false;
                            for (Tuple2<String, String> e2 : second) {
                                if (e1.f0.equals(e2.f0)) {
                                    out.collect(new Tuple2<>(e1.f0, e2.f1));
                                    matched = true;
                                }
                            }
                            if (!matched) {
                                out.collect(new Tuple2<>(e1.f0, "Not Matched"));
                            }
                        }
                    }
                });

        // 使用JoinFunction进行关联操作
        DataStream<Tuple2<String, String>> joinResult = eventStream1
                .join(eventStream2)
                .where(t -> t.f0)
                .equalTo(t -> t.f0)
                .window(TumblingProcessingTimeWindows.of(Time.seconds(5)))
                .apply(new JoinFunction<Tuple2<String, Integer>, Tuple2<String, String>, Tuple2<String, String>>() {
                    @Override
                    public Tuple2<String, String> join(Tuple2<String, Integer> first, Tuple2<String, String> second) {
                        if (second != null) {
                            return new Tuple2<>(first.f0, second.f1);
                        } else {
                            return new Tuple2<>(first.f0, "Not Matched");
                        }
                    }
                });
        // 打印结果
        coGroupResult.print("coGroup>>>");
        joinResult.print();

        env.execute();
    }
}
