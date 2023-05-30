package huorong;

import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.sink.RichSinkFunction;
import org.apache.phoenix.query.QueryServices;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Properties;

/**
 * @author lzx
 * @date 2023/05/29 17:28
 **/
public class PhoenixSink extends RichSinkFunction<JSONObject> {

    private transient Connection conn;
    private Long cnt = 0L;

    private String dataBase;
    private String tableName;
    private Long batchSize;

    public PhoenixSink(String dataBase, String tableName,Long batchSize) {
        this.dataBase = dataBase;
        this.tableName = tableName;
        this.batchSize = batchSize;
    }

    @Override
    public void open(Configuration parameters) throws Exception {
        Properties prop = new Properties();
        prop.setProperty(QueryServices.IS_NAMESPACE_MAPPING_ENABLED, "true");
        prop.setProperty(QueryServices.AUTO_COMMIT_ATTRIB, "false");
        prop.setProperty(QueryServices.ZOOKEEPER_QUORUM_ATTRIB, "192.168.1.118,192.168.1.119,192.168.1.120:2181");
        prop.setProperty(QueryServices.KEEP_ALIVE_MS_ATTRIB, "600000");
        Class.forName("org.apache.phoenix.jdbc.PhoenixDriver");
        conn = DriverManager.getConnection("jdbc:phoenix:192.168.1.118:2181", prop);
    }

    @Override
    public void close() throws Exception {
        if (conn!=null) {
            conn.close();
        }
    }

    @Override
    public void invoke(JSONObject value, Context context) throws Exception {

        String upsertSql = generateUpsertSql(value);
        conn.setAutoCommit(false);
        PreparedStatement ps = null;
        try {
            ps = conn.prepareStatement(upsertSql);
            System.out.println(ps);
            ps.addBatch();
            cnt++;

            if (cnt % batchSize==0){
                ps.executeBatch();
                conn.commit();
                System.out.println("提交写入");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            conn.commit();
            if (ps!=null) {
                ps.close();
            }
        }
    }

    public String generateUpsertSql(JSONObject jsObj){

        StringBuilder sql = new StringBuilder();
        sql.append("upsert into "+dataBase+"."+tableName+"(");
        sql.append("\"").append(StringUtils.join(jsObj.keySet(), "\",\"")).append("\")");
        sql.append("values( '").append(StringUtils.join(jsObj.values(), "','")).append("')");
        return sql.toString();
    }
}
