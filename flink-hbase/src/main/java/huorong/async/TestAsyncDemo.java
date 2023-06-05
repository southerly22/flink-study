package huorong.async;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import huorong.entity.SampleUerdefinedScan;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.restartstrategy.RestartStrategies;
import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.connector.kafka.source.KafkaSource;
import org.apache.flink.connector.kafka.source.enumerator.initializer.OffsetsInitializer;
import org.apache.flink.runtime.state.hashmap.HashMapStateBackend;
import org.apache.flink.streaming.api.CheckpointingMode;
import org.apache.flink.streaming.api.datastream.AsyncDataStream;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.CheckpointConfig;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.TopicPartition;

import java.util.HashMap;
import java.util.concurrent.TimeUnit;

/**
 * @author lzx
 * @date 2023/06/05 14:37
 **/
public class TestAsyncDemo {
    public static void main(String[] args) throws Exception {
        Configuration configuration = new Configuration();
        configuration.setInteger("rest.port", 8085);
        StreamExecutionEnvironment env = StreamExecutionEnvironment.createLocalEnvironmentWithWebUI(configuration);

        // 检查点
        env.enableCheckpointing(3000, CheckpointingMode.EXACTLY_ONCE);
        env.getCheckpointConfig().setCheckpointStorage("file:///D:\\WorkPlace\\flink-study\\flink-hbase\\ck");
        env.getCheckpointConfig().setTolerableCheckpointFailureNumber(3);//容忍 ck 失败最大次数
        env.getCheckpointConfig().setExternalizedCheckpointCleanup(CheckpointConfig.ExternalizedCheckpointCleanup.DELETE_ON_CANCELLATION);
        // 容错
        env.setRestartStrategy(RestartStrategies.noRestart());
        // 状态后端
        env.setStateBackend(new HashMapStateBackend());

        // 指定分区
        HashMap<TopicPartition, Long> topicMap = new HashMap<>();
        topicMap.put(new TopicPartition("task_mapping", 0), 129630118L);
        topicMap.put(new TopicPartition("task_mapping", 1), 129630606L);
        topicMap.put(new TopicPartition("task_mapping", 2), 129630453L);
        topicMap.put(new TopicPartition("task_mapping", 3), 129630300L);
        topicMap.put(new TopicPartition("task_mapping", 4), 129630667L);
        topicMap.put(new TopicPartition("task_mapping", 5), 129630380L);

        KafkaSource<String> kafkaSource = KafkaSource.<String>builder()
                .setBootstrapServers("192.168.1.56:9092,192.168.1.61:9092,192.168.1.58:9092,192.168.3.71:9092,192.168.3.178:9092")
                .setTopics("task_mapping")
                .setGroupId("test0526")
                .setProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "false")
                .setProperty("commit.offsets.on.checkpoint", "false") // 指定是否在进行 checkpoint 时将消费位点提交至 Kafka broker
                .setValueOnlyDeserializer(new SimpleStringSchema())
                .setStartingOffsets(OffsetsInitializer.offsets(topicMap))
                .build();

        DataStreamSource<String> kafkaStream = env.fromSource(kafkaSource, WatermarkStrategy.noWatermarks(), "kafkaSource");

        SingleOutputStreamOperator<SampleUerdefinedScan> filterDS = kafkaStream
                .setParallelism(6)
                .map(s -> JSON.parseObject(s, SampleUerdefinedScan.class))
                .filter(sample -> "U".equals(sample.getTask_type()) && sample.getSha1() != null && !"".equals(sample.getSha1().trim()));

        // 关联维度信息
        SingleOutputStreamOperator<SampleUerdefinedScan> asyncDS = AsyncDataStream.unorderedWait(
                filterDS, new CustomAsyncCommon<SampleUerdefinedScan>("SAMPLE_SRC") {
                    @Override
                    public String getKey(SampleUerdefinedScan input) {
                        return input.getSha1();
                    }
                    @Override
                    public void join(SampleUerdefinedScan input, JSONObject jsonObject) {
                        input.setRk(jsonObject.getString("rk"));
                        input.setSrc_id(jsonObject.getString("src_id"));
                        input.setSrc_name(jsonObject.getString("src_name"));
                        input.setAdd_timestamp(jsonObject.getLong("add_timestamp"));
                    }
                }, 100, TimeUnit.SECONDS
        );
        asyncDS.print();
        env.execute("ASYNC");
    }
}
