package IODemos;

import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.time.StopWatch;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.*;

/**
 * @author lzx
 * @date 2023/3/23 16:27
 * @description: TODO
 */
public class JdbcTest {
    private static final Log logger = LogFactory.getFactory().getInstance(JdbcTest.class);
    private static final String JDBC_DRIVER = "com.mysql.cj.jdbc.Driver";
    private static final String DB_URL = "jdbc:mysql://192.168.1.73:9030/sample_data_warehouse?useServerPrepStmts=true";
    private static final String TBL = "sample_data_warehouse.DIM_SAMPLE_INFO_CJ";
    private static final String USER = "root";
    private static final String PASSWD = "000000";

    public static void main(String[] args) throws Exception {
        String filePath = "C:\\Users\\HR\\Desktop\\pressureTestData\\id_200.csv";
        FileReader fileReader = null;
        BufferedReader bufferedReader = null;
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        JSONObject jsonObject = new JSONObject();
        StopWatch watch = new StopWatch();
        watch.start();
        int count = 0;
        try {
            Class.forName(JDBC_DRIVER);
            conn = DriverManager.getConnection(DB_URL, USER, PASSWD);
            stmt = conn.prepareStatement("SELECT  * FROM  sample_data_warehouse.DIM_SAMPLE_INFO_CJ WHERE id = ?");
            fileReader = new FileReader(filePath);
            bufferedReader = new BufferedReader(fileReader);
            String line;
            while ((line = (bufferedReader.readLine())) != null) {
                stmt.setLong(1, Long.parseLong(line));
                rs = stmt.executeQuery();
                count++;
                ResultSetMetaData metaData = rs.getMetaData();
                while (rs.next()) {
                    for (int i = 1; i < metaData.getColumnCount(); i++) {
                        jsonObject.put(metaData.getColumnName(i), rs.getObject(i));
                    }
                    System.out.println(jsonObject);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            rs.close();
            stmt.close();
            conn.close();
            bufferedReader.close();
            fileReader.close();
        }
        watch.stop();
        logger.warn("数据量："+ count+"，耗时："+watch.getTime());
    }
    public void singleQuery(){

    }
    public void longSqlQuery(){

    }
}
