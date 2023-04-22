package flink_core.source;

import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.CheckpointingMode;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

/**
 * 文件输入源
 *
 * @author lzx
 * @date 2023/04/21 19:25
 **/
public class FileSourceStream {
    public static void main(String[] args) throws Exception {
        Configuration configuration = new Configuration();
        configuration.setInteger("rest.port",8085);
        StreamExecutionEnvironment env = StreamExecutionEnvironment.createLocalEnvironmentWithWebUI(configuration);

        //开启ck
        env.enableCheckpointing(5000, CheckpointingMode.EXACTLY_ONCE);
        env.getCheckpointConfig().setCheckpointStorage("file:///Users\\liuzhixin\\codeplace\\flink-study\\flink-java\\ck");

        DataStreamSource<String> textFileDS = env.readTextFile("/Users/liuzhixin/Desktop/all.txt");

        textFileDS.print();

        env.execute("FileSourceStream");
    }
}
