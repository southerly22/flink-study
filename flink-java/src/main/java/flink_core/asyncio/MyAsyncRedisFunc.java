package flink_core.asyncio;

import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.async.ResultFuture;
import org.apache.flink.streaming.api.functions.async.RichAsyncFunction;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.util.Collections;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

public class MyAsyncRedisFunc extends RichAsyncFunction<String, String> {

    private transient Jedis jedis;

    @Override
    public void open(Configuration parameters) throws Exception {
        JedisPoolConfig config = new JedisPoolConfig();
        config.setLifo(true); // 链接 fifo
        config.setMaxTotal(10);
        config.setMaxIdle(10); //设置最大空闲链接
        config.setMinIdle(0);//设置最大空闲链接

        JedisPool pool = new JedisPool(config, "192.168.1.58", 6379, 2000, "Huorong123", 3);
        jedis = pool.getResource();
    }

    @Override
    public void close() throws Exception {
        jedis.close();
    }

    // 处理异步请求超时的方法，（默认超时会抛出异常，程序停止）
    @Override
    public void timeout(String input, ResultFuture<String> resultFuture) throws Exception {
        //System.out.println("请求超时》》》" + input);
        super.timeout(input,resultFuture);
    }

    // 异步请求方法： input：输入数据  resultFuture：异步请求的结果对象
    @Override
    public void asyncInvoke(String input, ResultFuture<String> resultFuture) throws Exception {

        CompletableFuture.supplyAsync(new Supplier<String>() {
            @Override
            public String get() {
                String result = jedis.hget("AsyncReadRedis", input);
                try {
                    if ("hangzhou".equals(result)) Thread.sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                return result;
            }
        }).thenAccept((String result) -> {
            // 异步回调返回
            resultFuture.complete(Collections.singletonList(result));
        });
    }
}
