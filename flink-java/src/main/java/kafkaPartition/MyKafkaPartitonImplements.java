package kafkaPartition;

import org.apache.kafka.clients.producer.*;
import org.apache.kafka.common.serialization.StringSerializer;

import java.util.Properties;

/**
 * @author lzx
 * @date 2023/4/14 18:10
 * @description: TODO
 */
public class MyKafkaPartitonImplements {
    public static void main(String[] args) {
        //1. 创建kafka生产者配置对象
        Properties properties = new Properties();
        properties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "node03:9092,node04:9092,node05:9092,node71:9092,node178:9092");
        System.out.println(StringSerializer.class.getName());
        properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        // 添加自定义分区器
        properties.put(ProducerConfig.PARTITIONER_CLASS_CONFIG,MykafkaPartitioner.class.getName());
        // 创建消费者
        KafkaProducer<Object, Object> kafkaProducer = new KafkaProducer<>(properties);

        for (int i = 0; i < 3; i++) {
            // 仅指定 topic 和 value
            kafkaProducer.send(new ProducerRecord<>("kafkaOrderTest", "testMessage" + i), new Callback() {
                @Override
                public void onCompletion(RecordMetadata metadata, Exception e) {
                    if (e==null){
                        System.out.println("主题 " + metadata.topic() + "--> " + "分区 " + metadata.partition());
                    }else {
                        e.printStackTrace();
                    }
                }
            });
        }
        kafkaProducer.close();
    }
}
