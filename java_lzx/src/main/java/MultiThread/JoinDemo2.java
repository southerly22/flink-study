package MultiThread;

import Threads.MyThread;

import java.util.Random;

/**
 * @author lzx
 * @date 2023/3/28 13:38
 * @description: TODO Java中如何让多线程按照自己指定的顺序执行
 */
public class JoinDemo2 extends Thread {
    public static void main(String[] args) throws InterruptedException {
        Thread thread = new Thread(new MyThread());
        thread.start();
        thread.join(3*1000);
        System.out.println("Main start");
    }
     static class MyThread implements Runnable{

        @Override
        public void run() {
            System.out.println("MyThread start...");
            try {
                Thread.sleep(5*1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("MyThread end...");
        }
    }
}
