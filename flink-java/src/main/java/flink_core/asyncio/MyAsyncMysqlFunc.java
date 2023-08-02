package flink_core.asyncio;

import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.druid.pool.DruidPooledConnection;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.async.ResultFuture;
import org.apache.flink.streaming.api.functions.async.RichAsyncFunction;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.concurrent.*;
import java.util.function.Supplier;

/**
 * mysqlAsync方法
 *
 * @author lzx
 * @date 2023/05/23 11:35
 **/
public class MyAsyncMysqlFunc extends RichAsyncFunction<String, Tuple2<String, String>> {
    private transient DruidDataSource druidDataSource;
    private transient ExecutorService executorService;
    private final int maxConnTotal; //最大连接数

    public MyAsyncMysqlFunc(int maxConnTotal) {
        this.maxConnTotal = maxConnTotal;
    }

    @Override
    public void open(Configuration parameters) throws Exception {
        // 创建一个线程池（为了实现并发请求的）
        executorService = Executors.newFixedThreadPool(maxConnTotal);
        // 创建连接池（异步IO 一个请求就是一个线程，一个请求对应一个连接）
        druidDataSource = new DruidDataSource();
        druidDataSource.setDriverClassName("com.mysql.cj.jdbc.Driver");
        druidDataSource.setUsername("root");
        druidDataSource.setPassword("123456");
        druidDataSource.setUrl("jdbc:mysql://localhost:3306/test?characterEncoding=UTF-8");
        druidDataSource.setMaxActive(maxConnTotal);
    }

    @Override
    public void close() throws Exception {
        druidDataSource.close();
        executorService.shutdown();
    }

    @Override
    public void asyncInvoke(String id, ResultFuture<Tuple2<String, String>> resultFuture) throws Exception {
        // 将一个查询 丢进连接池中
        Future<String> future = executorService.submit(new Callable<String>() {
            @Override
            public String call() throws Exception {
                return queryFromMysql(id);
            }
        });

        CompletableFuture.supplyAsync(new Supplier<String>() {
            @Override
            public String get() {
                try {
                    return future.get();
                } catch (Exception e) {
                    e.printStackTrace();
                    return null;
                }
            }
        }).thenAccept(result -> {
            resultFuture.complete(Collections.singletonList(Tuple2.of(id, result)));
        });
    }

    @Override
    public void timeout(String input, ResultFuture<Tuple2<String, String>> resultFuture) throws Exception {
        super.timeout(input, resultFuture);
    }


    // 查询方法
    private String queryFromMysql(String param) throws SQLException {
        String sql = "select guid,eventId from EventLog where guid = ? ";

        DruidPooledConnection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        String result = null;

        try {
            conn = druidDataSource.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, Integer.parseInt(param));
            rs = ps.executeQuery();
            while (rs.next()) {
                result = rs.getString("eventId");
            }
        } finally {
            if (rs != null) {
                rs.close();
            }
            if (ps != null) {
                ps.close();
            }
            if (conn != null) {
                conn.close();
            }
        }
        return result;
    }


}
