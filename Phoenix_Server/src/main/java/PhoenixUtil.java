import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.phoenix.query.QueryServices;

import java.sql.*;
import java.util.Properties;

/**
 * @author lzx
 * @date 2023/04/23 10:32
 **/
public class PhoenixUtil {
    private final Log log = LogFactory.getFactory().getInstance(PhoenixUtil.class);

    public Connection getConn() {

        Properties prop = new Properties();
        prop.setProperty(QueryServices.IS_NAMESPACE_MAPPING_ENABLED,"true");
        //prop.setProperty(QueryServices.)
        Connection conn = null;
        try {
            Class.forName("org.apache.phoenix.queryserver.client.Driver");
            String url = "jdbc:phoenix:thin:url=http://192.168.3.190:8765;serialization=PROTOBUF";
            conn = DriverManager.getConnection(url,prop);
        } catch (ClassNotFoundException e) {
            log.error(e.getMessage());
            e.printStackTrace();
        } catch (SQLException e1) {
            log.error(e1.getMessage());
            e1.printStackTrace();
        }
        return conn;

    }

    /**
     * 关闭资源
     *
     * @param conn
     * @param statement
     * @param rs
     */
    public void closeRes(Connection conn, Statement statement, ResultSet rs) {
        try {
            if (conn != null) {
                conn.close();
            }
            if (statement != null)
                statement.close();
            if (rs != null)
                rs.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 通过phoenix 创建表、插入数据、索引、查询数据
     * 写入数据conn.setAutoCommit(false);
     * 删除数据conn.setAutoCommit(true);
     * phoenix demo
     */
    public void phoenixDemo() {
        Connection conn = getConn();
        ResultSet rs = null;
        Statement stmt = null;
        try {
            stmt = conn.createStatement();

            //stmt.execute("create table usdp.test_java4 (mykey integer not null primary key, mycolumn varchar)");
            for (int i = 0; i < 100; i++) {
                stmt.executeUpdate("upsert into usdp.test_java1 values ("+ i +",'The num is "+ i +"')");
            }
            conn.commit();
            PreparedStatement statement = conn.prepareStatement("select mykey from usdp.test_java1 where mycolumn='The num is 88'");
            rs = statement.executeQuery();
            while (rs.next()) {
                System.out.println("-------------The num is ---------------" + rs.getInt(1));
            }
        } catch (SQLException e) {
            log.error(e.getMessage());
            e.printStackTrace();
        } finally {
            closeRes(conn,stmt,rs);
        }
    }

    public static void main(String[] args) {
        PhoenixUtil phoenixUtil = new PhoenixUtil();
        phoenixUtil.phoenixDemo();
    }
}
