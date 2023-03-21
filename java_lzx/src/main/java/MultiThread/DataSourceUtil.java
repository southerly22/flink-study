package MultiThread;



import com.alibaba.druid.pool.DruidDataSourceFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

/**
 * 数据库连接工具类
 *
 * @author lzx
 * @date 2023/03/20 22:59
 **/
public class DataSourceUtil {
    public static void main(String[] args) throws SQLException {
        Connection conn = DataSourceUtil.getConn();
        System.out.println("获得的连接 conn="+conn);
    }

    // 创建一个成员变量
    private static DataSource ds;

    static {
        try {
            Properties info = new Properties();
            //加载类路径下，即src目录下的druid.properties这个文件
            info.load(DataSourceUtil.class.getResourceAsStream("src/main/resources/druid.properties"));

            //读取属性文件创建连接池
            ds = DruidDataSourceFactory.createDataSource(info);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 得到数据源
     */
    public static DataSource getDataSource() {
        return ds;
    }

    /**
     * 得到数据库连接
     */
    public static Connection getConn() throws SQLException {
        return ds.getConnection();
    }

    /**
     * 释放资源
     */
    public static void close(Connection conn, Statement stmt, ResultSet rs) {
        if (rs != null) {
            try {
                rs.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        if (stmt != null) {
            try {
                stmt.close();
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


    public static void close(Connection conn, Statement stmt) {
        close(conn, stmt, null);
    }
}
