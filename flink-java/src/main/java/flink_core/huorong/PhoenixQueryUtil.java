package flink_core.huorong;

import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.druid.pool.DruidPooledConnection;
import com.alibaba.fastjson.JSONObject;

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

    private transient DruidDataSource dataSource;

    public PhoenixQueryUtil(DruidDataSource dataSource) {
        this.dataSource = dataSource;
    }

    // val src_selectSql: String = "SELECT \"src_name\" FROM " + hbaseSchema + ".SAMPLE_SRC WHERE \"rk\"  like ?"

    public JSONObject queryPhoenixPad(String sha1) {
        JSONObject jSONObject = new JSONObject();
        String sql = "SELECT * FROM OFFICIAL.SAMPLE_PAD_SCAN_LATEST WHERE \"rk\"  like ?%";

        DruidPooledConnection conn = null;
        PreparedStatement ps = null;
        ResultSet resultSet = null;

        try {
            conn = dataSource.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setString(1, sha1);
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
                try {
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
        return jSONObject;
    }

    public JSONObject queryPhoenixInfo(String sha1) {
        JSONObject jSONObject = new JSONObject();
        String sql = "SELECT * FROM OFFICIAL.SAMPLE_INFO WHERE \"rk\"  like ?%";

        DruidPooledConnection conn = null;
        PreparedStatement ps = null;
        ResultSet resultSet = null;

        try {
            conn = dataSource.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setString(1, sha1);
            resultSet = ps.executeQuery();
            while (resultSet.next()) {
                jSONObject.put("id", resultSet.getBigDecimal("id"));
                jSONObject.put("fdfs_path", resultSet.getString("fdfs_path"));
                jSONObject.put("filesize", resultSet.getBigDecimal("filesize"));
                jSONObject.put("md5", resultSet.getString("md5"));
                jSONObject.put("simhash", resultSet.getString("simhash"));
                jSONObject.put("hashsig", resultSet.getString("hashsig"));
                jSONObject.put("hashsig_pe", resultSet.getString("hashsig_pe"));
                jSONObject.put("filetype", resultSet.getString("filetype"));
                jSONObject.put("die", resultSet.getString("die"));
                jSONObject.put("sha1", sha1);
                jSONObject.put("sha256", resultSet.getString("sha256"));
                jSONObject.put("sha512", resultSet.getString("sha512"));
                jSONObject.put("addtime", resultSet.getBigDecimal("addtime"));
                jSONObject.put("lastaddtime", resultSet.getBigDecimal("lastaddtime"));
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
                try {
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
        return jSONObject;
    }

    public JSONObject queryPhoenixSrc(String sha1) {
        JSONObject jSONObject = new JSONObject();
        String sql = "SELECT * FROM OFFICIAL.SAMPLE_SRC WHERE \"rk\"  like ?%";

        DruidPooledConnection conn = null;
        PreparedStatement ps = null;
        ResultSet resultSet = null;

        try {
            conn = dataSource.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setString(1, sha1);
            resultSet = ps.executeQuery();
            StringBuffer buffer = new StringBuffer(20);
            while (resultSet.next()) {
                String src_list = resultSet.getString("src_list");
                buffer.append(src_list).append(",");
            }
            jSONObject.put("src_list", buffer.deleteCharAt(buffer.length() - 1));
            buffer.setLength(0); //清空
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
                try {
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
        return jSONObject;
    }
}
