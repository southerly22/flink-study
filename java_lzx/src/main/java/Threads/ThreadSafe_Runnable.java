package Threads;

/**
 * @author lzx
 * @date 2023/2/28 10:04
 * @description: TODO 同步方法解决线程安全问题 非静态方法
 */
class Window implements Runnable {
    int tickets = 100;

    @Override
    public void run() {
       while (tickets > 0){
           saleOneTicket();
       }
    }
    //锁对象是this，这里就是Window对象，因为下面3个线程使用同一个Window对象，唯一 所以可以
    private synchronized void saleOneTicket() {
        if (tickets > 0){
            System.out.println(Thread.currentThread().getName()+"售卖票号:"+tickets);
            tickets--;
        }
    }
}

public class ThreadSafe_Runnable {
    public static void main(String[] args) {
        Window window = new Window();
        Thread t1 = new Thread(window, "t1");
        Thread t2 = new Thread(window, "t2");
        Thread t3 = new Thread(window, "t3");

        t1.start();
        t2.start();
        t3.start();
    }
}
