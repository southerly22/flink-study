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
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.*;
import java.util.function.Supplier;

/**
 * mysqlAsync方法
 *
 * @author lzx
 * @date 2023/05/23 11:35
 **/
public class MyAsyncDorisFunc extends RichAsyncFunction<String, Tuple2<String, String>> {
    private transient DruidDataSource druidDataSource;
    private transient ExecutorService executorService;
    private int maxConnTotal; //最大连接数

    public MyAsyncDorisFunc(int maxConnTotal) {
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
        druidDataSource.setPassword("000000");
        druidDataSource.setUrl("jdbc:mysql://192.168.1.73:9030/tmp?characterEncoding=UTF-8&rewriteBatchedStatements=true");
        druidDataSource.setMaxActive(maxConnTotal);
    }

    @Override
    public void close() throws Exception {
        druidDataSource.close();
        executorService.shutdown();
    }

    @Override
    public void asyncInvoke(String id, ResultFuture<Tuple2<String, String>> resultFuture) throws Exception {
        System.out.println("inPut:" + id + "," + Instant.now());
        // 将一个查询 丢进连接池中
        Future<String> future = executorService.submit(new Callable<String>() {
            @Override
            public String call() throws Exception {
                return queryFromMysql();
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
    private String queryFromMysql() throws SQLException {
        String sql = "	SELECT a.sha1,b.* "+
                "	FROM DW_HASHSIG_SHA1_CO a "+
                "	JOIN ( "+
                "		SELECT c.hashsig,c.hashsig_type,c.mark_result,d.sum_cnt,e.mark_usernames "+
                "		FROM DW_HASHSIG_TABLE_CO c "+
                "	JOIN ( "+
                "		SELECT hashsig,sum(cnt) as sum_cnt "+
                "		FROM DW_HASHSIG_LOG "+
                "		WHERE log_type = 1 "+
                "		GROUP BY hashsig "+
                "	) d "+
                "	ON c.hashsig = d.hashsig "+
                "	JOIN ( "+
                "		SELECT hashsig,group_concat(DISTINCT mark_user_name) as mark_usernames "+
                "		FROM DW_HASHSIG_LOG "+
                "		GROUP BY hashsig "+
                "	) e "+
                "	ON c.hashsig = e.hashsig  "+
                "	WHERE c.hashsig_type = 1  and c.publish_process = 1 and c.mark_result = 1  "+
                "	)b ON a.hashsig = b.hashsig  "+
                "	where a.hashsig_type = 1 "+
                "	LIMIT 20, 10 ";

        DruidPooledConnection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        ArrayList<String> list = new ArrayList<>();

        try {
            conn = druidDataSource.getConnection();
            ps = conn.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                Object o1 = rs.getObject(1);
                Object o2 = rs.getObject(2);
                Object o3 = rs.getObject(3);
                Object o4 = rs.getObject(4);
                Object o5 = rs.getObject(5);
                Object o6 = rs.getObject(6);
                list.add(Arrays.asList(o1,o2,o3,o4,o5,o6).toString());
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
        return list.toString();
    }


}
