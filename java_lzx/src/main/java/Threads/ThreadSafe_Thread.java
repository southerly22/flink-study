package Threads;

/**
 * @author lzx
 * @date 2023/2/28 10:04
 * @description: TODO
 */
class Window1 extends Thread{
    private static int tickets = 100;
    @Override
    public void run() {
        while (tickets > 0){
            saleOneTicket();
        }
    }
    //锁对象是Window1类的Class对象，而一个类的Class对象在内存中肯定只有一个
    private synchronized static void saleOneTicket() {
        if (tickets > 0){
            System.out.println(Thread.currentThread().getName()+"售卖票号:"+tickets);
            tickets--;
        }
    }
}
public class ThreadSafe_Thread {
    public static void main(String[] args) {
        Window1 t1 = new Window1();
        Window1 t2 = new Window1();
        Window1 t3 = new Window1();

        t1.setName("窗口1");
        t2.setName("窗口2");
        t3.setName("窗口3");

        t1.start();
        t2.start();
        t3.start();
    }
}
