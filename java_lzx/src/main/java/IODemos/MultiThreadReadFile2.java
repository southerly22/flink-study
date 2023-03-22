package IODemos;

import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.time.StopWatch;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author lzx
 * @date 2023/3/22 14:53
 * @description: TODO 多线程读文件  fileReader
 */
public class MultiThreadReadFile2 {
    private static final Log logger = LogFactory.getFactory().getInstance(MultiThreadReadFile2.class);
    private static final String JDBC_DRIVER = "com.mysql.cj.jdbc.Driver";
    private static final String DB_URL = "jdbc:mysql://192.168.1.73:9030/sample_data_warehouse?rewriteBatchedStatements=true&query_timeout=600";
    private static final String TBL = "DIM_SAMPLE_INFO";
    private static final String USER = "root";
    private static final String PASSWD = "000000";

    public static void main(String[] args) {

        String filePath = "C:\\Users\\HR\\Desktop\\pressureTestData\\sha1.csv";

        File file = new File(filePath);

        long fileSize = fileCnt(file); // 总量
        logger.warn("数据量" + fileSize);
        int numThreads = 10; // 线程数量
        long perThreadSize = 200; // 每个线程要处理的数量
        long batchThread = fileSize / (numThreads * perThreadSize); // 线程要跑几轮

        long remainSize = fileSize % perThreadSize; //不足一批的数量

        //创建线程池
        ExecutorService threadPool = Executors.newFixedThreadPool(numThreads);
        for (long j = 0; j < batchThread; j++) {
            long mid = j * numThreads * perThreadSize;
            for (int i = 0; i < numThreads; i++) {
                long start = i * perThreadSize + mid;
                long end = (i + 1) * perThreadSize - 1 + mid;

                if (j == batchThread-1 && i == numThreads - 1) {
                    end += remainSize;
                }
                threadPool.execute(new FileReadTask(file, start, end));
            }
        }

        threadPool.shutdown();
    }

    // 读文件
    static class FileReadTask implements Runnable {
        private File file;
        private long start;
        private long end;

        public FileReadTask(File file, long start, long end) {
            this.file = file;
            this.start = start;
            this.end = end;
        }

        @Override
        public void run() {
            String threadName = Thread.currentThread().getName();
            logger.warn("当前运行的线程： " + threadName);
            StopWatch watch = new StopWatch();
            watch.start();
            Connection conn = null;
            FileReader fileReader = null;
            BufferedReader bufferedReader = null;
            StringBuffer stringBuffer = new StringBuffer("SELECT * FROM " + TBL + " WHERE sha1=");
            long rowCnt = 0L;
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
                            // System.out.println(stringBuffer);
                            // 查询数据库
                            conn = DriverManager.getConnection(DB_URL,USER,PASSWD);
                            getData(stringBuffer.toString(),conn);
                        }
                    }
                    rowCnt ++;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }finally {
                try {
                    bufferedReader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                try {
                    fileReader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                try {
                    if (conn!=null) conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
                stringBuffer.setLength(0);
            }
            watch.stop();
            logger.warn("线程：" +threadName+", 处理数据："+(end - start +1) +"条, 耗费时间："+watch.getTime()+"ms");
        }
    }

    // 获取文件行数
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

    //读取数据库 获取结果
    public static void getData(String sql, Connection conn) {
        logger.warn("获取数据库 数据。。。");
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
                // System.out.println(jsonObject);
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
        }
    }
}
