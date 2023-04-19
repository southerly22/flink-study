package kafka_sort.consumer;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.TopicPartition;

import java.util.*;
import java.util.concurrent.*;

/**
 * 测试
 *
 * @author lzx
 * @date 2023/04/17 11:04
 **/
public class KafkaThreadTest {
    public static void main(String[] args) throws InterruptedException {
        int numThreads = 3;
        int numPartitions = 3;
        String topicName = "lzx_test1";
        Properties props = new Properties();
        props.put("bootstrap.servers", "192.168.1.56:9092,192.168.1.61:9092,192.168.1.58:9092");
        props.put("group.id", "lzx_test04172");
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG,"earliest");
        props.put("enable.auto.commit", "false");
        props.put("auto.commit.interval.ms", "1000");
        props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");

        Map<String,List<TopicPartition>> threadPar = new ConcurrentHashMap<>();
        for (int i = 0; i < numPartitions; i++) {
        ArrayList<TopicPartition> partitions = new ArrayList<>();
            partitions.add(new TopicPartition(topicName,i));
            threadPar.put("ConsumerThread"+i,partitions);
        }

        ConcurrentHashMap<Integer, BlockingQueue<String>> queues = new ConcurrentHashMap<>();
        for (int i = 0; i < numPartitions; i++) {
            queues.put(i,new LinkedBlockingQueue<>());
        }

        Thread previousThread = Thread.currentThread();
        for (int i = 0; i < numThreads; i++) {
            ConsumerThread1 consumerThread = new ConsumerThread1("ConsumerThread"+i,props, threadPar, queues,previousThread);
            consumerThread.start();
            previousThread = consumerThread;
        }
    }
}
