package MultiThread;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 线程池
 *
 * @author lzx
 * @date 2023/03/23 19:37
 **/
public class ThreadPoolTest {
    public static void main(String[] args) {
        ThreadPoolExecutor executor = new ThreadPoolExecutor(
                2,
                4,
                10,
                TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(2),
                new ThreadPoolExecutor.AbortPolicy()
        );
        executor.execute(()->{
            System.out.println("hello");
        });
    }
}
