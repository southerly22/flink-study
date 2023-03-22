package IODemos;

import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.time.StopWatch;

import java.io.*;
import java.sql.*;
import java.util.Random;

/**
 * @author lzx
 * @date 2023/3/22 14:12
 * @description: TODO
 */
public class test {
    private static final String JDBC_DRIVER = "com.mysql.cj.jdbc.Driver";
    private static final String DB_URL = "jdbc:mysql://192.168.1.73:9030/sample_data_warehouse?rewriteBatchedStatements=true";
    private static final String TBL = "DIM_SAMPLE_INFO";
    private static final String USER = "root";
    private static final String PASSWD = "000000";

    public static void main(String[] args) throws IOException {
        String filePath = "C:\\Users\\HR\\Desktop\\javaRead.txt";
        File file = new File(filePath);
        int numThreads = 4;
        long fileSize = fileCnt(file);
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
        StringBuffer stringBuffer = new StringBuffer("SELECT * FROM DIM_SAMPLE_INFO_CJ WHERE sha1=");
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
                        stringBuffer.append(" OR sha1='".concat(str).concat("'"));
                    }
                    if (rowCnt == end){
                        System.out.println(stringBuffer);
                        // 查询数据库
                        watch.start();
                        conn = DriverManager.getConnection(DB_URL,USER,PASSWD);
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
                conn.close();
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
            Class.forName(JDBC_DRIVER);
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
