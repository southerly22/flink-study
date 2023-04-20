package flink_core.sink;

import com.alibaba.fastjson.JSON;
import flink_core.source.EventLog;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.CheckpointingMode;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.connectors.redis.RedisSink;
import org.apache.flink.streaming.connectors.redis.common.config.FlinkJedisPoolConfig;
import org.apache.flink.streaming.connectors.redis.common.mapper.RedisCommand;
import org.apache.flink.streaming.connectors.redis.common.mapper.RedisCommandDescription;
import org.apache.flink.streaming.connectors.redis.common.mapper.RedisMapper;

import java.util.Optional;

/**
 * @author lzx
 * @date 2023/4/20 15:58
 * @description: TODO
 */
public class RedisSinkOperator {
    public static void main(String[] args) throws Exception {
        Configuration configuration = new Configuration();
        configuration.setInteger("rest.port",8085);
        StreamExecutionEnvironment env = StreamExecutionEnvironment.createLocalEnvironmentWithWebUI(configuration);

        //开启ck
        env.enableCheckpointing(5000, CheckpointingMode.EXACTLY_ONCE);
        env.getCheckpointConfig().setCheckpointStorage("file:///D:\\WorkPlace\\flink-study\\flink-java\\ck\\");

        //构造 输入流
        DataStreamSource<EventLog> streamSource = env.addSource(new LzxSourceFunction());

        //eventLog采取什么结构来储存
        FlinkJedisPoolConfig config = new FlinkJedisPoolConfig.Builder().setHost("192.168.1.58")
                .setDatabase(0)
                .setPassword("Huorong123")
                .build();

        // string数据
        // RedisSink<EventLog> redisSink = new RedisSink<>(config, new StringInsertMapper());

        // HASH 数据
        RedisSink<EventLog> redisSink = new RedisSink<>(config,new HSetInsertMapper());

        //构造 输出流
        streamSource.addSink(redisSink);

        env.execute("RedisSinkOperator");
    }

    // string 结构插入
    static class StringInsertMapper implements RedisMapper<EventLog>{

        @Override
        public RedisCommandDescription getCommandDescription() { //数据类型
            return new RedisCommandDescription(RedisCommand.SET); //存储String 类型
        }
        /***
         * @Author: lzx
         * @Description: SET存储的是String类型数据，选择的是没有内部Key的redis数据结构，下面的方法返回的是大Key
         **/
        @Override
        public String getKeyFromData(EventLog eventLog) {
            return eventLog.getGuid()+"-"+eventLog.getSessionId()+"-"+eventLog.getTimeStamp(); //大key
        }

        @Override
        public String getValueFromData(EventLog eventLog) {
            return JSON.toJSONString(eventLog); // value
        }
    }

    /***
     * @Author: lzx
     * @Description: HASH 结构数据插入 选择额外的key（hash这种结构，他有额外的 key）
     **/
    static class HSetInsertMapper implements RedisMapper<EventLog>{

        // 可以根据具体数据， 选择额外key（就是hash这种结构，它有额外key（大key）
        @Override
        public Optional<String> getAdditionalKey(EventLog data) {
            return RedisMapper.super.getAdditionalKey(data);
        }

        // 可以设置具体数据，设置不同的TTL
        @Override
        public Optional<Integer> getAdditionalTTL(EventLog data) {
            return Optional.of(30); // 30s 过期时间
        }

        @Override
        public RedisCommandDescription getCommandDescription() {
            return new RedisCommandDescription(RedisCommand.HSET,"event-log6");
        }
        /***
         * @Author: lzx
         * @Description: 选择的是具有内部key的redis数据结构（hset），则此方法返回的是hset内部的小key
         **/
        @Override
        public String getKeyFromData(EventLog data) {
            return data.getGuid()+"-"+data.getSessionId()+"-"+data.getTimeStamp();
        }

        @Override
        public String getValueFromData(EventLog data) {
            return data.getEventId();
        }
    }
}
