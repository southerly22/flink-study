package huorong;

import com.alibaba.fastjson.JSONObject;
import com.zaxxer.hikari.HikariDataSource;
import huorong.phoenixPool.HikariPoolUtil;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.async.ResultFuture;
import org.apache.flink.streaming.api.functions.async.RichAsyncFunction;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collections;
import java.util.concurrent.CompletableFuture;

/**
 * @author lzx
 * @date 2023/5/26 15:34
 * @description: TODO 异步读取Phoenix
 */
public class HrAsyncPhoenixFunc extends RichAsyncFunction<SampleTaskMappingInfo, JSONObject> {
    private transient HikariDataSource dataSource; //连接池

    @Override
    public void open(Configuration parameters) throws Exception {
        dataSource = HikariPoolUtil.createConn();
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
            JSONObject jsonObject = null;
            try {
                Connection conn = dataSource.getConnection();
                jsonObject = PhoenixQueryUtil.queryPhoenix(conn, "SAMPLE_PAD_SCAN_LATEST", input.getSha1());
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return jsonObject;
        });
        CompletableFuture<JSONObject> infoTask = CompletableFuture.supplyAsync(() -> {
            JSONObject jsonObject = null;
            try {
                Connection conn = dataSource.getConnection();
                jsonObject = PhoenixQueryUtil.queryPhoenix(conn, "SAMPLE_INFO", input.getSha1());
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return jsonObject;
        });
        CompletableFuture<JSONObject> srcTask = CompletableFuture.supplyAsync(() -> {
            JSONObject jsonObject = null;
            try {
                Connection conn = dataSource.getConnection();
                jsonObject = PhoenixQueryUtil.queryPhoenix(conn, "SAMPLE_SRC", input.getSha1());
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return jsonObject;
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
