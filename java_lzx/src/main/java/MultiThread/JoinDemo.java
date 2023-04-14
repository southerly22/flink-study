package MultiThread;

import java.util.Random;

/**
 * @author lzx
 * @date 2023/3/28 13:38
 * @description: TODO Java中如何让多线程按照自己指定的顺序执行
 */
public class JoinDemo extends Thread {
    int i;
    Thread previousThread; // 上一个线程

    public JoinDemo(int i, Thread previousThread) {
        this.i = i;
        this.previousThread = previousThread;
    }

    @Override
    public void run() {
        // 增加一个随机的睡眠，不然会出现乱序情况，当previousThread.join() 被注释的时候
        Random random = new Random();
        int i = random.nextInt(1000);
        try {
            Thread.sleep(i);
            // 调用上一个线程的join方法，当前线程的正常运行需要上一个线程的结束，保证有序性
            previousThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("num :" + this.i);
    }

    public static void main(String[] args) {
        Thread previousThread = Thread.currentThread();
        for (int i = 0; i < 10; i++) {
            JoinDemo joinDemo = new JoinDemo(i, previousThread);
            joinDemo.start();
            previousThread = joinDemo;
        }
    }
}
