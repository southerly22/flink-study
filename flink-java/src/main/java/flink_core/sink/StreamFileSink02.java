package flink_core.sink;

import org.apache.flink.streaming.api.CheckpointingMode;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import flink_core.source.EventLog;
import flink_core.source.MyRichSourceFunction;


/***
 * @Author: lzx
 * @Description: 写数据到文件中
 * @Date: 2022/12/20
 **/
public class StreamFileSink02 {
    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        // 开启ck
        env.enableCheckpointing(5000, CheckpointingMode.EXACTLY_ONCE);
        env.getCheckpointConfig().setCheckpointStorage("file:///d:/ckpt");

        //添加source
        DataStreamSource<EventLog> streamSource = env.addSource(new MyRichSourceFunction());

        // ParquetAvroWriters.forSpecificRecord(Event.class)
        //构建一个fileSink



        env.execute();
    }
}
