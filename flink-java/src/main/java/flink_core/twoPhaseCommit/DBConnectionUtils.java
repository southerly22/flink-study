package flink_core.twoPhaseCommit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * @author lzx
 * @date 2023/5/17 13:08
 * @description: TODO
 */
public class DBConnectionUtils {
    private final static Logger log = LoggerFactory.getLogger(DBConnectionUtils.class);

    public static Connection getConn(String url,String user,String passwd) throws SQLException {
        Connection conn = null;
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            log.error("获取com.mysql.cj.jdbc.Driver 失败！");
            e.printStackTrace();
        }
        try {
            conn = DriverManager.getConnection(url,user,passwd);
            log.info("获取 conn 成功");
        }catch (Exception e){
            log.info("获取 conn 失败");
            e.printStackTrace();
        }

        conn.setAutoCommit(false);
        return conn;
    }

    /***
     * @Author: lzx
     * @Description: 提交事务
     * @Date: 2023/5/17
     **/
    public static void commit(Connection conn){
        if (conn!=null){
            try {
                conn.commit();
            } catch (SQLException e) {
                log.error("提交事务失败！");
                e.printStackTrace();
            }finally {
                close(conn);
            }
        }
    }

    /***
     * @Author: lzx
     * @Description: 事务回滚
     * @Date: 2023/5/17
     **/
    public static void rollback(Connection conn){
        if (conn!=null){
            try {
                conn.rollback();
            } catch (SQLException e) {
                log.error("事务回滚失败！");
                e.printStackTrace();
            }finally {
                close(conn);
            }
        }
    }

    /**
     * 关闭连接
     * @param conn
     */
    public static void close(Connection conn) {
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException e) {
                log.error("关闭连接失败,Connection:" + conn);
                e.printStackTrace();
            }
        }
    }
}
