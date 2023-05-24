package flink_core.asyncio;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.apache.flink.api.common.functions.RuntimeContext;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.datastream.AsyncDataStream;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.ProcessFunction;
import org.apache.flink.streaming.api.functions.async.ResultFuture;
import org.apache.flink.streaming.api.functions.async.RichAsyncFunction;
import org.apache.flink.util.Collector;
import org.apache.http.HttpResponse;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClients;
import org.apache.http.util.EntityUtils;
import org.apache.parquet.filter2.predicate.Operators;

import java.util.Collections;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * 异步IO使用场景：
 * 用来查询外部数据的，访问大量的数据，这些数据无法直接拿到（请求的数据库、接口）并又希望关联的速度更快，提高程序的吞吐量
 * <p>
 * 异步IO的底层原理：
 * 底层原理就是使用多线程的方式，在一段时间内，使用多线程(线程池) 方式在同一个subtask发送更多的请求（异步的）
 * 代价：
 * 消耗更多的cpu 资源
 * 需求：
 * 使用异步IO的方式请求高德地图地理位置API，获取相关信息
 * 测试数据：
 * //{"oid": "o1000", "cid": "c10", "money": 99.99, "longitude": 116.413467, "latitude": 39.908072}
 * //{"oid": "o1001", "cid": "c11", "money": 99.99, "longitude": 116.413467, "latitude": 39.908072}
 * //{"oid": "o1000", "cid": "c10", "money": 99.99, "longitude": 116.413467, "latitude": 39.908072}
 * //{"oid": "o1001", "cid": "c11", "money": 99.99, "longitude": 116.413467, "latitude": 39.908072}
 * //{"oid": "o1000", "cid": "c10", "money": 99.99, "longitude": 116.413467, "latitude": 39.908072}
 * //{"oid": "o1001", "cid": "c11", "money": 99.99, "longitude": 116.413467, "latitude": 39.908072}
 *
 * @author lzx
 * @date 2023/05/23 17:20
 **/
public class AsyncIoNetDemo {
    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        DataStreamSource<String> source = env.socketTextStream("localhost", 9999);

        SingleOutputStreamOperator<OrderBean> processDS = source.process(new ProcessFunction<String, OrderBean>() {
            @Override
            public void processElement(String value, ProcessFunction<String, OrderBean>.Context ctx, Collector<OrderBean> out) throws Exception {
                OrderBean orderBean = JSON.parseObject(value, OrderBean.class);
                out.collect(orderBean);
            }
        });

        AsyncDataStream.unorderedWait(
                processDS,
                new HttpAsyncFunction(),
                3000,
                TimeUnit.MILLISECONDS

        ).print();

        env.execute("async");
    }

    /**
     * 在pom文件中添加异步httpClient的依赖
     * <p>
     * <!-- 高效的异步HttpClient -->
     * <dependency>
     * <groupId>org.apache.httpcomponents</groupId>
     * <artifactId>httpasyncclient</artifactId>
     * <version>4.1.4</version>
     * </dependency>
     */
    public static class HttpAsyncFunction extends RichAsyncFunction<OrderBean, OrderBean> {

        String key = "74e962e2f795114980cd33ce09d923d1";
        private CloseableHttpAsyncClient httpAsyncClient;

        @Override
        public void open(Configuration parameters) throws Exception {
            // 初始化可以多线程发送异步请求的客户端
            // 创建异步查询的httpclient
            // 创建异步查询的httpclient的连接池
            RequestConfig requestConfig = RequestConfig.custom()
                    .setSocketTimeout(3000)
                    .setConnectTimeout(3000)
                    .build();
            httpAsyncClient = HttpAsyncClients.custom()
                    .setMaxConnTotal(20)
                    .setDefaultRequestConfig(requestConfig)
                    .build();
            //开启异步查询线程池
            httpAsyncClient.start();
        }

        @Override
        public void close() throws Exception {
            httpAsyncClient.close();
        }

        // asyncInvoke 也是来一条调用一次
        // 该方法中可以开启多线程进行查询，不必等待该方法的返回，就可以对下一条数据进行异步查询
        @Override
        public void asyncInvoke(OrderBean orderBean, ResultFuture<OrderBean> resultFuture) throws Exception {
            // 获取查询条件
            Double longitude = orderBean.longitude;
            Double latitude = orderBean.latitude;
            // 使用Get方式进行查询
            HttpGet httpGet = new HttpGet("https://restapi.amap.com/v3/geocode/regeo?&location=" + longitude + "," + latitude + "&key=" + key);
            System.out.println("httpGet.getURI().toString() = " + httpGet.getURI().toString());
            //查询返回Future
            Future<HttpResponse> future = httpAsyncClient.execute(httpGet, null);

            // 从Future中取数据 (回调方法)
            CompletableFuture.supplyAsync(new Supplier<OrderBean>() {
                // 当future中有数据 会调用get方法
                @Override
                public OrderBean get() {
                    // 从future中获取数据
                    try {
                        HttpResponse response = future.get();
                        String province = null;
                        String city = null;
                        //获取查询状态
                        if (response.getStatusLine().getStatusCode() == 200) {
                            //获取请求的字符串
                            String result = EntityUtils.toString(response.getEntity());
                            // 转成json对象
                            JSONObject jsonObj = JSON.parseObject(result);
                            //获取位置信息
                            JSONObject regeocode = jsonObj.getJSONObject("regeocode");
                            if (regeocode != null && !regeocode.isEmpty()) {
                                JSONObject address = regeocode.getJSONObject("addressComponent");
                                //获取省市区
                                province = address.getString("province");
                                city = address.getString("city");
                                //String businessAreas = address.getString("businessAreas");
                            }
                        }
                        orderBean.province = province;
                        orderBean.city = city;
                        return orderBean;
                    } catch (Exception e) {
                        e.printStackTrace();
                        return null;
                    }
                }
            }).thenAccept((OrderBean res) -> {
                resultFuture.complete(Collections.singletonList(res));
            });
        }

        @Override
        public void timeout(OrderBean input, ResultFuture<OrderBean> resultFuture) throws Exception {
            System.out.println("超时数据>>>" + input.toString());
        }
    }

    public static class OrderBean {
        public String oid;

        public String cid;

        public Double money;

        public Double longitude;

        public Double latitude;

        public String province;

        public String city;

        public OrderBean() {
        }

        public OrderBean(String oid, String cid, Double money, Double longitude, Double latitude) {
            this.oid = oid;
            this.cid = cid;
            this.money = money;
            this.longitude = longitude;
            this.latitude = latitude;
        }

        public static OrderBean of(String oid, String cid, Double money, Double longitude, Double latitude) {
            return new OrderBean(oid, cid, money, longitude, latitude);
        }

        @Override
        public String toString() {
            return "OrderBean{" + "oid='" + oid + '\'' + ", cid='" + cid + '\'' + ", money=" + money + ", longitude=" + longitude + ", latitude=" + latitude + ", province='" + province + '\'' + ", city='" + city + '\'' + '}';
        }
    }
}
