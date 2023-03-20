package MultiThread;

import org.apache.commons.lang3.time.StopWatch;

import java.util.ArrayList;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 生产数据
 *
 * @author lzx
 * @date 2023/03/20 22:40
 **/
public class Producer {
    public static void main(String[] args)
    {

    }

    public void createData(){
        ExecutorService pool = Executors.newFixedThreadPool(100);
        final int totalPageNo = 50; //分50各批次
        final int pageSize = 20000; //每页大小是2万条
        StopWatch watch = new StopWatch();
        watch.start();
        final AtomicInteger atomicInt = new AtomicInteger();
        for (int currentPageNo = 0;currentPageNo < totalPageNo;currentPageNo ++){
            final int finalCurrentPageNo = currentPageNo;

            Runnable run = new Runnable() {
                @Override
                public void run() {
                    ArrayList<User> userList = new ArrayList<>();
                    for (int i = 0; i < pageSize; i++) {
                        int id = i + finalCurrentPageNo * pageSize;
                        User user = new User();
                        user.setId(id);
                        user.setName("lzx" + i);
                        userList.add(user);
                    }

                    atomicInt.addAndGet(UserBatchHandle.batchSave(userList,Thread.currentThread().getName()));
                    //入库的数据达到一百万的时候会有个统计
                    if (atomicInt.get() == (totalPageNo * totalPageNo)) {
                        watch.stop();
                        System.out.println("同步数据到db，它已经花费 "+watch.getTime());
                    }
                }
            };
            try {
                Thread.sleep(5);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            pool.execute(run);
        }
    }

}
