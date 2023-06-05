package huorong.phoenixPool;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.util.Map;

/**
 * redis连接池
 *
 * @author lzx
 * @date 2023/06/05 10:15
 **/
public class JedisPoolUtil {

    private static JedisPool jedisPool;

    private static void initJedisPool() {
        JedisPoolConfig poolConfig = new JedisPoolConfig();
        poolConfig.setMaxTotal(100);
        poolConfig.setMaxIdle(5);
        poolConfig.setMinIdle(5);
        poolConfig.setBlockWhenExhausted(true);
        poolConfig.setMaxWaitMillis(2000);
        poolConfig.setTestOnBorrow(true);
        jedisPool = new JedisPool(poolConfig, "192.168.1.58", 6379,
                10000,"Huorong123",3);
    }

    public static Jedis getJedis() {
        if (jedisPool == null) {
            initJedisPool();
        }
        // 获取Jedis客户端
        return jedisPool.getResource();
    }

    public static void main(String[] args) {
        Jedis jedis = getJedis();
        String pong = jedis.ping();
        System.out.println(pong);

        Map<String, String> map = jedis.hgetAll("AsyncReadRedis");
        map.forEach((k,v)->{
            System.out.println(k+","+v);
        });

        jedis.close();
    }

}

