package flink_core.asyncio;

import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.async.ResultFuture;
import org.apache.flink.streaming.api.functions.async.RichAsyncFunction;
import org.apache.kafka.common.protocol.types.Field;

import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

/**
 * @author lzx
 * @date 2023/05/24 14:45
 **/
public class SampleAsyncFunc extends RichAsyncFunction<Integer,String> {
    // 模拟 连接等待时间
    private long[] sleep = {100L, 1000L, 5000L, 2000L, 6000L, 100L};

    @Override
    public void open(Configuration parameters) throws Exception {
        super.open(parameters);
    }

    @Override
    public void close() throws Exception {
        super.close();
    }

    @Override
    public void timeout(Integer input, ResultFuture<String> resultFuture) throws Exception {
        super.timeout(input, resultFuture);
    }

    //并不是只要实现了asyncInvoke方法就是异步了，这个方法并不是异步的，而是要依靠这个方法里面所写的查询是异步的才可以
    @Override
    public void asyncInvoke(Integer input, ResultFuture<String> resultFuture) throws Exception {
        System.out.println(System.currentTimeMillis()+" - input:"+input+" will sleep "+sleep[input]+"ms");

        //query(input,resultFuture);
        async_query(input,resultFuture);
    }

    // 同步方法
    public void query(final Integer input,final ResultFuture<String> resultFuture){
        try {
            Thread.sleep(sleep[input]);
            resultFuture.complete(Collections.singletonList(String.valueOf(input)));
        }catch (Exception e){
            e.printStackTrace();
            resultFuture.complete(new ArrayList<>(0));
        }
    }

    // 异步查询方法
    public void async_query(final Integer input,ResultFuture<String> resultFuture){
        CompletableFuture.supplyAsync(new Supplier<Integer>() {
            @Override
            public Integer get() {
                try {
                    Thread.sleep(sleep[input]);
                    return input;
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    return null;
                }
            }
        }).thenAccept((Integer res)->{
            resultFuture.complete(Collections.singletonList(String.valueOf(res)));
        });
    }
}
