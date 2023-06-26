package BlockingQueue.DelayQueue;

import java.util.UUID;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.TimeUnit;

/**
 * @author lzx
 * @date 2023/6/25 15:39
 * @description: TODO 创建延时队列，插入或者移除元素
 * 1.单例模式创建延时队列
 * 2.插入元素与移除元素
 */
public class MyDelayQueue {
    private static DelayQueue<MyDelayMessage> delayQueue = new DelayQueue<>();

    // add 返回true 表名插入成功
    public Boolean producer(MyDelayMessage message){
        return delayQueue.add(message);
    }

    // 取不到会一直阻塞
    public MyDelayMessage consumer() throws InterruptedException {
        return delayQueue.take();
    }

    public static void main(String[] args) {
        // 生成订单号
        // String id = UUID.randomUUID().toString();

        // MyDelayQueue myDelayQueue = new MyDelayQueue();
        // myDelayQueue.producer(new MyDelayMessage(id,10));


        Runnable producer = ()->{
            String id = UUID.randomUUID().toString();
            System.out.println("生产id = " + id);
            MyDelayQueue myDelayQueue = new MyDelayQueue();
            myDelayQueue.producer(new MyDelayMessage(id,1));
        };

        Runnable consumer = () ->{
            MyDelayQueue myDelayQueue = new MyDelayQueue();
            try {
                MyDelayMessage message = myDelayQueue.consumer();
                if (message != null) {
                    System.out.println("有订单过期了"+message.getOrderId());
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        };

        for (int i = 0; i < 2; i++) {
            new Thread(producer).start();
        }

        for (int i = 0; i < 3; i++) {
            new Thread(consumer).start();
        }

    }
}
