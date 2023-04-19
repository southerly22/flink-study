package Threads;

/**
 * 线程通信
 *
 * @author lzx
 * @date 2023/03/20 21:45
 * 三个线程之间的通信
 **/
public class Demo01 {
    public static void main(String[] args) {
        //todo 三个线程之间通信
        MyTask task = new MyTask();

        new Thread(){
            @Override
            public void run() {
                while (true){
                    try {
                        task.task1();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }.start();


        new Thread(){
            @Override
            public void run() {
                while (true){
                    try {
                        task.task2();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }.start();

        new Thread(){
            @Override
            public void run() {
                try {
                    task.task3();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }
}
class MyTask{
    //todo 标识1：可以执行任务1 ，2：可以执行任务2 ， 3：可以执行任务3
    int flag = 1;
    public synchronized void task1() throws InterruptedException {
        if (flag != 1){
            this.wait(); //当前线程等待
        }
        System.out.println("1，银行信用卡还款业务。。。");
        flag = 2;
        this.notifyAll(); //唤醒所有等待线程
    }

    public synchronized void task2() throws InterruptedException {
        if (flag !=2 ){
            this.wait();
        }
        System.out.println("2.银行储蓄卡自动结算利息业务。。。");
        flag =3;
        this.notifyAll();//唤醒其他线程
    }
    public synchronized void task3() throws InterruptedException {
        if (flag != 3){
            this.wait();
        }
        System.out.println("3.银行卡短信业务。。。");
        flag =3;
        this.notifyAll();
    }
}
