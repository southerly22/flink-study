package MultiThread;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * CompletableFuture 常用方法：对计算结果进行消费 thenAccept、thenRun、thenApply
 *
 * @author lzx
 * @date 2023/05/25 18:12
 **/
public class CompletableFuture_Methods3 {
    public static void main(String[] args) {

        CompletableFuture.supplyAsync(()->{
            System.out.println("aaa");
            return 1;
        }).thenApply(f->{
            return f + 2;
        }).thenAccept(res->{
            System.out.println("res = " + res);
        });

        System.out.println(Thread.currentThread().getName()+"主线程run。。。");
    }
}
