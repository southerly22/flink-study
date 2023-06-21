package huorong;

import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.sink.RichSinkFunction;
import org.apache.phoenix.query.QueryServices;

import java.lang.management.MemoryUsage;
import java.sql.*;
import java.util.List;
import java.util.Properties;

/**
 * @author lzx
 * @date 2023/05/29 17:28
 **/
public class PhoenixSink extends RichSinkFunction<List<JSONObject>> {

    private transient Connection conn;
    private String dataBase;
    private String tableName;

    public PhoenixSink(String dataBase, String tableName) {
        this.dataBase = dataBase;
        this.tableName = tableName;
    }

    @Override
    public void open(Configuration parameters) throws Exception {
        Properties prop = new Properties();
        prop.setProperty(QueryServices.IS_NAMESPACE_MAPPING_ENABLED, "true");
        prop.setProperty(QueryServices.ZOOKEEPER_QUORUM_ATTRIB, "192.168.1.118,192.168.1.119,192.168.1.120:2181");
        Class.forName("org.apache.phoenix.jdbc.PhoenixDriver");
        conn = DriverManager.getConnection("jdbc:phoenix:192.168.1.118:2181", prop);

    }

    @Override
    public void close() throws Exception {
        if (conn != null) {
            conn.close();
        }
    }

    @Override
    public void invoke(List<JSONObject> list, Context context) throws Exception {
        Statement statement = conn.createStatement();
        try {
            for (JSONObject l : list) {
                String upsertSql = generateUpsertSql(l);
                statement.addBatch(upsertSql);
            }
            statement.executeBatch();
            conn.commit();
            // int[] batch = statement.executeBatch();//批量后执行
            // System.out.println("插入数据：" + batch.length);
        }catch (Exception e){
            System.out.println("报错了！！！");
            System.out.println(statement);
            System.out.println("e.getMessage() = " + e.getMessage());
            for (JSONObject l : list) {
                System.out.println(generateUpsertSql(l));
            }
        }
        finally {
            conn.commit();
            if (statement != null) {
                statement.close();
            }
        }
    }

    public String generateUpsertSql(JSONObject jsObj) {

        StringBuilder sql = new StringBuilder();
        sql.append("upsert into " + dataBase + "." + tableName + "(");
        sql.append("\"").append(StringUtils.join(jsObj.keySet(), "\",\"")).append("\")");
        sql.append("values( '").append(StringUtils.join(jsObj.values(), "','")).append("')");
        return sql.toString();
    }
}
