package flink_core.state;

import flink_core.source.EventLog;
import flink_core.source.MyRichSourceFunction;
import org.apache.flink.api.common.state.ValueState;
import org.apache.flink.api.common.state.ValueStateDescriptor;
import org.apache.flink.api.common.typeinfo.Types;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.runtime.state.hashmap.HashMapStateBackend;
import org.apache.flink.streaming.api.CheckpointingMode;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.environment.CheckpointConfig;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.KeyedProcessFunction;
import org.apache.flink.streaming.api.functions.source.datagen.DataGeneratorSource;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumerBase;
import org.apache.flink.util.Collector;

/**
 * @author lzx
 * @date 2023/09/14 22:19
 **/
public class FlinkStreamingDemo {
    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(1);
        // 设置任务的最大并行度 也就是keyGroup的个数
        //env.setMaxParallelism(128);
        //env.getConfig().setAutoWatermarkInterval(1000L);
        // 设置开启checkpoint
        env.enableCheckpointing(10000L);
        env.getCheckpointConfig().setCheckpointingMode(CheckpointingMode.EXACTLY_ONCE);

        env.setStateBackend(new HashMapStateBackend());
        env.getCheckpointConfig().setCheckpointStorage("file:////Users/liuzhixin/codeplace/flink-study/flink-java/ck");

        // 确保检查点之间有至少500 ms的间隔【checkpoint最小间隔】
        env.getCheckpointConfig().setMinPauseBetweenCheckpoints(500);

        env.getCheckpointConfig().setMaxConcurrentCheckpoints(1);
        env.getCheckpointConfig().setExternalizedCheckpointCleanup(CheckpointConfig.ExternalizedCheckpointCleanup.RETAIN_ON_CANCELLATION);

        DataStreamSource<EventLog> dataStreamSource = env.addSource(new MyRichSourceFunction());
        dataStreamSource.keyBy(EventLog::getEventId)
                .process(new KeyedProcessFunction<String, EventLog, String>() {
                    private ValueState<Long> state;

                    @Override
                    public void open(Configuration parameters) throws Exception {
                        ValueStateDescriptor<Long> stateDescriptor = new ValueStateDescriptor<>("state", Types.LONG);
                        state = getRuntimeContext().getState(stateDescriptor);
                    }

                    @Override
                    public void processElement(EventLog value, KeyedProcessFunction<String, EventLog, String>.Context ctx, Collector<String> out) throws Exception {
                        if (state.value() != null) {
                            System.out.println("状态里面有数据 :" + state.value());
                            state.update(state.value() + value.getGuid());
                        } else {
                            state.update(value.getGuid());
                        }
                        out.collect(value.toString());
                    }
                }).uid("my-uid")
                .print("local-print");


        env.execute();
    }
}
