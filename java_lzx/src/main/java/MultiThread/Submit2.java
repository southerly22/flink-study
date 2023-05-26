package MultiThread;

import java.util.concurrent.*;

/**
 * @author lzx
 * @date 2023/05/23 15:00
 **/
public class Submit2 {
    public static void main(String[] args) throws ExecutionException, InterruptedException {

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                System.out.println("result");
            }
        };
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.submit(runnable);
    }
}
