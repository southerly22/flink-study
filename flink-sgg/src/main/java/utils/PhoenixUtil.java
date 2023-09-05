package utils;

import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.druid.pool.DruidPooledConnection;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;

import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * @author lzx
 * @date 2023/08/29 10:48
 *  写phoenix 类
 **/
public class PhoenixUtil {

    // {"id":"1001","name":"zhangsan","sex":"male"}
    public static void upsertIntoTable(String sinkTable, JSONObject data, DruidDataSource druidDataSource) throws SQLException {


        // 拼接sql
        String upsertSql = "upsert into "+sinkTable+"("+ StringUtils.join(data.keySet(),",")+")"
                + "('" + StringUtils.join(data.values(),"','")+"')";

        DruidPooledConnection conn = druidDataSource.getConnection();
        PreparedStatement preparedStatement = conn.prepareStatement(upsertSql);

        preparedStatement.execute();
        conn.commit();
        preparedStatement.close();
    }
}
