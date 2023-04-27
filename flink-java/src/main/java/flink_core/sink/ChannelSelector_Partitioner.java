package flink_core.sink;

import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.KeyedProcessFunction;
import org.apache.flink.util.Collector;

/**
 * @author lzx
 * @date 2023/04/23 11:50
 **/
public class ChannelSelector_Partitioner {
    public static void main(String[] args) throws Exception {
        Configuration configuration = new Configuration();
        configuration.setInteger("rest.port", 8085);
        StreamExecutionEnvironment env = StreamExecutionEnvironment.createLocalEnvironmentWithWebUI(configuration);

        DataStreamSource<String> stream = env.socketTextStream("localhost", 9999);

        DataStream<String> s2 = stream
                .map(str -> str.toUpperCase())
                .setParallelism(4)
                .flatMap(new FlatMapFunction<String, String>() {
                    @Override
                    public void flatMap(String s, Collector<String> collector) throws Exception {
                        String[] arr = s.split(",");
                        for (String s1 : arr) {
                            collector.collect(s1);
                        }
                    }
                })
                .setParallelism(4)
                .shuffle()  // map -> flatMap  - shuffle -> map
                ;

        SingleOutputStreamOperator<String> s3 = s2.map(s -> s.toUpperCase())
                .setParallelism(8);

        SingleOutputStreamOperator<String> s4 = s3.keyBy(s -> s.substring(0, 2))
                .process(new KeyedProcessFunction<String, String, String>() {
                    @Override
                    public void processElement(String value, KeyedProcessFunction<String, String, String>.Context ctx, Collector<String> out) throws Exception {
                        out.collect(value + ">");
                    }
                    @Override
                    public void onTimer(long timestamp, KeyedProcessFunction<String, String, String>.OnTimerContext ctx, Collector<String> out) throws Exception {
                        super.onTimer(timestamp, ctx, out);
                    }
                }).setParallelism(4);

        SingleOutputStreamOperator<String> s5 = s4.filter(s -> s.startsWith("b")).setParallelism(4);

        s5.print().setParallelism(4); // keyBy -> filter -> sink(print)

        env.execute("ChannelSelector_Partitioner");
    }
}
