/**
 * 线程安全
 *
 * @author lzx
 * @date 2023/02/27 23:23
 **/


class Window implements Runnable {
    int tickets = 100;

    @Override
    public void run() {
        while (true) {
            synchronized (this) {
                if (tickets > 0) {
                    System.out.println(Thread.currentThread().getName() + " 窗口售出：" + tickets);
                    tickets--;
                } else {
                    break;
                }
            }
        }
    }
}

public class ThreadSafe {
    public static void main(String[] args) {
        Window w1 = new Window();
        Thread t1 = new Thread(w1, "t1");
        Thread t2 = new Thread(w1, "t2");
        Thread t3 = new Thread(w1, "t3");

        t1.start();
        t2.start();
        t3.start();
    }
}
