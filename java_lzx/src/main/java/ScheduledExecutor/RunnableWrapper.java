package ScheduledExecutor;

import lombok.extern.slf4j.Slf4j;
import org.mortbay.log.Log;

/**
 * @author lzx
 * @date 2023/10/26 21:57
 **/
// 自定义线程的包装类
@Slf4j
public class RunnableWrapper implements Runnable {

    // 实际要执行的线程任务
    private Runnable task;
    // 线程任务被创建出来的时间
    private long createTime;
    // 线程任务被线程池运行的开始时间
    private long startTime;
    // 线程任务被线程池运行的结束时间
    private long endTime;
    // 线程信息
    private String taskInfo;

    private boolean showWaitLog = true;

    private long durMs = 1000L;

    //当这个任务被创建出来的时候，就会设置他的创建时间
    //但是接下来这个任务可能会提交到线程池，会进入线程池排队
    public RunnableWrapper(Runnable task, String taskInfo) {
        this.task = task;
        this.taskInfo = taskInfo;
        this.createTime = System.currentTimeMillis();
    }

    public void setShowWaitLog(boolean showWaitLog){
        this.showWaitLog = showWaitLog;
    }

    public void setDurMs(long durMs){
        this.durMs = durMs;
    }

    /**
     * 当任务在线程池排队时 run方法是不会运行的
     * 当结束排队后 才会运行
     */
    @Override
    public void run() {
        this.startTime = System.currentTimeMillis();

        //startTime-createTime 就是排队时间
        if (showWaitLog){
            log.warn("任务信息：[{}],任务排队时间[{}]ms",taskInfo,startTime-createTime);
        }

        // 调用实际任务的run方法
        try {
            task.run();
        }catch (Exception e){
            e.printStackTrace();
        }
        // 任务结束时间
        if (endTime - startTime > durMs){
            log.warn("任务信息：[{}],任务运行时间[{}]ms",taskInfo,endTime - startTime);
        }
    }
}
