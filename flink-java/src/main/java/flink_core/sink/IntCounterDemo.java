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
 * Flink的累加器 使用，1. 程序执行完毕，输出累加器的值
 *
 * @author lzx
 * @date 2023/05/27 18:01
 **/
public class IntCounterDemo {
    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.createLocalEnvironmentWithWebUI(new Configuration());

        DataStreamSource<String> source = env.fromElements("a a a", "b b b");
        //DataStreamSource<String> source = env.socketTextStream("localhost", 9999);

        source.flatMap(new RichFlatMapFunction<String, Tuple2<String,Integer>>() {

            IntCounter intCounter;

            @Override
            public void open(Configuration parameters) throws Exception {
                intCounter = new IntCounter(); //声明累加器
                getRuntimeContext().addAccumulator("myAcc",intCounter);//注册累加器
            }

            @Override
            public void flatMap(String value, Collector<Tuple2<String, Integer>> out) throws Exception {
                String[] values = value.split(" ");
                for (String s : values){
                    intCounter.add(1); // 累加器赋值
                    out.collect(Tuple2.of(s,1));
                }
            }
        }).print();

        JobExecutionResult executionResult = env.execute("IntCounter"); //execute 是阻塞的，在流式运行中，该语句下面代码不会执行的
        Integer myAcc = (Integer) executionResult.getAccumulatorResult("myAcc");
        System.out.println("myAcc = " + myAcc);
    }
}
