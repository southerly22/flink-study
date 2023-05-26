package MultiThread;

import java.util.ArrayList;
import java.util.concurrent.*;

/**
 * @author lzx
 * @date 2023/05/23 15:51
 *  todo FutureTask 示例:用多线程的方式提交计算任务，主线程继续执行其他任务，当主线程需要子线程的计算结果时，再异步获取子线程的执行结果
 **/
public class MyFutureTask implements Callable<Integer> {

    @Override
    public Integer call() throws Exception {
        int res =0;
        for (int i = 0; i < 10; i++) {
            res += i;
        }
        System.out.println("res = " + res);
        Thread.sleep(3000);
        return res;
    }

    public static void main(String[] args) {
        ArrayList<FutureTask<Integer>> arrayList = new ArrayList<>();
        // 底层调用：ThreadPoolExecutor(0, Integer.MAX_VALUE, 60L, TimeUnit.SECONDS, new SynchronousQueue<Runnable>());
        ExecutorService threadPool = Executors.newCachedThreadPool();

        for (int i = 0; i < 5; i++) {
            FutureTask<Integer> futureTask = new FutureTask<>(new MyFutureTask());
            arrayList.add(futureTask);
            threadPool.execute(futureTask); // 线程池提交同一个futureTask 会去重
            threadPool.submit(futureTask);
        }
        threadPool.shutdown();
        System.out.println("running main -----------");

        while (!threadPool.isTerminated()) {

        }
        System.out.println("main running after all task down -----------");

        // 主线程需要 子线程的结果
        int result = 0;
        for (FutureTask<Integer> task : arrayList) {
            try {
                result += task.get();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }

            System.out.println("result " + result);
        }
    }
}
