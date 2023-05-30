package huorong;

import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.fastjson.JSONObject;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.async.ResultFuture;
import org.apache.flink.streaming.api.functions.async.RichAsyncFunction;
import org.apache.phoenix.query.QueryServices;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Collections;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;

/**
 * @author lzx
 * @date 2023/5/26 15:34
 * @description: TODO 异步读取Phoenix
 */
public class HrAsyncPhoenixFunc extends RichAsyncFunction<SampleTaskMappingInfo, JSONObject> {

    //private transient DruidDataSource dataSource; //连接池
    private transient Connection conn;
    private PhoenixQueryUtil2 phoenixQueryUtil;

    @Override
    public void open(Configuration parameters) throws Exception {

        Properties prop = new Properties();
        prop.setProperty(QueryServices.IS_NAMESPACE_MAPPING_ENABLED, "true");
        prop.setProperty(QueryServices.AUTO_COMMIT_ATTRIB, "false");
        prop.setProperty(QueryServices.ZOOKEEPER_QUORUM_ATTRIB, "192.168.1.118,192.168.1.119,192.168.1.120:2181");
        prop.setProperty(QueryServices.KEEP_ALIVE_MS_ATTRIB, "600000");
        Class.forName("org.apache.phoenix.jdbc.PhoenixDriver");
        conn = DriverManager.getConnection("jdbc:phoenix:192.168.1.118:2181", prop);

        phoenixQueryUtil = new PhoenixQueryUtil2(conn);
    }

    @Override
    public void close() throws Exception {
        if (conn != null) {
            conn.close();
        }
    }

    // 异步IO处理
    @Override
    public void asyncInvoke(SampleTaskMappingInfo input, ResultFuture<JSONObject> resultFuture) throws Exception {
        CompletableFuture<JSONObject> padTask = CompletableFuture.supplyAsync(() -> {
            return phoenixQueryUtil.queryPhoenixPad(input.getSha1());
        });
        CompletableFuture<JSONObject> infoTask = CompletableFuture.supplyAsync(() -> {
            return phoenixQueryUtil.queryPhoenixInfo(input.getSha1());
        });
        CompletableFuture<JSONObject> srcTask = CompletableFuture.supplyAsync(() -> {
            return phoenixQueryUtil.queryPhoenixSrc(input.getSha1());
        });
        padTask.thenCombine(infoTask, (pad, info) -> {
            for (String key : pad.keySet()) {
                info.put(key, pad.get(key));
            }
            return info;
        }).thenCombine(srcTask, (info, src) -> {
            for (String key : src.keySet()) {
                info.put(key, src.get(key));
            }
            return info;
        }).thenAccept((JSONObject res) -> {
            resultFuture.complete(Collections.singletonList(res));
        });

    }

    @Override
    public void timeout(SampleTaskMappingInfo input, ResultFuture<JSONObject> resultFuture) throws Exception {
        System.out.println("超时，出错数据为====》 " + input.toString());
        super.timeout(input, resultFuture);
    }
}
