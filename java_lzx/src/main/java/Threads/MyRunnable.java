package Threads;

/**
 * @author lzx
 * @date 2023/2/27 13:16
 * @description: TODO 创建线程方法2：实现 Runnable接口 因为java的类具有单继承的特性，当无法继承类的时候，需要实现接口来创建线程
 *                  1.实现runnable接口
 *                  2.重写run方法
 */
public class MyRunnable implements Runnable {
    @Override
    public void run() {
        for (int i = 0; i < 100; i++) {
            if (i%2==0){
                System.out.println(Thread.currentThread().getName()+ "," +i);
            }
        }
    }
}
