package flink_core.join;

import org.apache.flink.api.common.eventtime.SerializableTimestampAssigner;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.functions.RichReduceFunction;
import org.apache.flink.api.java.tuple.Tuple;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.windowing.ProcessWindowFunction;
import org.apache.flink.streaming.api.functions.windowing.WindowFunction;
import org.apache.flink.streaming.api.windowing.assigners.TumblingEventTimeWindows;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.util.Collector;

import java.time.Duration;

/**
 * 迟到数据
 *
 * @author lzx
 * @date 2023/09/01 20:22
 **/
public class Lateness {
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
                Tuple2.of("b", 2000L),
                Tuple2.of("a", 3000L),
                Tuple2.of("a", 3002L)
        ).<Tuple2<String, Integer>>assignTimestampsAndWatermarks(d1Strategy);

        SingleOutputStreamOperator<Tuple2<String, Long>> resDs = ds1.keyBy(1)
                .window(TumblingEventTimeWindows.of(Time.seconds(2)))
                .allowedLateness(Time.seconds(1))
                        //.process(new ProcessWindowFunction<Tuple2<String, Long>, Tuple2<String, Long>, Tuple, TimeWindow>() {
                        //    @Override
                        //    public void process(Tuple tuple, ProcessWindowFunction<Tuple2<String, Long>, Tuple2<String, Long>, Tuple, TimeWindow>.Context context, Iterable<Tuple2<String, Long>> elements, Collector<Tuple2<String, Long>> out) throws Exception {
                        //            context.window().getEnd()
                        //    }
                        //})
                .reduce(new RichReduceFunction<Tuple2<String, Long>>() {
                    @Override
                    public void open(Configuration parameters) throws Exception {
                        getRuntimeContext().getTaskNameWithSubtasks();
                    }

                    @Override
                    public Tuple2<String, Long> reduce(Tuple2<String, Long> value1, Tuple2<String, Long> value2) throws Exception {
                        return Tuple2.of(value1.f0, value1.f1 + value2.f1);
                    }
                });

        resDs.print();

        env.execute();
    }
}
