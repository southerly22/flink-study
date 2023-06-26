package BlockingQueue.DelayQueue;

import java.util.concurrent.DelayQueue;
import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

/**
 * @author lzx
 * @date 2023/6/25 16:17
 * @description: TODO 使用延时队列 实现生产者消费者 需求
 */
public class DelayQueue_Demo {
    public static void main(String[] args) {
        DelayQueue<Message> delayQueue = new DelayQueue<>();

        //创建生产者和消费者 线程
        Thread producerThread = new Thread(new Producer(delayQueue));
        Thread consumerThread = new Thread(new Consumer(delayQueue));

        // 启动线程
        producerThread.start();
        consumerThread.start();
    }
}
// 消息类，实现Delayed接口
class Message implements Delayed {
    private String content;
    private long delayTime; // 延迟时间
    private long expireTime; // 到期时间

    public Message(String content, long delayTime) {
        this.content = content;
        this.delayTime = delayTime;
        this.expireTime = System.currentTimeMillis() + delayTime;
    }

    // 获取剩余延迟时间
    @Override
    public long getDelay(TimeUnit unit) {
        return unit.convert(expireTime - System.currentTimeMillis(), TimeUnit.MILLISECONDS);
    }

    // 比较优先级
    @Override
    public int compareTo(Delayed other) {
        return Long.compare(this.expireTime, ((Message) other).expireTime);
    }

    public String getContent() {
        return content;
    }
}

// 生产者线程
class Producer implements Runnable {
    private DelayQueue<Message> delayQueue;

    public Producer(DelayQueue<Message> delayQueue) {
        this.delayQueue = delayQueue;
    }

    @Override
    public void run() {
        try {
            delayQueue.put(new Message("Hello", 2000)); // 延迟2秒
            delayQueue.put(new Message("World", 5000)); // 延迟5秒
            delayQueue.put(new Message("Java", 3000)); // 延迟3秒
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

// 消费者线程
class Consumer implements Runnable {
    private DelayQueue<Message> delayQueue;

    public Consumer(DelayQueue<Message> delayQueue) {
        this.delayQueue = delayQueue;
    }

    @Override
    public void run() {
        try {
            while (true) {
                Message message = delayQueue.take(); // 阻塞获取消息
                System.out.println("消费消息：" + message.getContent());
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
