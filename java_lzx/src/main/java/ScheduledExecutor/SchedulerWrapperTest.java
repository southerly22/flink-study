package ScheduledExecutor;

import java.util.concurrent.*;

/**
 * @author lzx
 * @date 2023/10/26 22:11
 **/
public class SchedulerWrapperTest {

    public static void main(String[] args) throws InterruptedException {
        ScheduledExecutorService threadPool = Executors.newScheduledThreadPool(3);
        CountDownLatch countDownLatch = new CountDownLatch(1);

        threadPool.scheduleWithFixedDelay(
                new RunnableWrapper(new MyRunnable(countDownLatch),"lzxRunnable"),
                3,
                3,
                TimeUnit.SECONDS
        );
        countDownLatch.await();
        // 关闭线程池
        threadPool.shutdown();
    }


    static class MyRunnable implements Runnable{
        private CountDownLatch countDownLatch;

        public MyRunnable(CountDownLatch countDownLatch) {
            this.countDownLatch = countDownLatch;
        }

        @Override
        public void run() {
            System.out.printf("我的MyRunnable");
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            countDownLatch.countDown();
        }
    }
}
