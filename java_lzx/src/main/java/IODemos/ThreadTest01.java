package IODemos;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 线程池
 *
 * @author lzx
 * @date 2023/03/22 23:04
 **/
public class ThreadTest01 {
    private static Log logger =  LogFactory.getFactory().getInstance(ThreadTest01.class);

    public static void main(String[] args) {
        int threadNum = 10;
        try {
            ExecutorService threadPool = Executors.newFixedThreadPool(threadNum);
            for (int i = 0; i < threadNum; i++) {
                threadPool.execute(new Mytask("lzx"+i,i));
            }
            threadPool.shutdown();
        } catch (Exception e) {
            e.printStackTrace();
        }

        List<Thread> threads = new ArrayList<>();
        for (Thread thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    static class Mytask implements Runnable{

        private String name;
        private int id;

        public Mytask(String name, int id) {
            this.name = name;
            this.id = id;
        }

        @Override
        public String toString() {
            return "Mytask{" +
                    "name='" + name + '\'' +
                    ", id=" + id +
                    '}';
        }

        @Override
        public void run() {
            String name = Thread.currentThread().getName();
            long id = Thread.currentThread().getId();
            logger.warn("线程名字："+name+"，线程Id： "+ id+","+this.toString());
        }
    }
}
