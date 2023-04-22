package flink_core.sink;

import flink_core.source.EventLog;
import org.apache.flink.api.common.functions.RuntimeContext;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.common.typeinfo.Types;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.CheckpointingMode;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.datastream.KeyedStream;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.KeyedProcessFunction;
import org.apache.flink.streaming.api.functions.ProcessFunction;
import org.apache.flink.util.Collector;
import org.apache.flink.util.OutputTag;

/**
 * @author lzx
 * @date 2023/4/21 18:43
 * @description: TODO
 * 在不同的数据流上调用process算子时，所需要传入的processFunction也是不同的
 */
public class ProcessFunctions_Demo {
    public static void main(String[] args) throws Exception {
        Configuration configuration = new Configuration();
        configuration.setInteger("rest.port", 8085);
        StreamExecutionEnvironment env = StreamExecutionEnvironment.createLocalEnvironmentWithWebUI(configuration);

        //开启ck
        env.enableCheckpointing(5000, CheckpointingMode.EXACTLY_ONCE);
        env.getCheckpointConfig().setCheckpointStorage("file:///Users\\liuzhixin\\codeplace\\flink-study\\flink-java\\ck");

        DataStreamSource<EventLog> streamSource = env.addSource(new LzxSourceFunction());
        /**
         * 在普通流上调用process 算子，传入的是 ProcessFunction
         */

        SingleOutputStreamOperator<Tuple2<String, Object>> s1 = streamSource.process(new ProcessFunction<EventLog, Tuple2<String, Object>>() {

            // 可以使用生命周期 open 方法
            @Override
            public void open(Configuration parameters) throws Exception {

                // 可以调用 getRuntimeContext 拿到各种运行的上下文信息
                RuntimeContext runtimeContext = getRuntimeContext();
                System.out.println(runtimeContext.getTaskName());
                System.out.println(runtimeContext.getJobId());

                super.open(parameters);
            }

            @Override
            public void processElement(EventLog eventLog, ProcessFunction<EventLog, Tuple2<String, Object>>.Context context, Collector<Tuple2<String, Object>> collector) throws Exception {
                // 可以做侧输出流
                context.output(new OutputTag<>("s1", Types.STRING)
                        , eventLog.getEventId());

                // 可以做主流输出
                collector.collect(Tuple2.of(eventLog.getEventId(), eventLog.getTimeStamp()));
            }

            @Override
            public void close() throws Exception {
                System.out.println("流 即将关闭！！！");
                super.close();
            }
        });

        /**
         *
         * @author lzx
         * @return
         */

        KeyedStream<Tuple2<String, Object>, String> keyByDS = s1.keyBy(tp2 -> tp2.f0);

        // 然后再keyby上调用process 算子
        keyByDS.process(new KeyedProcessFunction<String, Tuple2<String, Object>, Object>() {
            @Override
            public void processElement(Tuple2<String, Object> value, KeyedProcessFunction<String, Tuple2<String, Object>, Object>.Context ctx, Collector<Object> out) throws Exception {
                out.collect(value.f0.toUpperCase()+","+value.f1);
            }
        }).print();

        env.execute();
    }
}
