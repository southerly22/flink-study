package Threads;

/**
 * @author lzx
 * @date 2023/2/27 11:35
 * @description: TODO
 */
public class TestMyThread {
    public static void main(String[] args) {

        //创建自定义线程
        MyThread t1 = new MyThread("子线程1");
        t1.start();

        MyThread t2 = new MyThread("子线程2");
        t2.start();

        //main线程
        for (int i = 0; i <100; i++) {
            if (i % 2 == 1){
                System.out.println(i);
            }
        }
    }
}
