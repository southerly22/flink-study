package huorong;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * @author lzx
 * @date 2023/5/26 17:54
 * @description: TODO
 */
public class Test {
    public static void main(String[] args) throws InterruptedException {

        CompletableFuture<String> A = CompletableFuture.supplyAsync(() -> {
            System.out.println("\"playA\" = " + "playA");
            try {
                TimeUnit.SECONDS.sleep(2);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return "playA";
        });

        CompletableFuture<String> B = CompletableFuture.supplyAsync(() -> {
            System.out.println("\"playB\" = " + "playB");
            try {
                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return "playB";
        });

        CompletableFuture<String> C = CompletableFuture.supplyAsync(() -> {
            System.out.println("\"playC\" = " + "playC");
            try {
                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return "playC";
        });

        A.thenCombine(B,(a,b)->{
            return a.concat(",").concat(b);
        }).thenCombine(C,(ab,c)->{
            return ab.concat(",").concat(c);
        }).thenAccept(res->{
            System.out.println("res = " + res);
        });


        // CompletableFuture.supplyAsync(() -> {
        //     System.out.println("\"playA\" = " + "playA");
        //     try {
        //         TimeUnit.SECONDS.sleep(2);
        //     } catch (InterruptedException e) {
        //         e.printStackTrace();
        //     }
        //     return "playA";
        // }).thenCombine(CompletableFuture.supplyAsync(()->{
        //     System.out.println("\"playB\" = " + "playB");
        //     try {
        //         TimeUnit.SECONDS.sleep(1);
        //     } catch (InterruptedException e) {
        //         e.printStackTrace();
        //     }
        //     return "playB";
        // }),(a,b)->{
        //     return a.concat(b);
        // }).thenCombine(CompletableFuture.supplyAsync(()->{
        //     System.out.println("\"playC\" = " + "playC");
        //     try {
        //         TimeUnit.SECONDS.sleep(1);
        //     } catch (InterruptedException e) {
        //         e.printStackTrace();
        //     }
        //     return "playC";
        // }),(ab,c)->{
        //     return ab.concat(c);
        // }).thenAccept(res->{
        //     System.out.println(res);
        // });


        TimeUnit.SECONDS.sleep(2);

        // StringBuffer buffer = new StringBuffer();
        // buffer.append("张三").append(",");
        // buffer.append("李四").append(",");
        // buffer.append("王二").append(",");
        //
        // System.out.println(buffer.toString());
        // System.out.println(buffer.deleteCharAt(buffer.length() - 1));
        // // jSONObject.put("src_list",buffer.toString());
    }
}
