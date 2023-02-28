package Threads;

/**
 * @author lzx
 * @date 2023/2/27 13:41
 * @description: TODO
 */
public class TestPriority {
    public static void main(String[] args) {
        int i =0;
        Thread t1 = new Thread("t1"){
            @Override
            public void run() {
                System.out.println(getName()+"优先级"+getPriority());
                System.out.println("啦啦啦");
            }
        };
        t1.setPriority(Thread.MAX_PRIORITY);
        t1.start();

        System.out.println("main线程优先级：" + Thread.currentThread().getPriority());
    }
}
