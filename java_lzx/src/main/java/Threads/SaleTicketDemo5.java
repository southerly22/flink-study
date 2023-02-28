package Threads;

/**
 * @author lzx
 * @date 2023/2/28 16:37
 * @description: TODO
 */

class SaleTickets{
    private int tickets = 100;
    public void sale(){
        if (tickets>0){
            System.out.println(Thread.currentThread().getName()+"售出："+tickets);
            tickets--;
        }else {
            throw new RuntimeException("无票了...");
        }
    }
    public int getTickets(){
        return tickets;
    }
}

public class SaleTicketDemo5 {
    public static void main(String[] args) {
        // 1.创建资源对象
        SaleTickets saleTickets = new SaleTickets();

        // 2.启动多个线程操作资源类的对象
        Thread t1 = new Thread("t1"){
            @Override
            public void run() {
                while (true){
                    synchronized (saleTickets){
                        saleTickets.sale();
                    }
                }
            }
        };
        Thread t2 = new Thread("t2"){
            @Override
            public void run() {
                while (true){
                    synchronized (saleTickets){
                        saleTickets.sale();
                    }
                }
            }
        };
        Thread t3 = new Thread("t3"){
            @Override
            public void run() {
                while (true){
                    synchronized (saleTickets){
                        saleTickets.sale();
                    }
                }
            }
        };
        t1.start();
        t2.start();
        t3.start();
    }
}
