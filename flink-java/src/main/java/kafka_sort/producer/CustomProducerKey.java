package kafka_sort.producer;

import org.apache.kafka.clients.producer.*;
import org.apache.kafka.common.serialization.StringSerializer;

import java.util.Properties;

/**
 * @author lzx
 * @date 2023/4/14 17:53
 * @description: TODO 指定key 不指定partition，会根据key的hash得到分区号
 */
public class CustomProducerKey {
    public static void main(String[] args) {
        //1. 创建kafka生产者配置对象
        Properties properties = new Properties();
        properties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "node03:9092,node04:9092,node05:9092,node71:9092,node178:9092");
        properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        KafkaProducer<Object, Object> kafkaProducer = new KafkaProducer<>(properties);

        String[] chars = {"a", "b", "f"};
        for (int j = 0; j < chars.length; j++) {
            for (int i = 0; i < 5; i++) {
                // 依次指定key值为a,b,f,数据key的hash与3个分区取余，分别发往1、2、0
                kafkaProducer.send(new ProducerRecord<>("kafkaOrderTest", chars[j], "testMessage" + i + chars[j]), new Callback() {
                    @Override
                    public void onCompletion(RecordMetadata metadata, Exception e) {
                        if (e == null) {
                            System.out.println("主题 " + metadata.topic() + "--> " + "分区 " + metadata.partition());
                        } else {
                            e.printStackTrace();
                        }
                    }
                });
            }
        }


        kafkaProducer.close();
    }
}
