package Threads;

/**
 * @author lzx
 * @date 2023/2/27 14:29
 * @description: TODO 守护线程特点：所有非守护线程死亡后，守护线程自动死亡。
 *                      守护线程必须在线程启动之前设置，否则会报异常
 */
public class MyDaemon extends Thread {
    @Override
    public void run() {
        while (true){
            System.out.println("守护线程。。。");
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
