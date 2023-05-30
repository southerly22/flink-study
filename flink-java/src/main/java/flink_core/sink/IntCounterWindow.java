package flink_core.sink;

import org.apache.flink.api.common.accumulators.IntCounter;
import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.api.common.functions.ReduceFunction;
import org.apache.flink.api.common.functions.RichFlatMapFunction;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.datastream.DataStreamUtils;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.windowing.assigners.TumblingEventTimeWindows;
import org.apache.flink.streaming.api.windowing.assigners.TumblingProcessingTimeWindows;
import org.apache.flink.streaming.api.windowing.assigners.WindowAssigner;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.util.CloseableIterator;
import org.apache.flink.util.Collector;

/**
 * Flink的累加器 使用 结合窗口函数。输出累加器的值
 *
 * @author lzx
 * @date 2023/05/27 18:01
 **/
public class IntCounterWindow {
    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.createLocalEnvironmentWithWebUI(new Configuration());

        DataStreamSource<String> source = env.socketTextStream("localhost", 9999);

        SingleOutputStreamOperator<Integer> reduce = source.flatMap(new FlatMapFunction<String, Integer>() {
                    @Override
                    public void flatMap(String value, Collector<Integer> out) throws Exception {
                        String[] values = value.split(" ");
                        for (String s : values) {
                            out.collect(1);
                        }
                    }
                }).windowAll(TumblingProcessingTimeWindows.of(Time.seconds(3)))
                .reduce(new ReduceFunction<Integer>() {
                    @Override
                    public Integer reduce(Integer value1, Integer value2) throws Exception {
                        return value1 + value2;
                    }
                });

        // 收集流中的数据
        CloseableIterator<Integer> reduceIterator = reduce.executeAndCollect();
        while (reduceIterator.hasNext()) {
            Integer next = reduceIterator.next();
            // 插入数据库
            System.out.println("next = " + next);
        }

        env.execute("IntCounter");
    }
}
