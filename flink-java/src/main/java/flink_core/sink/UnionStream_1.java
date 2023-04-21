package flink_core.sink;

import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.CheckpointingMode;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

/**
 * @author lzx
 * @date 2023/4/21 10:14
 * @description: TODO  union 可以将多个相同类型的流 合并为一个流
 *                     该方法 union(DataStream<T>... streams)参数为可变参数
 */
public class UnionStream_1 {
    public static void main(String[] args) throws Exception{
        Configuration configuration = new Configuration();
        configuration.setInteger("rest.port",8085);
        StreamExecutionEnvironment env = StreamExecutionEnvironment.createLocalEnvironmentWithWebUI(configuration);

        //开启ck
        env.enableCheckpointing(5000, CheckpointingMode.EXACTLY_ONCE);
        env.getCheckpointConfig().setCheckpointStorage("file:///D:\\WorkPlace\\flink-study\\flink-java\\ck\\");

        // 创建两个流
        DataStreamSource<Integer> dS1 = env.fromElements(2, 4, 6, 8);
        DataStreamSource<Integer> dS2 = env.fromElements(1, 3, 5, 7);

        // 合并两个流
        DataStream<Integer> res = dS1.union(dS2);
        res.print("union");

        env.execute("UnionStream_1");
    }
}
