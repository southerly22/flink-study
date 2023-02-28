package Threads;

/**
 * @author lzx
 * @date 2023/2/27 13:47
 * @description: TODO 声明一个匿名内部类继承Thread类，重写run方法，实现打印[1,100]之间的奇数，
 *                  当打印到5时，让奇数线程暂停一下，再继续。
 *                 当打印到5时，让奇数线程停下来，让偶数线程执行完再打印。
 */
public class TestThreadStateChange {
    public static void main(String[] args) {
        //打印偶数
        Thread t1 = new Thread("t1") {
            @Override
            public void run() {
                for (int i = 0; i < 101; i++) {
                    if (i % 2 == 0) {
                        System.out.println(getName()+" 偶数线程,"+i);
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        };

        //打印奇数
        Thread t2 = new Thread("t2") {
            @Override
            public void run() {
                for (int i = 0; i < 101; i++) {
                    if (i % 2 == 1) {
                        System.out.println(getName() + " 奇数线程," + i);
                    }
                    if (i==5){
                        Thread.yield(); //暂停t2
                        try {
                            t1.join();//等待t1线程 执行完 在执行t2
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        };

        t1.start();
        t2.start();


    }
}
