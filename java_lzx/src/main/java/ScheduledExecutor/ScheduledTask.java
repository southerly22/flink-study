package ScheduledExecutor;

import java.time.temporal.ValueRange;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author lzx
 * @date 2023/06/24 15:36
 **/
public class ScheduledTask {
    private static final AtomicInteger count = new AtomicInteger(0);

    private static final ScheduledThreadPoolExecutor scheduler = new ScheduledThreadPoolExecutor(1, new ThreadFactory() {
        @Override
        public Thread newThread(Runnable r) {
            Thread thread = new Thread(Thread.currentThread().getThreadGroup(), r, "sc-tak");
            thread.setDaemon(true);
            return thread;
        }
    });

    public static void main(String[] args) throws InterruptedException {

        CountDownLatch latch = new CountDownLatch(1);
        scheduler.scheduleWithFixedDelay(
                () -> {
                    System.out.println("start scheduler " + count.get());
                    try {
                        if (count.get() == 5) {
                            throw new IllegalArgumentException("my Exception");
                        }
                    } catch (Exception e) { //将异常自己抛出，避免抛到框架内导致周期任务卡住
                        System.out.println("e = " + e);
                    }

                    count.incrementAndGet();
                }, 0, 1, TimeUnit.SECONDS
        );
        latch.await();
    }
}
