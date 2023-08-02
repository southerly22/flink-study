package MultiThread;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * CompletableFuture 常用方法：对速度进行选用,谁快用谁 applyToEither、acceptEither、thenRun、thenApply
 *
 * @author lzx
 * @date 2023/05/25 18:12
 **/
public class CompletableFuture_Methods4 {
    public static void main(String[] args) throws InterruptedException {

        CompletableFuture<String> palyA = CompletableFuture.supplyAsync(() -> {
            try {
                TimeUnit.SECONDS.sleep(2);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return "playA";
        });

        CompletableFuture<String> palyB = CompletableFuture.supplyAsync(() -> {
            try {
                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return "playB";
        });
        // 根据速度选择
        palyA.acceptEither(palyB,r->{
            System.out.println(r + " is winner");
        });

        palyA.applyToEither(palyB,f->{
            System.out.println(f+" is winner");
            return f;
        }).join();

        // 合并两个结果
        palyA.thenCombine(palyB,(a,b)->{
            return a.concat("---").concat(b);

        }).join();

        Thread.sleep(2000);
    }
}
