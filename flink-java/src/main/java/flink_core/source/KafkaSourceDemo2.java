package flink_core.source;

import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.connector.kafka.source.KafkaSource;
import org.apache.flink.connector.kafka.source.enumerator.initializer.OffsetsInitializer;
import org.apache.flink.streaming.api.CheckpointingMode;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.environment.CheckpointConfig;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.KeyedProcessFunction;
import org.apache.flink.util.Collector;
import org.apache.kafka.common.TopicPartition;

import java.util.HashSet;

public class KafkaSourceDemo2 {
    public static void main(String[] args) throws Exception {

        Configuration configuration = new Configuration();
        configuration.setInteger("rest.port", 8085);
        StreamExecutionEnvironment env = StreamExecutionEnvironment.createLocalEnvironmentWithWebUI(configuration);

        env.enableCheckpointing(3000, CheckpointingMode.EXACTLY_ONCE);
        CheckpointConfig checkpointConfig = env.getCheckpointConfig();
        checkpointConfig.setCheckpointStorage("file:////Users/liuzhixin/codeplace/flink-study/flink-java/ck");

        KafkaSource<String> kafkaSource = KafkaSource.<String>builder()
                .setBootstrapServers("localhost:9094,localhost:9092,localhost:9093")
                .setTopics("lzx_0912")
                .setGroupId("lzx_test04912")
                .setStartingOffsets(OffsetsInitializer.earliest())
                .setValueOnlyDeserializer(new SimpleStringSchema())
                .setProperty("auto.offset.commit", "false")
                .build();

        //env.addSource(); // 接收的是SourceFunction 接口的实现类
        //env.fromSource(); // 接收的是Source 接口的实现类

        DataStreamSource<String> kfkDs = env.fromSource(kafkaSource, WatermarkStrategy.noWatermarks(), "kfk");
        kfkDs.print();

        kfkDs.keyBy(s->"1")
                        .process(new KeyedProcessFunction<String, String, String>() {
                            @Override
                            public void open(Configuration parameters) throws Exception {
                                getRuntimeContext();
                            }

                            @Override
                            public void processElement(String value, KeyedProcessFunction<String, String, String>.Context ctx, Collector<String> out) throws Exception {

                            }
                        });

        env.execute();
    }
}
