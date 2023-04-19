package kafka_sort.consumer;

import lombok.Data;

import java.util.function.Consumer;

/**
 * kafka顺序消费线程池配置参数
 *
 * @author lzx
 * @date 2023/04/16 16:29
 **/
@Data
public class KafkaSortConsumerConfig<E> {
    // 业务名称
    String bizName;

    // 并发级别，多少的队列与线程处理任务
    Integer concurrentSize;

    // 业务处理服务
    Consumer<E> bizService;
}
