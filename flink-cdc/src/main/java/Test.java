import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.druid.pool.DruidPooledConnection;
import utils.DruidPoolUtil;

import java.sql.SQLException;

/**
 * @author lzx
 * @date 2023/10/18 22:40
 **/
public class Test {
    public static void main(String[] args) throws SQLException {
        DruidDataSource dataSource = DruidPoolUtil.getDruidDataSource("mysql");
        DruidPooledConnection conn = dataSource.getConnection();
        System.out.print(conn);

        conn.close();
    }
}