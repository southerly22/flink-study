package flink_sql.test;

import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.serialization.DeserializationSchema;
import org.apache.flink.api.common.serialization.SerializationSchema;
import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.api.common.typeinfo.BasicTypeInfo;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.connector.kafka.source.KafkaSource;
import org.apache.flink.connector.kafka.source.enumerator.initializer.OffsetsInitializer;
import org.apache.flink.formats.json.JsonRowDataDeserializationSchema;
import org.apache.flink.formats.json.JsonRowDeserializationSchema;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.util.serialization.JSONKeyValueDeserializationSchema;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * @author lzx
 * @date 2023/6/29 17:45
 * @description: TODO
 */
public class KafkaDataStream {
    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        KafkaSource<String> kafkaSource = KafkaSource.<String>builder()
                .setBootstrapServers("192.168.1.56:9092,192.168.1.61:9092,192.168.1.58:9092,192.168.3.71:9092,192.168.3.178:9092")
                .setTopics("test0629")
                .setGroupId("gid0629")
                .setStartingOffsets(OffsetsInitializer.earliest())
                .setValueOnlyDeserializer(new MySimpleStringSchema())
                .setDeserializer()
                .setProperty("auto.offset.commit", "false")
                .build();
        DataStreamSource<String> kfkDs = env.fromSource(kafkaSource, WatermarkStrategy.noWatermarks(), "kfk");
        kfkDs.print();

        env.execute();
    }

    // 自定义反序列化器
    static class MySimpleStringSchema implements DeserializationSchema<String>, SerializationSchema<String>{

        @Override
        public String deserialize(byte[] message) {
            if (message != null) return new String(message, StandardCharsets.UTF_8);
            else{
                return deserialize(new byte[1]); // 返回空 不是Null
            }
        }

        @Override
        public boolean isEndOfStream(String nextElement) {
            return false;
        }

        @Override
        public byte[] serialize(String element) {
            return element.getBytes(StandardCharsets.UTF_8);
        }

        @Override
        public TypeInformation<String> getProducedType() {
            return BasicTypeInfo.STRING_TYPE_INFO;
        }
    }

    static class MyKafkaDeserialization implements KafkaDeserializationSchema<Jason> {
        private static final Logger log = Logger.getLogger(MyKafkaDeserialization.class);
        private final String encoding = "UTF8";
        private boolean includeTopic;
        private boolean includeTimestamp;

        public MyKafkaDeserialization(boolean includeTopic, boolean includeTimestamp) {
            this.includeTopic = includeTopic;
            this.includeTimestamp = includeTimestamp;
        }

        @Override
        public TypeInformation<Jason> getProducedType() {
            return TypeInformation.of(Jason.class);
        }

        @Override
        public boolean isEndOfStream(Jason nextElement) {
            return false;
        }

        @Override
        public Jason deserialize(ConsumerRecord<byte[], byte[]> consumerRecord) throws Exception {
            if (consumerRecord != null) {
                try {
                    String value = new String(consumerRecord.value(), encoding);
                    Jason jason = JSON.parseObject(value, Jason.class);
                    if (includeTopic) jason.setTopic(consumerRecord.topic());
                    if (includeTimestamp) jason.setTimestamp(consumerRecord.timestamp());
                    return jason;
                } catch (Exception e) {
                    log.error("deserialize failed : " + e.getMessage());
                }
            }
            return null;
        }
    }
}
