package huorong.phoenixPool;

import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.druid.pool.DruidPooledConnection;
import com.alibaba.fastjson.JSONObject;
import com.zaxxer.hikari.HikariDataSource;
import org.apache.phoenix.query.QueryServices;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Druid连接池管理Phoenix
 *
 * @author lzx
 * @date 2023/06/04 17:26
 **/
public class DruidPoolUtil {
    private static DruidDataSource druidDataSource;

    // Phoenix驱动
    public static final String PHOENIX_DRIVER = "org.apache.phoenix.jdbc.PhoenixDriver";

    // Phoenix连接参数 jdbc:phoenix:192.168.1.118:2181
    public static final String PHOENIX_SERVER = "jdbc:phoenix:node118,node119,node120:2181";

    public static DruidDataSource createConn() {
        //创建连接池
        druidDataSource = new DruidDataSource();

        druidDataSource.setDriverClassName(PHOENIX_DRIVER); //设置驱动
        druidDataSource.setUrl(PHOENIX_SERVER); // 设置 url
        Properties prop = new Properties();
        prop.setProperty(QueryServices.IS_NAMESPACE_MAPPING_ENABLED, "true");
        druidDataSource.setConnectProperties(prop);
        // 设置初始化连接池时池中连接的数量
        druidDataSource.setInitialSize(3);
        // 设置同时活跃的最大连接数
        druidDataSource.setMaxActive(10);
        // 设置空闲时的最小连接数，必须介于 0 和最大连接数之间，默认为 0
        druidDataSource.setMinIdle(1);
        // 设置没有空余连接时的等待时间，超时抛出异常，-1 表示一直等待
        druidDataSource.setMaxWait(-1);
        // 验证连接是否可用使用的 SQL 语句
        druidDataSource.setValidationQuery("select 1");
        // 指明连接是否被空闲连接回收器（如果有）进行检验，如果检测失败，则连接将被从池中去除
        // 注意，默认值为 true，如果没有设置 validationQuery，则报错
        // testWhileIdle is true, validationQuery not set
        druidDataSource.setTestWhileIdle(true);
        // 借出连接时，是否测试，设置为 false，不测试，否则很影响性能
        druidDataSource.setTestOnBorrow(false);
        // 归还连接时，是否测试
        druidDataSource.setTestOnReturn(false);
        // 设置空闲连接回收器每隔 30s 运行一次
        druidDataSource.setTimeBetweenEvictionRunsMillis(30 * 1000L);
        // 设置池中连接空闲 30min 被回收，默认值即为 30 min
        druidDataSource.setMinEvictableIdleTimeMillis(30 * 60 * 1000L);

        return druidDataSource;
    }

    // 测试
    public static void main(String[] args) throws SQLException {
        DruidDataSource dataSource = createConn();
        JSONObject jsonObject = queryPhoenixPad(dataSource, "000000015acd38b8f06d639438ac442493d08180");
        System.out.println(jsonObject);
    }

    public static JSONObject queryPhoenixPad(DruidDataSource dataSource, String sha1) throws SQLException {
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
        } finally {
            if (resultSet != null) {
                resultSet.close();
            }
            if (ps != null) {
                ps.close();
            }
            if (conn != null) {
                conn.close();
            }
        }
        return jSONObject;
    }
}
