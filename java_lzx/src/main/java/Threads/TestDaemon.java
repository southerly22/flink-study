package Threads;

/**
 * @author lzx
 * @date 2023/2/27 14:32
 * @description: TODO
 */
public class TestDaemon {
    public static void main(String[] args) {
        MyDaemon daemon = new MyDaemon();
        daemon.setDaemon(true); //设置线程为守护线程
        daemon.start();
        System.out.println(daemon.isDaemon());

        for (int i = 0; i < 50; i++) {
            System.out.println(Thread.currentThread().getName() + "," + i);
        }
    }
}
