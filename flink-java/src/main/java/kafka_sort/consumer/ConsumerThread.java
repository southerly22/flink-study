package kafka_sort.consumer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.TopicPartition;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;

/**
 * 消费者线程
 *
 * @author lzx
 * @date 2023/04/17 10:50
 **/
public class ConsumerThread implements Runnable{
    private final Log logger = LogFactory.getFactory().getInstance(ConsumerThread.class);
    private final KafkaConsumer<String,String> consumer;

    //为了将每个线程绑定到特定的队列，可以使用ConcurrentHashMap来存储分区到队列的映射。
    // 然后，每个线程可以从映射中检索与其分配的分区相关联的队列，并从中消费消息。
    private final ConcurrentHashMap<Integer, BlockingQueue<String>> queues;

    private final Map<String, Semaphore[]> semaphore;

    private final String threadName;

    public ConsumerThread(String threadName, Properties prop, Map<String, List<TopicPartition>> partitions, ConcurrentHashMap<Integer, BlockingQueue<String>> queues, Map<String, Semaphore[]> semaphore) {
        this.threadName = threadName;
        this.consumer = new KafkaConsumer<String, String>(prop);
        this.consumer.assign(partitions.get(threadName));
        this.queues = queues;
        this.semaphore = semaphore;
    }




    @Override
    public void run() {
        try {
            while (true){
                ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(100));
                if (semaphore.get(threadName)[0] != null) {
                    semaphore.get(threadName)[0].acquire();
                    logger.warn(threadName+"阻塞");
                }
                if (threadName.contains("0")||threadName.contains("2")){
                    Thread.sleep(10000);
                }
                for (ConsumerRecord<String, String> record : records) {
                    int partition = record.partition();
                    BlockingQueue<String> queue = queues.get(partition);
                    queue.put(record.value());
                    System.out.println(Thread.currentThread().getName()+","+queue.take());
                }
                semaphore.get(threadName)[1].release();
                logger.warn(threadName+"释放");
                //ConcurrentHashMap.KeySetView<Integer, BlockingQueue<String>> keySet = queues.keySet();
                //for (int partition : keySet) {
                //    for (String msg : queues.get(partition)) {
                //        logger.info(partition+","+msg);
                //    }
                //}
            }

        } catch (InterruptedException e) {
            e.printStackTrace();
        }finally {
            consumer.close();
        }
    }
}
