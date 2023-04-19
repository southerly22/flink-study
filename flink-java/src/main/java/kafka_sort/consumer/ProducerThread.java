package kafka_sort.consumer;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.util.Properties;

/**
 * 生产者线程
 *
 * @author lzx
 * @date 2023/04/17 10:58
 **/
public class ProducerThread implements Runnable {

    private final KafkaProducer<String,String> producer;
    private final int numPartitions;
    private final String topicName;

    public ProducerThread(Properties prop, int numPartitions,String topicName) {
        this.producer = new KafkaProducer<String, String>(prop);
        this.numPartitions = numPartitions;
        this.topicName = topicName;
    }

    @Override
    public void run() {
     try {
         for (int i = 0; i < 100; i++) {
             String key = String.valueOf(i);
             String message = "message "+ i;
             int partition = Math.abs(key.hashCode()) % numPartitions;
             ProducerRecord<String, String> producerRecord = new ProducerRecord<>(topicName, partition, key, message);
             producer.send(producerRecord);
         }
     }finally {
         producer.close();
     }
    }

    public static void main(String[] args) {
        int numThreads = 3;
        int numPartitions = 3;
        String topicName = "lzx_test1";
        Properties props = new Properties();
        props.put("bootstrap.servers", "192.168.1.56:9092,192.168.1.61:9092,192.168.1.58:9092,192.168.1.71:9092,192.168.1.178:9092");
        props.put("group.id", "lzx_test0417");
        props.put("enable.auto.commit", "true");
        props.put("auto.commit.interval.ms", "1000");
        props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");

        new Thread(new ProducerThread(props, numPartitions, topicName)).start();
    }
}
