package dorisDemo;

import org.apache.doris.flink.cfg.DorisOptions;
import org.apache.doris.flink.cfg.DorisReadOptions;
import org.apache.doris.flink.deserialization.SimpleListDeserializationSchema;
import org.apache.doris.flink.source.DorisSource;
import org.apache.doris.flink.source.DorisSourceBuilder;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

import java.util.List;

/**
 * @author lzx
 * @date 2023/6/21 13:59
 * @description: TODO
 */
public class Demo1 {
    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        DorisOptions.Builder builder = DorisOptions.builder()
                .setFenodes("192.168.1.73:8130")
                .setTableIdentifier("tmp.TMP_HANDLE_DATA_SCAN_NAME")
                .setUsername("root")
                .setPassword("000000");

        DorisSource<List<?>> dorisSource = DorisSourceBuilder.<List<?>>builder()
                .setDorisOptions(builder.build())
                .setDorisReadOptions(DorisReadOptions.builder().build())
                .setDeserializer(new SimpleListDeserializationSchema())
                .build();

        env.fromSource(dorisSource, WatermarkStrategy.noWatermarks(),"doris_source").print();

        env.execute();
    }
}
