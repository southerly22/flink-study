package MultiThread;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * @author lzx
 * @date 2023/05/23 15:00
 **/
public class Submit3 {
    public static void main(String[] args) throws ExecutionException, InterruptedException {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Data data = new Data();
        Future<Data> future = executor.submit(new MyRunnable(data), data);
        String name = future.get().getName();
        System.out.println(name);
        executor.shutdown();
    }
}
class Data{
    String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
class MyRunnable implements Runnable{

    Data data;

    public MyRunnable(Data data) {
        this.data = data;
    }

    @Override
    public void run() {
        data.setName("小明");
    }
}