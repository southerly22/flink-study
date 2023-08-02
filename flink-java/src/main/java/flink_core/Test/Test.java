package flink_core.Test;

import java.sql.*;

/**
 * @author lzx
 * @date 2023/6/19 14:00
 * @description: TODO
 */
public class Test {
    public static void main(String[] args) throws ClassNotFoundException, SQLException {
        String jdbcUrl = "jdbc:mysql://localhost:3306/test?characterEncoding=UTF-8";
        String driverName = "com.mysql.cj.jdbc.Driver";

        Class.forName(driverName);
        Connection conn = DriverManager.getConnection(jdbcUrl, "root", "123456");

        PreparedStatement ps = conn.prepareStatement("SELECT id, name, class, count, date FROM fruits");
        ResultSet rs = ps.executeQuery();
        ResultSetMetaData metaData = rs.getMetaData();
        int columnCount = metaData.getColumnCount();

        while (rs.next()) {
            for (int i = 1; i <= columnCount; i++) {
                String column = rs.getString(metaData.getColumnName(i));
                System.out.println(metaData.getColumnName(i)+" : " +column);
            }
        }

        rs.close();
        ps.close();
        conn.close();
    }
}
