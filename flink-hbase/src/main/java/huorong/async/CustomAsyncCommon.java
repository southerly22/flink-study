package huorong.async;

import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.druid.pool.DruidPooledConnection;
import com.alibaba.fastjson.JSONObject;
import huorong.phoenixPool.DimUtil;
import huorong.phoenixPool.ThreadPoolUtil;
import huorong.phoenixPool.DruidPoolUtil;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.async.ResultFuture;
import org.apache.flink.streaming.api.functions.async.RichAsyncFunction;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.*;

/**
 * @author lzx
 * @date 2023/06/04 17:35
 *  todo 通用的异步io实现类，泛型 + 抽象方法
 **/
public abstract class CustomAsyncCommon<T> extends RichAsyncFunction<T, T> implements CustomAsyncInter<T> {

    private transient DruidDataSource druidDataSource;
    private ThreadPoolExecutor threadPoolExecutor;
    private String tableName;

    public CustomAsyncCommon() {
    }

    public CustomAsyncCommon(String tableName) {
        this.tableName = tableName;
    }

    @Override
    public void open(Configuration parameters) throws Exception {
        // 获取连接池
        druidDataSource = DruidPoolUtil.createConn();
        //获取线程池
        threadPoolExecutor = ThreadPoolUtil.getThreadPoolExecutor();
    }

    @Override
    public void close() throws Exception {
        if (druidDataSource != null) {
            druidDataSource.close();
        }
        if (threadPoolExecutor != null) {
            threadPoolExecutor.shutdown();
        }
    }


    // 异步处理
    @Override
    public void asyncInvoke(T input, ResultFuture<T> resultFuture) throws Exception {
        Future<T> future1 = threadPoolExecutor.submit(new Callable<T>() {
            @Override
            public T call() throws Exception {
                //JSONObject dimInfo = null;
                List<JSONObject> dimInfoList = null;
                try {
                    // 获取连接
                    DruidPooledConnection conn = druidDataSource.getConnection();

                    // 获取key
                    String key = getKey(input);

                    //查询维表
                    //dimInfo = DimUtil.getDimInfo(conn, tableName, key);
                    dimInfoList = DimUtil.getDimInfoList(conn, tableName, key);

                    //补充维度信息
                    join(input, dimInfoList);

                    //归还连接
                    conn.close();
                } catch (Exception e) {
                    e.printStackTrace();
                    throw new RuntimeException("关联失败输出为：" + input + "，表为：" + tableName);
                }
                return input;
            }
        });
        CompletableFuture.supplyAsync(()->{
            try {
                return future1.get();
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }).thenAccept(res->{
            resultFuture.complete(Collections.singletonList(res));
        });

    }

    @Override
    public void timeout(T input, ResultFuture<T> resultFuture) throws Exception {
        System.out.println("失败重试--> " + input);
        asyncInvoke(input,resultFuture);
    }
}
