package flink_core.source;

import org.apache.flink.api.java.functions.KeySelector;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

/**
 * @author lzx
 * @date 2023/6/15 18:27
 * @description: TODO
 */
public class TestSource {
    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        DataStreamSource<EventLog> source = env.addSource(new MyRichSourceFunction());
        source.keyBy(new KeySelector<EventLog, Long>() {
            @Override
            public Long getKey(EventLog value) throws Exception {
                return value.getGuid();
            }
        }).print();
        env.execute();
    }
}
