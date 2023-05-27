package flink_core.sink;

import org.apache.flink.api.common.JobExecutionResult;
import org.apache.flink.api.common.accumulators.IntCounter;
import org.apache.flink.api.common.functions.RichFlatMapFunction;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.util.Collector;

/**
 * Flink的累加器 使用，2. 程序执行过程中，输出累加器的值:只能把并行度设置为1 ，不然累加值是各个分区的，而不是全局累加的结果
 *
 * @author lzx
 * @date 2023/05/27 18:01
 **/
public class IntCounterDemo2 {
    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.createLocalEnvironmentWithWebUI(new Configuration());

        //DataStreamSource<String> source = env.fromElements("a a a", "b b b");
        env.setParallelism(1);
        DataStreamSource<String> source = env.socketTextStream("localhost", 9999);

        source.flatMap(new RichFlatMapFunction<String, Tuple2<String,IntCounter>>() {
            IntCounter intCounter;

            @Override
            public void open(Configuration parameters) throws Exception {
                intCounter = new IntCounter();
                getRuntimeContext().addAccumulator("myAcc",intCounter);
            }

            @Override
            public void flatMap(String value, Collector<Tuple2<String, IntCounter>> out) throws Exception {
                String[] values = value.split(" ");
                for (String s : values) {
                    intCounter.add(1);
                    out.collect(Tuple2.of(s,intCounter));
                }
            }
        }).print();

        env.execute("IntCounter"); //execute 是阻塞的，在流式运行中，该语句下面代码不会执行的
    }
}
