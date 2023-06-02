package huorong;

import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.fastjson.JSONObject;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
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

    private transient HikariDataSource dataSource; //连接池
    private PhoenixQueryUtil phoenixQueryUtil;

    @Override
    public void open(Configuration parameters) throws Exception {

        Class.forName("org.apache.phoenix.jdbc.PhoenixDriver");
        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.addDataSourceProperty(QueryServices.IS_NAMESPACE_MAPPING_ENABLED, "true");
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:phoenix:192.168.1.118:2181");
        config.setMaximumPoolSize(9);
        config.setMinimumIdle(3);
        config.setConnectionTimeout(30000);
        config.setIdleTimeout(600000);
        config.setMaxLifetime(1800000);
        dataSource = new HikariDataSource(config);
        phoenixQueryUtil = new PhoenixQueryUtil(dataSource);
    }

    @Override
    public void close() throws Exception {
        if (dataSource != null) {
            dataSource.close();
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
        // JSONObject jsonObject = JSONObject.parseObject(input.toString());
        // resultFuture.complete(Collections.singletonList(jsonObject));
        System.err.println("数据超时--> "+input);
        // 超时重试
        asyncInvoke(input,resultFuture);
    }
}
