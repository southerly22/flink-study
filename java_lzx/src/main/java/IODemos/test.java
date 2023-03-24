package IODemos;

import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.time.StopWatch;
import org.apache.phoenix.query.QueryServices;

import java.io.*;
import java.sql.*;
import java.util.Properties;
import java.util.Random;

/**
 * @author lzx
 * @date 2023/3/22 14:12
 * @description: TODO
 */
public class test {
    // private static final String JDBC_DRIVER = "com.mysql.cj.jdbc.Driver";
    // private static final String DB_URL = "jdbc:mysql://192.168.1.73:9030/sample_data_warehouse?rewriteBatchedStatements=true";
    // private static final String TBL = "DIM_SAMPLE_INFO";
    // private static final String USER = "root";
    // private static final String PASSWD = "000000";

    private static String PHOENIX_JDBC_URL = "jdbc:phoenix:192.168.1.118:2181";
    private static String PHOENIX_JDBC_DRIVER  = "org.apache.phoenix.jdbc.PhoenixDriver";
    private static final String TBL = "OFFICIAL.SAMPLE_INFO";
    private static Properties connectionProperties = null;
    static {
        connectionProperties = new Properties();
        connectionProperties.setProperty(QueryServices.MAX_MUTATION_SIZE_ATTRIB, "500000"); //commit或rollback前，一次批量处理的最大的行数,默认500000
        connectionProperties.setProperty(QueryServices.IS_NAMESPACE_MAPPING_ENABLED, "true"); //开启schema与namespace的对应关系
        connectionProperties.setProperty(QueryServices.ZOOKEEPER_QUORUM_ATTRIB, "192.168.1.118,192.168.1.119,192.168.1.120:2181"); //Zookeeper URL
        connectionProperties.setProperty("skipNormalizingIdentifier", "true"); //跳过规范化检查
    }
    public static void main(String[] args) throws IOException {
        String filePath = "C:\\Users\\HR\\Desktop\\pressureTestData\\id_3000_2.csv";
        File file = new File(filePath);
        int numThreads = 4;
        long fileSize = fileCnt(file) +1 ;
        System.out.println(fileSize);
        long chunkSize = fileSize / numThreads;
        long remainSize = fileSize % numThreads; //不足一批的数量

        for (int i = 0; i < numThreads; i++) {
            long start = i * chunkSize;
            long end = (i + 1) * chunkSize - 1;

            if (i == numThreads - 1) {
                end += remainSize;
            }
            System.out.println("批次" +i);
            System.out.println(start+"--"+end);
            printFile(file,start,end);

        }
    }

    public static long fileCnt(File file) {
        long cnt = 0L;
        try {
            if (file.exists()) {
                long fileLen = file.length();
                LineNumberReader lineNumberReader = new LineNumberReader(new FileReader(file));
                lineNumberReader.skip(fileLen);
                cnt = lineNumberReader.getLineNumber();
                lineNumberReader.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return cnt;
    }

    public static void printFile(File file, long start, long end) throws IOException {
        StopWatch watch = new StopWatch();
        Connection conn = null;
        FileReader fileReader = null;
        BufferedReader bufferedReader = null;
        long rowCnt = 0L;
        StringBuffer stringBuffer = new StringBuffer("SELECT * FROM "+ TBL +" WHERE \"rk\" =");
        // 读文件
        try {

            fileReader = new FileReader(file);
            bufferedReader = new BufferedReader(fileReader);
            String str;
            while ((str = bufferedReader.readLine()) != null) {
                if (rowCnt >= start && rowCnt <= end){
                    // 拼接sql
                    if (rowCnt == start){
                        stringBuffer.append("'".concat(str).concat("'"));
                    }else {
                        stringBuffer.append(" OR \"rk\"='".concat(str).concat("'"));
                    }
                    if (rowCnt == end){
                        System.out.println(stringBuffer);
                        // 查询数据库
                        watch.start();
                        conn = DriverManager.getConnection(PHOENIX_JDBC_URL,connectionProperties);
                        watch.stop();
                        System.out.println(watch.getTime());
                        getData(conn,stringBuffer.toString());
                    }
                }
                rowCnt ++;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            bufferedReader.close();
            fileReader.close();
            try {
                if (conn!=null) conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            stringBuffer.setLength(0);
        }
    }

    public static void getData(Connection conn,String sql) {
        System.out.println("开始获取数据库数据");
        PreparedStatement preparedStatement = null;
        JSONObject jsonObject = new JSONObject();
        try {
            Class.forName(PHOENIX_JDBC_DRIVER);
            preparedStatement = conn.prepareStatement(sql);
            ResultSet rs = preparedStatement.executeQuery();
            ResultSetMetaData metaData = rs.getMetaData();
            while (rs.next()) {
                for (int i = 1; i < metaData.getColumnCount(); i++) {
                    jsonObject.put(metaData.getColumnName(i),rs.getObject(i));
                }
                System.out.println(jsonObject);
            }
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        } finally {
            if (preparedStatement != null) {
                try {
                    preparedStatement.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            jsonObject.clear();
        }
    }
}
