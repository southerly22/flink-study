package MultiThread;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * CompletableFuture 常用方法：对计算结果进行处理 thenApply、handle
 *
 * @author lzx
 * @date 2023/05/25 18:12
 **/
public class CompletableFuture_Methods2 {
    public static void main(String[] args) {
        ExecutorService threadPool = Executors.newFixedThreadPool(3);

        //CompletableFuture.supplyAsync(()->{
        //    System.out.println("aaa");
        //    return 1;
        //},threadPool).thenApply(f->{
        //    int i = 10 / 0;  // 有异常中断程序
        //    System.out.println("bbb");
        //   return f + 2;
        //}).thenApply(f->{
        //    System.out.println("ccc");
        //    return f + 3;
        //}).whenComplete((res,e)->{
        //    if (e==null) {
        //        System.out.println("res = " + res);
        //    }
        //}).exceptionally(e->{
        //    e.printStackTrace();
        //    System.out.println("e.getMessage() = " + e.getMessage());
        //    return null;
        //});

        CompletableFuture.supplyAsync(()->{
            System.out.println("aaa");
            return 1;
        },threadPool).handle((f,e)->{
            int i = 10 / 0; // 有异常 不中断程序
            System.out.println("bbb");
            return f + 2;
        }).handle((f,e)->{
            System.out.println("ccc");
            return f + 3;
        }).whenComplete((res,e)->{
            if (e==null) {
                System.out.println("res = " + res);
            }
        }).exceptionally(e->{
            e.printStackTrace();
            System.out.println("e.getMessage() = " + e.getMessage());
            return null;
        });

        System.out.println(Thread.currentThread().getName()+"主线程run。。。");
        threadPool.shutdown();
    }
}
