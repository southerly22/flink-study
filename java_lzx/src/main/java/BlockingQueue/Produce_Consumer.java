package BlockingQueue;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * @author lzx
 * @date 2023/6/25 13:43
 * @description: TODO 利用阻塞队列实现 生产者和消费者模式
 * 需求：2个厨师和三个顾客，假设厨师炒一个菜时间3秒 顾客吃一个菜的时间4s 窗口上仅能放一个菜
 */
public class Produce_Consumer {
    public static void main(String[] args) {
        BlockingQueue<Object> blockingQueue = new ArrayBlockingQueue<>(1);

        Runnable supplier = () -> {
            while (true) {
                try {
                    String name = Thread.currentThread().getName();
                    System.err.println(time() + "消费者" + name + "等待出餐。。。");
                    TimeUnit.SECONDS.sleep(3);

                    System.err.println(time() + "生产者" + name + "已出餐。。。");
                    blockingQueue.put(new Object());

                } catch (InterruptedException e) {
                    e.printStackTrace();
                    break;
                }
            }
        };

        Runnable consumer = () -> {
            while (true) {
                try {
                    String name = Thread.currentThread().getName();
                    System.out.println(time() + "消费者" + name + "等待出餐。。。");
                    blockingQueue.take();
                    System.out.println(time() + "消费者" + name + "取到了餐。。。");
                    TimeUnit.SECONDS.sleep(4);
                    System.out.println(time() + "消费者" + name + "已吃完菜。。。");

                } catch (InterruptedException e) {
                    e.printStackTrace();
                    break;
                }
            }
        };
        // 两个生产者
        for (int i = 0; i < 2; i++) {
            new Thread(supplier, "supplier" + i).start();
        }

        // 三个消费者
        for (int i = 0; i < 3; i++) {
            new Thread(consumer, "consumer" + i).start();
        }
    }

    private static String time() {
        SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss");
        return "[" + format.format(new Date()) + "]";
    }

}
