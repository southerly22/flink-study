package MultiThread;

import org.joda.time.IllegalFieldValueException;

import java.util.concurrent.*;
import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * @author lzx
 * @date 2023/05/25 15:27
 **/
public class CompletableFuture2 {
    public static void main(String[] args) {

        CompletableFuture<Integer> completableFuture = CompletableFuture.supplyAsync(() -> {
            System.out.println(Thread.currentThread().getName() + " come in");
            int res = ThreadLocalRandom.current().nextInt(10);
            try {
                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (res > 4) {
                int i = 10 / 0;
            }
            System.out.println("1s 后出结果");
            return res;
        }).whenComplete((res, e) -> {  //result exception
            if (e == null) {
                System.out.println("计算结果为:" + res);
            }
        }).exceptionally(e -> {
            e.printStackTrace();
            System.out.println("异常情况" + e.getMessage());
            return null;
        });

        System.out.println(Thread.currentThread().getName() + "main 线程先去忙其他任务");

        // 休眠一段时间 不然异步线程很快就关闭了
        try {
            TimeUnit.SECONDS.sleep(2);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }
}
