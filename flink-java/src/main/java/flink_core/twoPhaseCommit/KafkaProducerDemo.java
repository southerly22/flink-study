package flink_core.twoPhaseCommit;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;

import java.util.Properties;

/**
 * @author lzx
 * @date 2023/5/17 13:29
 * @description: TODO
 */
public class KafkaProducerDemo {
    private static final String broker_list = "192.168.1.56:9092,192.168.1.61:9092,192.168.1.58:9092,192.168.3.71:9092,192.168.3.178:9092";
    //flink 读取kafka写入mysql exactly-once 的topic
    private static final String topic_ExactlyOnce = "eos0515";

    public static void writeToKafka2() throws InterruptedException {
        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,broker_list);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);

        KafkaProducer<String, Tuple2<String, Integer>> kafkaProducer = new KafkaProducer<>(props);

        Tuple2<String, Integer> tuple2 = new Tuple2<>();
        for (int i = 0; i < 20; i++) {
            tuple2.setFields("lisi"+i,i);
            ProducerRecord<String, Tuple2<String,Integer>> producerRecord = new ProducerRecord<>(topic_ExactlyOnce, null, null, tuple2);
            kafkaProducer.send(producerRecord);
            System.out.println("发送数据 "+ tuple2);
            Thread.sleep(1000);
        }
        kafkaProducer.flush();
    }

    public static void main(String[] args) throws InterruptedException {
        writeToKafka2();
    }
}
