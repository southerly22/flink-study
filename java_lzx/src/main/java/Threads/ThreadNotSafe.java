package Threads;

/**
 * @author lzx
 * @date 2023/2/27 17:24
 * @description: TODO
 */

class Ticket implements Runnable{
   static int tickets = 100;
    @Override
    public void run() {
        while (tickets > 0){
           synchronized (this){
               System.out.println(Thread.currentThread().getName()+" 售票号码为："+tickets);
               tickets--;
           }
        }
    }
}

public class ThreadNotSafe {
    public static void main(String[] args) {
        Ticket ticket = new Ticket();
        //开三个售票窗口
        Thread t1 = new Thread(ticket, "t1");
        Thread t2 = new Thread(ticket, "t2");
        Thread t3 = new Thread(ticket, "t3");

        t1.start();
        t2.start();
        t3.start();
    }
}
