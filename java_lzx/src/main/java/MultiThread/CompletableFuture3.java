package MultiThread;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

/**
 * @author lzx
 * @date 2023/05/25 15:27
 **/
public class CompletableFuture3 {
    public static void main(String[] args) {

        // 任务1：洗水壶--> 烧开水 （同步的）
        CompletableFuture<Void> f1 = CompletableFuture.runAsync(() -> {
            System.out.println("T1:洗水壶。。。");
            try {
                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            System.out.println("T1:烧开水。。。");
            try {
                TimeUnit.SECONDS.sleep(5);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });

        //任务2：洗茶壶->洗茶杯-->拿茶叶
        CompletableFuture<String> f2 = CompletableFuture.supplyAsync(() -> {
            System.out.println("T2:洗茶壶");
            try {
                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("T2:洗茶杯");
            try {
                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("T2:拿茶叶");
            try {
                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return "龙井";
        });

        //任务3：任务1和2完成后 泡茶
        CompletableFuture<String> f3 = f1.thenCombine(f2, (a, b) -> {
            System.out.println("拿到茶叶" + b);
            System.out.println("泡茶...");
            try {
                TimeUnit.MILLISECONDS.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return "上茶" + b;
        });

        // 等待任务3执行结果
        System.out.println("f3.join() = " + f3.join());

    }
}
