package ScheduledExecutor;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

/**
 * @author lzx
 * @date 2023/06/23 16:28
 * 延迟且循环执行任务的线程池
 * 以上一次任务执行结束时间 加上任务时间间隔作为下一次任务的开始时间
 **/
public class ScheduleWithFixedDelayTest {

    public static void main(String[] args) {
        // 1.获取一个具备延迟且循环执行任务的线程池对象
        ScheduledExecutorService es = Executors.newScheduledThreadPool(3, new ThreadFactory() {
            int n = 1;

            @Override
            public Thread newThread(Runnable r) {
                return new Thread(r, "自定义线程名：" + n++);
            }
        });


        //2.创建多个任务对象，提交任务，每个任务延迟2秒执行
        es.scheduleWithFixedDelay(
                new MyRunnable3(1),
                1,
                2,
                TimeUnit.SECONDS
        );
        System.out.println("over!");
    }
}

@Slf4j
class MyRunnable3 implements Runnable{
    private final static SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private int id;

    public MyRunnable3(int id) {
        this.id = id;
    }

    @Override
    public void run() {
        String name = Thread.currentThread().getName();
        try {
            log.info("开始执行。。。time{}",format.format(new Date()));
            // 任务执行时间超过 period 任务时间间隔，那么任务时间间隔参数将无效，任务会不停的循环执行

            Thread.sleep(3000);
            log.info("执行结束。。。time{}",format.format(new Date()));

        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println(name + "执行了任务：" + id);
    }
}
