package Threads;

/**
 * @author lzx
 * @date 2023/2/27 11:31
 * @description: TODO 创建线程的方法一：继承Thread类
 */
public class MyThread extends Thread{

    public MyThread(String name) {
        super(name);
    }

    @Override
    public void run() {
        for (int i = 0; i < 100; i++) {
            if (i % 2==0){
                System.out.println(Thread.currentThread().getName() + " , " + i);
            }
        }
    }
}
