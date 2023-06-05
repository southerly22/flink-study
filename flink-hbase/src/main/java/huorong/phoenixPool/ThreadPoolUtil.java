package huorong.phoenixPool;

import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 单例线程池工具类
 *
 * @author lzx
 * @date 2023/06/04 23:04
 **/
public class ThreadPoolUtil {

    private static ThreadPoolExecutor threadPoolExecutor = null;
    private static int corePoolSize;

    public ThreadPoolUtil() {
    }

    public ThreadPoolUtil(int corePoolSize) {
        this.corePoolSize = corePoolSize;
    }

    public static ThreadPoolExecutor getThreadPoolExecutor() {
        if (threadPoolExecutor == null) {
            synchronized (ThreadPoolUtil.class) {
                if (threadPoolExecutor == null) {
                    threadPoolExecutor = new ThreadPoolExecutor(corePoolSize,
                            20,
                            100,
                            TimeUnit.SECONDS,
                            new LinkedBlockingDeque<>());
                }
            }

        }
        return threadPoolExecutor;
    }
}
