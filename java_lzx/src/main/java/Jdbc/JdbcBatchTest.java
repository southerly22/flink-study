package Jdbc;

/**
 * Jdbc测试
 *
 * @author lzx
 * @date 2023/09/15 22:46
 **/

import java.sql.*;

public class JdbcBatchTest {

    public static void main(String[] args) throws SQLException, ClassNotFoundException {
        //execPrepareStatement(100, 50);
        execCreateStatement(100, 50);
    }

    /**
     * @param totalCnt 插入的总行数
     * @param batchCnt 每批次插入的行数，0表示单条插入
     */
    public static void execPrepareStatement(int totalCnt, int batchCnt) throws SQLException, ClassNotFoundException {
        String user = "root";
        String password = "123456";

        String driver = "com.mysql.cj.jdbc.Driver";
        String url = "jdbc:mysql://localhost:3306/test?useServerPrepStmts=true&rewriteBatchedStatements=true";

        long l1 = System.currentTimeMillis();
        Class.forName(driver);
        Connection connection = DriverManager.getConnection(url, user, password);
        connection.setAutoCommit(false);
        String sql = "insert into jiahe values (?, ?)";
        PreparedStatement preparedStatement = connection.prepareStatement(sql);
        for (int i = 1; i <= totalCnt; i++) {
            preparedStatement.setString(1, "red" + i);
            preparedStatement.setString(2, "yel" + i);

            if (batchCnt > 0) {
                // 批量执行
                preparedStatement.addBatch();
                if (i % batchCnt == 0) {
                    preparedStatement.executeBatch();
                } else if (i == totalCnt) {
                    preparedStatement.executeBatch();
                }
            } else {
                // 单条执行
                preparedStatement.executeUpdate();
            }
        }
        connection.commit();
        connection.close();
        long l2 = System.currentTimeMillis();
        System.out.println("总条数：" + totalCnt + (batchCnt > 0 ? ("，每批插入：" + batchCnt) : "，单条插入") + "，一共耗时：" + (l2 - l1) + " 毫秒");
    }

    /**
     * @param totalCnt 插入的总行数
     * @param batchCnt 每批次插入的行数，0表示单条插入
     */
    public static void execCreateStatement(int totalCnt, int batchCnt) throws SQLException, ClassNotFoundException {
        String user = "root";
        String password = "123456";

        String driver = "com.mysql.cj.jdbc.Driver";
        String url = "jdbc:mysql://localhost:3306/test?useServerPrepStmts=true&rewriteBatchedStatements=true";

        long l1 = System.currentTimeMillis();
        Class.forName(driver);
        Connection connection = DriverManager.getConnection(url, user, password);
        connection.setAutoCommit(false);

        Statement statement = connection.createStatement();
        for (int i = 1; i <= totalCnt; i++) {
            String sql = "insert into jiahe values ("+"'red" + i +"', "+ "'yel" + i +"')";
            if (batchCnt > 0) {
                // 批量执行
                statement.addBatch(sql);
                if (i % batchCnt == 0) {
                    statement.executeBatch();
                } else if (i == totalCnt) {
                    statement.executeBatch();
                }
            } else {
                // 单条执行
                statement.executeBatch();
            }
        }
        connection.commit();
        connection.close();
        long l2 = System.currentTimeMillis();
        System.out.println("总条数：" + totalCnt + (batchCnt > 0 ? ("，每批插入：" + batchCnt) : "，单条插入") + "，一共耗时：" + (l2 - l1) + " 毫秒");
    }
}

