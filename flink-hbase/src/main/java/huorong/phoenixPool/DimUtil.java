package huorong.phoenixPool;

import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.druid.pool.DruidPooledConnection;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.time.StopWatch;
import redis.clients.jedis.Jedis;

import java.lang.reflect.InvocationTargetException;
import java.security.Key;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

/**
 * 维度表工具类：先去redis里面获取，获取不到去Phoenix获取，将值更新到redis
 *
 * @author lzx
 * @date 2023/06/05 10:09
 **/
public class DimUtil {

    public static JSONObject getDimInfo(Connection connection, String tableName, String key) throws SQLException, InvocationTargetException, InstantiationException, IllegalAccessException {

        // todo 先去redis里 获取值
        Jedis jedis = JedisPoolUtil.getJedis();
        // 拼接redis key
        String redisKey = tableName + "-" + key;
        String dimInfo = jedis.get(redisKey);
        if (dimInfo != null) {
            // 重置过期时间
            jedis.expire(redisKey,6 * 60 * 60);
            //归还链接
            jedis.close();
            //返回数据
            return JSONObject.parseObject(dimInfo);
        }else{ // todo 获取不到的 去Phoenix里面获取

            //组合SQL语句
            String sql = "SELECT * FROM OFFICIAL." + tableName + " WHERE \"rk\" like '" + key + "%'";
            //查询
            List<JSONObject> queryList = JdbcUtil.queryList(connection, sql, JSONObject.class, false);
            JSONObject jsonObj = queryList.get(0);

            // 将取到数据写入redis
            jedis.set(redisKey,jsonObj.toJSONString());

            //设置过期时间
            jedis.expire(redisKey,6 * 60 * 60);

            jedis.close();
            return jsonObj;
        }
    }

    public static void delDimInfo(String tableName,String key){
        Jedis jedis = JedisPoolUtil.getJedis();
        jedis.del(tableName.concat("-").concat(key));
        jedis.close();
    }

    public static void main(String[] args) throws SQLException, InvocationTargetException, InstantiationException, IllegalAccessException {
        String tableName = "SAMPLE_PAD_SCAN_LATEST";
        String key="000000015acd38b8f06d639438ac442493d08180";
        DruidDataSource dataSource = DruidPoolUtil.createConn();
        DruidPooledConnection conn = dataSource.getConnection();
        StopWatch watch = new StopWatch();
        watch.start();
        JSONObject dimInfo = getDimInfo(conn, tableName, key);
        watch.stop();
        conn.close();
        System.out.println(watch.getTime());
        System.out.println(dimInfo);
    }
}
