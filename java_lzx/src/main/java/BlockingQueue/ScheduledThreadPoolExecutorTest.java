package BlockingQueue;

import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author lzx
 * @date 2023/6/26 13:45
 * @description: TODO
 */
public class ScheduledThreadPoolExecutorTest {
    public static void main(String[] args) {
        ScheduledThreadPoolExecutor scheduledThreadPoolExecutor = new ScheduledThreadPoolExecutor(2);

        Runnable runnable = () -> {
            System.out.println(System.currentTimeMillis());
        };

        // scheduledThreadPoolExecutor.scheduleAtFixedRate(
        //         runnable,2000,1000, TimeUnit.MILLISECONDS
        // );
        scheduledThreadPoolExecutor.scheduleWithFixedDelay(
                runnable,2000,5000, TimeUnit.MILLISECONDS
        );
        // long nanos = TimeUnit.MILLISECONDS.toNanos(2000);

        // long now = TimeUnit.MILLISECONDS.toNanos(System.currentTimeMillis());

        // System.out.println(nanos);
        // scheduledThreadPoolExecutor.scheduleWithFixedDelay();

        // System.out.println(Long.MAX_VALUE >> 1);
        // System.out.println(Long.MAX_VALUE);
    }
}
