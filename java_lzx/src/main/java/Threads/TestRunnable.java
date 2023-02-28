package Threads;

/**
 * @author lzx
 * @date 2023/2/27 13:20
 * @description: TODO 2.创建实现runnable接口的类的实例，并且将实例最为构造参数Target传入到Thread构造函数里面，
 *                  其实该Thread才是真正的线程对象
 *                  3. 调用线程的start方法，启动线程。调用runnable接口实现类的run（）方法
 */
public class TestRunnable {
    public static void main(String[] args) {
        MyRunnable myRunnable = new MyRunnable(); //多个线程 共享类对象，适合相同线程处理同一份资源
        Thread thread1 = new Thread(myRunnable, "长江"); //target = myRunnable
        thread1.start(); // target.run()

        Thread thread2 = new Thread(myRunnable, "鸭绿江");
        thread2.start();

        //主线程
        // for (int i = 0; i < 10; i++) {
        //     if (i % 2 == 0) {
        //         System.out.println("黄河" + i);
        //     }
        // }


        //变形写法 匿名类
        // new Thread(new Runnable() {
        //     @Override
        //     public void run() {
        //         for (int i = 0; i <10; i++) {
        //             System.out.println(Thread.currentThread().getName()+":"+i);
        //         }
        //     }
        // }).start();
    }
}
