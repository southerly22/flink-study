package flink_core.join;

import org.apache.flink.api.common.eventtime.SerializableTimestampAssigner;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.functions.AggregateFunction;
import org.apache.flink.api.common.functions.JoinFunction;
import org.apache.flink.api.java.functions.KeySelector;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.api.java.tuple.Tuple3;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.windowing.assigners.TumblingEventTimeWindows;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.jcodings.util.Hash;

import java.time.Duration;
import java.util.HashMap;

/**
 * 窗口join
 *
 * @author lzx
 * @date 2023/09/01 18:21
 **/
public class WinJoin {
    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        env.setParallelism(1);

        // 水位线
        WatermarkStrategy<Tuple2<String, Long>> d1Strategy = WatermarkStrategy.<Tuple2<String, Long>>forBoundedOutOfOrderness(Duration.ZERO)
                .withTimestampAssigner(new SerializableTimestampAssigner<Tuple2<String, Long>>() {
                    @Override
                    public long extractTimestamp(Tuple2<String, Long> element, long recordTimestamp) {
                        return element.f1;
                    }
                });

        SingleOutputStreamOperator<Tuple2<String, Long>> ds1 = env.fromElements(
                Tuple2.of("a", 1000L),
                Tuple2.of("b", 1000L),
                Tuple2.of("a", 2000L),
                Tuple2.of("b", 2000L)
        ).<Tuple2<String, Integer>>assignTimestampsAndWatermarks(d1Strategy);


        SingleOutputStreamOperator<Tuple2<String, Long>> ds2 = env.fromElements(
                Tuple2.of("a", 3000L),
                Tuple2.of("b", 4000L),
                Tuple2.of("a", 4500L),
                Tuple2.of("b", 5500L)
        ).assignTimestampsAndWatermarks(d1Strategy);

        DataStream<Tuple3<String, Long, Long>> resDs = ds1.join(ds2).where(new KeySelector<Tuple2<String, Long>, String>() {
                    @Override
                    public String getKey(Tuple2<String, Long> value) throws Exception {
                        return value.f0;
                    }
                }).equalTo(data -> data.f0).window(TumblingEventTimeWindows.of(Time.seconds(5)))
                .allowedLateness(Time.seconds(3))
                .apply(new JoinFunction<Tuple2<String, Long>, Tuple2<String, Long>, Tuple3<String, Long, Long>>() {
                    @Override
                    public Tuple3<String, Long, Long> join(Tuple2<String, Long> first, Tuple2<String, Long> second) throws Exception {
                        return Tuple3.of(first.f0, first.f1, second.f1);
                    }
                });



        //resDs.print();
        //System.out.println("-------------------");
        SingleOutputStreamOperator<HashMap<String, String>> aggregateDS = resDs.keyBy(1)
                .countWindow(3).aggregate(new AggregateFunction<Tuple3<String, Long, Long>, HashMap<String, String>, HashMap<String, String>>() {
                    @Override
                    public HashMap<String, String> createAccumulator() {
                        return new HashMap<String,String>();
                    }

                    @Override
                    public HashMap<String, String> add(Tuple3<String, Long, Long> value, HashMap<String, String> accumulator) {
                        accumulator.put(value.f0, value.f1 + "," + value.f2);
                        return accumulator;
                    }

                    @Override
                    public HashMap<String, String> getResult(HashMap<String, String> accumulator) {
                        return accumulator;
                    }

                    @Override
                    public HashMap<String, String> merge(HashMap<String, String> a, HashMap<String, String> b) {
                        a.putAll(b);
                        return a;
                    }
                });

        aggregateDS.print();
        env.execute();
    }
}
