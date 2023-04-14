package kafkaPartition;

import org.apache.kafka.clients.producer.*;
import org.apache.kafka.common.serialization.StringSerializer;

import java.util.Properties;

/**
 * @author lzx
 * @date 2023/4/14 17:36
 * @description: TODO 指定partition 不指定key
 */
public class CustomPorducerPartitions {
    public static void main(String[] args) {
        //1. 创建kafka生产者配置对象
        Properties properties = new Properties();
        properties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,"node03:9092,node04:9092,node05:9092,node71:9092,node178:9092");
        properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());

        KafkaProducer<Object, Object> kafkaProducer = new KafkaProducer<>(properties);
        // 发送数据
        for (int i = 0; i < 5; i++) {
            // 往分区 1 里面发送数据，指定 key 为空
            kafkaProducer.send(new ProducerRecord<Object, Object>("kafkaOrderTest", 1, "", "testMessage" + i), new Callback() {
                @Override
                public void onCompletion(RecordMetadata metadata, Exception exception) {
                    if (exception == null){
                        System.out.println("主题 "+ metadata.topic()+"--> "+"分区 "+metadata.partition());
                    }else {
                        exception.printStackTrace();
                    }
                }
            });
        }
        // 关闭生产者
        kafkaProducer.close();
    }
}
