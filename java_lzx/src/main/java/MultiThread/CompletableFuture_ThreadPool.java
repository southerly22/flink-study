package MultiThread;

import java.util.concurrent.*;

/**
 * CompletableFuture 和 ThreadPool 线程池
 *
 * @author lzx
 * @date 2023/05/25 18:12
 **/
public class CompletableFuture_ThreadPool {
    public static void main(String[] args) {
        System.out.println(Runtime.getRuntime().availableProcessors());
        ExecutorService threadPool = Executors.newFixedThreadPool(3);
        try {
            CompletableFuture.supplyAsync(() -> {
                try {TimeUnit.MILLISECONDS.sleep(20);} catch (InterruptedException e) {e.printStackTrace();}
                System.out.println("1号任务" + Thread.currentThread().getName());
                return "playA";
            },threadPool).thenRunAsync(()->{
                try {TimeUnit.MILLISECONDS.sleep(20);} catch (InterruptedException e) {e.printStackTrace();}
                System.out.println("2号任务" +Thread.currentThread().getName());
            }).thenRun(()->{
                try {TimeUnit.MILLISECONDS.sleep(10);} catch (InterruptedException e) {e.printStackTrace();}
                System.out.println("3号任务" +Thread.currentThread().getName());
            }).join();
        } finally {
            threadPool.shutdown();
        }
    }
}
