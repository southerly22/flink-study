package huorong.phoenixPool;

import com.alibaba.druid.pool.DruidPooledConnection;
import com.alibaba.fastjson.JSONObject;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import huorong.PhoenixQueryUtil;
import org.apache.phoenix.query.QueryServices;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;

/**
 * @author lzx
 * @date 2023/6/2 10:55
 * @description: TODO
 */
public class PoolDemo {
    public static void main(String[] args) throws ClassNotFoundException {

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
        HikariDataSource dataSource = new HikariDataSource(config);
        String sha1 = "000000015acd38b8f06d639438ac442493d08180";
        try {
            PhoenixQueryUtil queryUtil = new PhoenixQueryUtil(dataSource);
            JSONObject jsonObject1 = queryUtil.queryPhoenixPad(sha1);
            JSONObject jsonObject2 = queryUtil.queryPhoenixInfo(sha1);
            JSONObject jsonObject3 = queryUtil.queryPhoenixSrc(sha1);
            System.out.println(jsonObject2 + "," + jsonObject1 + "," + jsonObject3);
        } finally {
            dataSource.close();
        }
    }

    public static JSONObject queryPhoenixPad(HikariDataSource dataSource, String sha1) {
        JSONObject jSONObject = new JSONObject();
        String sql = "SELECT * FROM OFFICIAL.SAMPLE_PAD_SCAN_LATEST WHERE \"rk\"  like ?";

        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet resultSet = null;

        try {
            conn = dataSource.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setString(1, sha1.concat("%"));
            resultSet = ps.executeQuery();
            while (resultSet.next()) {
                String engine_name = resultSet.getString("engine_name") + "_";
                jSONObject.put(engine_name + "id", resultSet.getString("scan_id"));
                jSONObject.put(engine_name + "name", resultSet.getString("scan_name"));
                jSONObject.put(engine_name + "virus_name", resultSet.getString("virus_name"));
                jSONObject.put(engine_name + "virus_platform", resultSet.getString("virus_platform"));
                jSONObject.put(engine_name + "virus_tech", resultSet.getString("virus_tech"));
                jSONObject.put(engine_name + "virus_type", resultSet.getString("virus_type"));
                jSONObject.put("scan_time", resultSet.getString("scan_time"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if (resultSet != null) {
                try {
                    resultSet.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            if (ps != null) {
                try {
                    ps.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
        return jSONObject;
    }
}
