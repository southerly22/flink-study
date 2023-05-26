package MultiThread;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

/**
 * @author lzx
 * @date 2023/05/25 13:59
 **/
public class FutureTask3 {
    public static void main(String[] args) throws ExecutionException, InterruptedException {
        FutureTask<String> futureTask = new FutureTask<>(() -> {
            System.out.println("come in");
            try {
                TimeUnit.SECONDS.sleep(5);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return "task over";
        });

        Thread t1 = new Thread(futureTask, "t1");
        t1.start();

        //System.out.println("futureTask.get() = " + futureTask.get()); //极易引起阻塞
        System.out.println(Thread.currentThread().getName()+"主线程");

        while (true){
            if (futureTask.isDone()) {
                System.out.println(futureTask.get());
                break;
            }else {
                TimeUnit.SECONDS.sleep(1);
                System.out.println("还未完成。。。");
            }
        }

    }
}
