package flink_core.sink;

import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.CheckpointingMode;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

/**
 * @author lzx
 * @date 2023/4/21 18:43
 * @description: TODO
 * 在不同的数据流上调用process算子时，所需要传入的processFunction也是不同的
 */
public class ProcessFunctions_Demo {
    public static void main(String[] args) {
        Configuration configuration = new Configuration();
        configuration.setInteger("rest.port",8085);
        StreamExecutionEnvironment env = StreamExecutionEnvironment.createLocalEnvironmentWithWebUI(configuration);

        //开启ck
        env.enableCheckpointing(5000, CheckpointingMode.EXACTLY_ONCE);
        env.getCheckpointConfig().setCheckpointStorage("file:///D:\\WorkPlace\\flink-study\\flink-java\\ck\\");
    }
}
