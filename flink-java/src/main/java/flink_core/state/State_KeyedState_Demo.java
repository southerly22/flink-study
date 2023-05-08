package flink_core.state;

import org.apache.flink.api.common.functions.RichMapFunction;
import org.apache.flink.api.common.functions.RuntimeContext;
import org.apache.flink.api.common.restartstrategy.RestartStrategies;
import org.apache.flink.api.common.state.ListState;
import org.apache.flink.api.common.state.ListStateDescriptor;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.CheckpointingMode;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

/**
 * @author lzx
 * @date 2023/5/8 18:04
 * @description: TODO 键控状态，只应用于KeyedStream的算子中（keyby后的处理算子中）
 * 算子为每一个key 绑定一份独立的状态，与并行度无关
 */
public class State_KeyedState_Demo {
    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.enableCheckpointing(1000, CheckpointingMode.EXACTLY_ONCE);
        env.getCheckpointConfig().setCheckpointStorage("file:///D:\\WorkPlace\\flink-study\\flink-java\\ck\\");
        env.setParallelism(1);

        // 开启Task 级别的故障自动修复（使用state 必须开启）
        env.setRestartStrategy(RestartStrategies.fixedDelayRestart(3, 1000));

        DataStreamSource<String> source = env.socketTextStream("192.168.1.76", 9999);

        source.keyBy(s -> s)
                .map(new RichMapFunction<String, String>() {

                    ListState<String> listState; //成员变量

                    // richFunction 生命周期方法 里面定义state
                    @Override
                    public void open(Configuration parameters) throws Exception {
                        RuntimeContext runtimeContext = getRuntimeContext();
                        listState = runtimeContext.getListState(new ListStateDescriptor<String>("string", String.class));
                    }

                    @Override
                    public String map(String value) throws Exception {
                        listState.add(value);
                        StringBuilder sb = new StringBuilder();
                        for (String s : listState.get()) {
                            sb.append("-" + s);
                        }
                        return sb.toString();
                    }
                }).setParallelism(2)
                .print().setParallelism(2);

        env.execute();
    }
}
