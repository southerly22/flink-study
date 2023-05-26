package MultiThread;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * CompletableFuture 常用方法：获得结果 和 触发计算：get join getNow complete
 *
 * @author lzx
 * @date 2023/05/25 18:12
 **/
public class CompletableFuture_Methods1 {
    public static void main(String[] args) {
        CompletableFuture<String> completableFuture = CompletableFuture.supplyAsync(() -> {
            try {
                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return "abc";
        });

        //completableFuture.join();  等于get方法，join不用处理异常

        //try {Thread.sleep(2000);} catch (InterruptedException e) {e.printStackTrace();}
        //System.out.println(completableFuture.getNow("aaa")); // 方法执行时获取异步io的值，获取到返回abc。获取不到返回aaa

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        //如果尚未完成，则将 {@link get（）} 和相关方法返回的值设置为给定值
        // complete(打断) :执行时获取不到异步IO的值，返回true，把aaa传入，此后join、get获取的值就是aaa了
        System.out.println(completableFuture.complete("aaa") + "\t" + completableFuture.join());
    }
}
