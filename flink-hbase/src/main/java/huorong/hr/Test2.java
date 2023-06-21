package huorong.hr;

import org.apache.phoenix.query.QueryServices;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Properties;

/**
 * @author lzx
 * @date 2023/6/20 16:11
 * @description: TODO
 */
public class Test2 {
    public static void main(String[] args) throws SQLException, ClassNotFoundException {
        Sample sample = new Sample("57bb46654ff483606d7d75c2fe12b95b", "6,5f4b62776b");

        Class.forName("com.mysql.cj.jdbc.Driver");
        Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/test?characterEncoding=UTF-8", "root","123456");
        PreparedStatement ps = conn.prepareStatement("insert into test.seaweedPath(md5,seaweed_path) values(?,?)");

        System.out.println("sample.getSeaweed_path() = " + sample.getSeaweed_path());
        System.out.println("sample.getMd5() = " + sample.getMd5());

        ps.setString(1, sample.getMd5());
        ps.setString(2, sample.getSeaweed_path());

        System.out.println("ps.toString() = " + ps.toString());
        ps.executeUpdate();
        // ps.execute();

        ps.close();
        conn.close();
    }
}
