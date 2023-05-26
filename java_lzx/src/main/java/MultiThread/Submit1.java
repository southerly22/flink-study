package MultiThread;

import java.util.concurrent.*;

/**
 * @author lzx
 * @date 2023/05/23 15:00
 **/
public class Submit1 {
    public static void main(String[] args) throws ExecutionException, InterruptedException {

        Callable<String> callable = new Callable<String>() {
            @Override
            public String call() throws Exception {
                return "result";
            }
        };

        ExecutorService executor = Executors.newSingleThreadExecutor();
        Future<String> future = executor.submit(callable);
        String s = future.get();
        System.out.println(s);
    }
}
