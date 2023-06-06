package huorong;

import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.druid.pool.DruidPooledConnection;
import com.alibaba.fastjson.JSONObject;
import com.zaxxer.hikari.HikariDataSource;
import org.apache.commons.lang.StringEscapeUtils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Locale;

/**
 * @author lzx
 * @date 2023/5/26 17:23
 * @description: TODO
 */
public class PhoenixQueryUtil {

    public static JSONObject queryPhoenix(Connection conn, String tableName, String sha1) throws SQLException {
        JSONObject jSONObject = new JSONObject();

        String sql = "SELECT * FROM OFFICIAL." + tableName + " WHERE \"rk\"  like ?";
        PreparedStatement ps = null;
        ResultSet resultSet = null;
        try {
            ps = conn.prepareStatement(sql);
            ps.setString(1, sha1.concat("%"));
            resultSet = ps.executeQuery();
            switch (tableName) {
                case "SAMPLE_PAD_SCAN_LATEST": {
                    while (resultSet.next()) {
                        String engine_name = resultSet.getString("engine_name") + "_";
                        jSONObject.put("rk", sha1);
                        jSONObject.put(engine_name + "id", resultSet.getString("scan_id"));
                        jSONObject.put(engine_name + "name", StringEscapeUtils.escapeSql(resultSet.getString("scan_name")));
                        jSONObject.put(engine_name + "virus_name", resultSet.getString("virus_name"));
                        jSONObject.put(engine_name + "virus_platform", resultSet.getString("virus_platform"));
                        jSONObject.put(engine_name + "virus_tech", resultSet.getString("virus_tech"));
                        jSONObject.put(engine_name + "virus_type", resultSet.getString("virus_type"));
                        jSONObject.put("scan_time", resultSet.getString("scan_time"));
                    }
                }
                break;
                case "SAMPLE_INFO": {
                    while (resultSet.next()) {
                        jSONObject.put("id", resultSet.getBigDecimal("id"));
                        jSONObject.put("fdfs_path", resultSet.getString("fdfs_path"));
                        jSONObject.put("filesize", resultSet.getBigDecimal("filesize"));
                        jSONObject.put("md5", resultSet.getString("md5"));
                        jSONObject.put("simhash", resultSet.getString("simhash"));
                        jSONObject.put("hashsig", resultSet.getString("hashsig"));
                        jSONObject.put("hashsig_pe", resultSet.getString("hashsig_pe"));
                        jSONObject.put("filetype", resultSet.getString("filetype"));
                        jSONObject.put("die", StringEscapeUtils.escapeSql(resultSet.getString("die")));
                        jSONObject.put("sha1", sha1);
                        jSONObject.put("sha256", resultSet.getString("sha256"));
                        jSONObject.put("sha512", resultSet.getString("sha512"));
                        jSONObject.put("addtime", resultSet.getBigDecimal("addtime"));
                        jSONObject.put("lastaddtime", resultSet.getBigDecimal("lastaddtime"));
                    }
                }
                break;
                case "SAMPLE_SRC": {
                    StringBuffer buffer = new StringBuffer();
                    while (resultSet.next()) {
                        String src_list = resultSet.getString("src_name");
                        buffer.append(src_list).append(",");
                    }
                    jSONObject.put("src_list", buffer.deleteCharAt(buffer.length() - 1).toString());
                    buffer.setLength(0); //清空
                }
                break;
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
