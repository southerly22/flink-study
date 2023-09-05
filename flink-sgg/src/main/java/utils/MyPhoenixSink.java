package utils;

import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.fastjson.JSONObject;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.sink.RichSinkFunction;

/**
 * @author lzx
 * @date 2023/08/29 10:49
 **/
public class MyPhoenixSink extends RichSinkFunction<JSONObject> {
    DruidDataSource druidDataSource;
    // 获取连接
    @Override
    public void open(Configuration parameters) throws Exception {
         druidDataSource = DruidPhoenixUtil.getDruidDataSource();
    }

    // 写出数据
    //value:{"database":"gmall-211126-flink","table":"base_trademark","type":"update","ts":1652499176,"xid":188,"commit":true,
    // "data":{"id":13,"tm_name":"atguigu"},
    // "old":{"logo_url":"/aaa/aaa"},
    // "sinkTable":"dim_xxx"}
    @Override
    public void invoke(JSONObject value, Context context) throws Exception {

        String sinkTable = value.getString("sinkTable");
        JSONObject data = value.getJSONObject("data");

        // 写出数据
        PhoenixUtil.upsertIntoTable(sinkTable,data,druidDataSource);

        // 归还链接
        druidDataSource.close();
    }
}
