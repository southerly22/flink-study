package flink_core.asyncio;

import org.apache.flink.streaming.api.datastream.AsyncDataStream;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

/**
 * Redis异步IO
 *
 * @author lzx
 * @date 2023/05/22 16:33
 **/
public class RedisAsync {
    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        DataStreamSource<String> source = env.fromCollection(Arrays.asList("1", "2", "3", "4", "5", "6", "7", "8", "9"));
        int capacity = 100;

        AsyncDataStream.unorderedWait(
                source,
                new MyAsyncRedisFunc(), //自定义异步IO类
                2000, // 超时时间
                TimeUnit.MILLISECONDS, // 超时时间的单位
                capacity // 最大异步并发请求数量 默认100
        ).print();

        env.execute("redisAsync");
    }
}
