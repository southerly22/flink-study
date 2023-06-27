package MultiThread;


import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * @author lzx
 * @date 2023/05/25 15:27
 **/
public class CompletableFuture4 {
    public static void main(String[] args) throws InterruptedException {

        System.out.println(System.currentTimeMillis() / 1000);
        CompletableFuture<String> f1 = CompletableFuture.supplyAsync(() -> {

            try {
                TimeUnit.SECONDS.sleep(2);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return "T2:洗茶杯";
        });

        //任务2：洗茶壶->洗茶杯-->拿茶叶
        CompletableFuture<String> f2 = CompletableFuture.supplyAsync(() -> {
            try {
                TimeUnit.SECONDS.sleep(2);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return "T2:洗茶壶";
        });


        CompletableFuture<String> f3 = CompletableFuture.supplyAsync(() -> {

            try {
                TimeUnit.SECONDS.sleep(4);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return "T2:拿茶叶";
        });




        // todo 2 join 会阻塞main 线程
        for (int i = 0; i < 10; i++) {
            TimeUnit.MILLISECONDS.sleep(500);
            System.out.println("main" + i);

            CompletableFuture<Void> allOf = CompletableFuture.allOf(f1, f2, f3);

            CompletableFuture<String> thenApply = allOf.thenApply(t -> {
                System.out.println(System.currentTimeMillis() / 1000);
                return f1.join().concat(f2.join()).concat(f3.join());
            });

            System.out.println(thenApply.join());

        }

        // todo 1 线程切换（自动的）
        //for (int i = 0; i < 10; i++) {
        //    TimeUnit.MILLISECONDS.sleep(500);
        //    System.out.println("main" + i);
        //
        //    CompletableFuture<String> combine = f1.thenCombine(f2, (str1, str2) -> {
        //        //System.out.println(str1.concat("---").concat(str2));
        //        return str1.concat("---").concat(str2);
        //    });
        //
        //    CompletableFuture<String> combine1 = combine.thenCombine(f3, (str2, str3) -> {
        //        //System.out.println(str2.concat("---").concat(str3));
        //        System.out.println(System.currentTimeMillis() / 1000);
        //        return str2.concat("---").concat(str3);
        //    });
        //
        //    combine1.thenAccept((String res)->{
        //        System.out.println(res);
        //    });
        //    //System.out.println(System.currentTimeMillis() / 1000);
        //}






    }
}
