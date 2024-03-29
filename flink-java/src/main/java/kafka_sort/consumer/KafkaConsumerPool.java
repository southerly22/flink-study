package kafka_sort.consumer;

import com.alibaba.fastjson.JSON;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

/**
 * kafka顺序消费工具类线程池
 *
 * @author lzx
 * @date 2023/04/16 16:06
 **/

@Slf4j
@Data
public class KafkaConsumerPool<E> {

    // 线程并发级别
    private Integer concurrentSize;

    // 工作线程数
    private List<Thread> workThreads;

    // 任务处理队列
    private List<ConcurrentLinkedQueue<E>> queues;

    // 是否全量停止任务，留个钩子，以便后续动态扩容
    private volatile boolean stopped;

    // 待提交的记录数
    private AtomicLong pendingOffsets;

    // kafka线程名字前缀
    private final static String KAFKA_CONSUMER_WORK_THREAD_PREFIX = "kafka-sort-consumer-thread-";

    /**
     * 顺序消费 任务池初始化
     */
    public KafkaConsumerPool(KafkaSortConsumerConfig<E> config) {
        this.concurrentSize = config.getConcurrentSize();
        // 初始化队列
    }

    // 初始化队列
    private void initQueue() {
        this.queues = new ArrayList<>();
        for (int i = 0; i < this.concurrentSize; i++) {
            this.queues.add(new ConcurrentLinkedQueue<>());
        }
    }

    /**
     * @param bizName    业务名称
     * @param bizService 业务处理服务
     * @return void
     * @author lzx
     * @date 2023-04-16 17:38
     */
    private void initWorkThread(String bizName, Consumer<E> bizService) {
        // 创建规定的线程
        for (int i = 0; i < this.concurrentSize; i++) {
            String threadName = KAFKA_CONSUMER_WORK_THREAD_PREFIX + bizName + i;
            int num = i;
            Thread workThread = new Thread(() -> {
                // 如果队列不为空 或者线程标识为false则进入循环
                while (!queues.get(num).isEmpty() || !stopped) {
                    try {
                        E task = pollTask(threadName, bizName);
                        if (Objects.nonNull(task)) {

                            // 模拟业务处理耗时
                            bizService.accept(task);

                            log.info("线程：{},执行任务：{}，成功", threadName, JSON.toJSON(task));

                            // 执行完成的任务加1
                            pendingOffsets.incrementAndGet();
                        }
                    } catch (Exception e) {
                        log.error("线程：{},执行任务：{},失败", threadName, e);
                    }
                }
                log.info("线程：{}退出", threadName);
            }, threadName);

            // 加入线程管理
            workThreads.add(workThread);

            // 开启线程
            workThread.start();
        }
    }

    /**
     * @param id   能够取模的键
     * @param task 需要提交处理的任务
     * @return void
     * @author lzx
     * @date 2023-04-16 17:48
     */
    public void submitTask(long id, E task) {
        ConcurrentLinkedQueue<E> taskQueue = queues.get((int) (id % this.concurrentSize));
        taskQueue.offer(task);
    }

    /**
     * 根据线程名获取对应的待执行的任务
     *
     * @param threadName 线程名称
     * @param bizName    业务名称
     * @return 队列内任务
     * @author lzx
     * @date 2023-04-16 17:42
     */
    private E pollTask(String threadName, String bizName) {
        int threadNum = Integer.valueOf(threadName.replace(KAFKA_CONSUMER_WORK_THREAD_PREFIX + bizName, ""));
        ConcurrentLinkedQueue<E> taskQueue = queues.get(threadNum);
        return taskQueue.poll();
    }
}
