package BlockingQueue.DelayQueue;

import lombok.ToString;

import java.util.Random;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

/**
 * @author lzx
 * @date 2023/6/26 13:12
 * @description: TODO
 */
public class DelayQueueTest {

    static class DelayedMessage implements Delayed{
        private final long delayTime; //延迟时间
        private final long expireTime; //到期时间
        private String taskName;

        public DelayedMessage(long delayTime, String taskName) {
            this.delayTime = delayTime;
            this.expireTime = System.currentTimeMillis() + delayTime;
            this.taskName = taskName;
        }

        @Override
        public long getDelay(TimeUnit unit) {
            return unit.convert(expireTime - System.currentTimeMillis(),TimeUnit.MILLISECONDS);
        }

        @Override
        public int compareTo(Delayed other) {
            return Long.compare(this.getDelay(TimeUnit.MILLISECONDS), other.getDelay(TimeUnit.MILLISECONDS));
        }

        @Override
        public String toString() {
            return "DelayedEle{" +
                    "delayTime=" + delayTime +
                    ", expire=" + expireTime +
                    ", taskName='" + taskName + '\'' +
                    '}';
        }
    }

    public static void main(String[] args) throws InterruptedException {
        // 创建延迟队列
        DelayQueue<DelayedMessage> delayQueue = new DelayQueue<>();

        Random random = new Random();
        for (int i = 0; i < 10; i++) {
            // 每个任务延迟时间在[0,1000)之间
            DelayedMessage delayedMessage = new DelayedMessage(random.nextInt(1000), "Task " + i);
            delayQueue.add(delayedMessage);
        }

        while (true){
            System.out.println(delayQueue.take());
            if (delayQueue.isEmpty()) {
                break;
            }
        }
    }
}
