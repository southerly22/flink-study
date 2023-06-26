package ScheduledExecutor;

import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author lzx
 * @date 2023/06/23 17:52
 **/
public class Test1 {
    public static void main(String[] args) {
        ScheduledThreadPoolExecutor scheduledThreadPoolExecutor = new ScheduledThreadPoolExecutor(3);
        scheduledThreadPoolExecutor.schedule(new MyRunnable(),3, TimeUnit.SECONDS);

    }
}
class MyRunnable implements Runnable{

    @Override
    public void run() {
        System.out.println("scheduledThreadPoolExecutor....");
    }
}
