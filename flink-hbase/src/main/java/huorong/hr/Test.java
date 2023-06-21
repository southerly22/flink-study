package huorong.hr;

import org.apache.phoenix.jdbc.PhoenixDriver;
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
public class Test {
    public static void main(String[] args) throws SQLException, ClassNotFoundException {
        Sample sample = new Sample("57bb46654ff483606d7d75c2fe12b966", "5f4b62776b");
        Properties prop = new Properties();
        prop.setProperty(QueryServices.IS_NAMESPACE_MAPPING_ENABLED, "true");
        Class.forName("org.apache.phoenix.jdbc.PhoenixDriver");

        Connection conn = DriverManager.getConnection("jdbc:phoenix:192.168.1.118:2181;AutoCommit=true", prop);

        PreparedStatement ps = conn.prepareStatement("UPSERT INTO TEST.MD5_SEAWEEDPATH2 VALUES(?,?)");

        System.out.println("sample.getSeaweed_path() = " + sample.getSeaweed_path());
        System.out.println("sample.getMd5() = " + sample.getMd5());

        ps.setString(1,sample.getMd5());
        ps.setString(2,sample.getSeaweed_path());

        System.out.println("ps.toString() = " + ps.toString());
        ps.execute();
        // conn.commit();

        ps.close();
        conn.close();
    }
}
