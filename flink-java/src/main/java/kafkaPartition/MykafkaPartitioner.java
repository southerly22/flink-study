package kafkaPartition;

import org.apache.kafka.clients.producer.Partitioner;
import org.apache.kafka.common.Cluster;

import java.util.Map;

/**
 * @author lzx
 * @date 2023/4/14 18:06
 * @description: TODO 自定义分区器，实现发送过来的数据中如果含有test，就发往0号分区，不包含test就发往1号分区
 *
 * 既没有partition值有没有key值的情况下，Kafka采用Sticky Partition（粘性分区器），会随机选择一个分区，并尽可能一直使用该分区，
 * 待该分区的batch已满或者已完成，Kafka再随机一个分区进行使用（和上一次的分区不同）。
 * 例如：第一次随机选择0号分区，等0号分区当前批次满了（默认16K）或者linger.ms设置的时间到，Kafka再随机一个分区进行使用（如果还是0会继续随机）
 */
public class MykafkaPartitioner implements Partitioner {
    /***
     * @Author: lzx
     * @Description:
     * @Date: 2023/4/14
     * @Param topic: 主题
     * @Param key: 消息的key
     * @Param keyBytes: 消息key序列化数组
     * @Param value:
     * @Param valueBytes:
     * @Param cluster:  集群元数据，可以查看分区信息
     * @return: int
     **/
    @Override
    public int partition(String topic, Object key, byte[] keyBytes, Object value, byte[] valueBytes, Cluster cluster) {
        String msgValue = value.toString();
        int partition;
        if (msgValue.contains("test")) {
            partition = 0;
        }else {
            partition = 1;
        }
        return partition;
    }

    @Override
    public void close() {

    }

    @Override
    public void configure(Map<String, ?> configs) {

    }
}
