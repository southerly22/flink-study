package huorong;

import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.fastjson.JSONObject;
import org.apache.phoenix.query.QueryServices;

import java.sql.*;
import java.util.Properties;

/**
 * @author lzx
 * @date 2023/05/29 10:02
 **/
public class TestPhoenix {
    public static void main(String[] args) throws SQLException {

        PhoenixConnPool phoenixConnPool = new PhoenixConnPool(3,3, 5);

        JSONObject jsonObject = queryPhoenixPad("0000007b89a659fa4aa9cfc943e4c14872d6242e",phoenixConnPool);
        System.out.println("jsonObject = " + jsonObject);

        phoenixConnPool.destroyPool();
    }
    public static JSONObject queryPhoenixPad(String sha1,PhoenixConnPool pool) {
        JSONObject jSONObject = new JSONObject();
        String sql = "SELECT * FROM OFFICIAL.SAMPLE_PAD_SCAN_LATEST WHERE \"rk\"  like ?";

        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet resultSet = null;

        try {
            conn = pool.getConn();
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
                pool.realease(conn); //释放
            }
        }
        return jSONObject;
    }
}
