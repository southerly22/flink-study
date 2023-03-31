package IODemos;

import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.time.StopWatch;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.phoenix.query.QueryServices;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Properties;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author lzx
 * @date 2023/3/22 14:53
 * @description: TODO 多线程读文件 3000条数据,10个线程每个线程跑300条数据，去查数据库
 */
public class MultiThreadReadPhoenix {
    private static final Log logger = LogFactory.getFactory().getInstance(MultiThreadReadPhoenix.class);
    private static String PHOENIX_JDBC_URL = "jdbc:phoenix:192.168.1.118:2181";
    private static String PHOENIX_JDBC_DRIVER  = "org.apache.phoenix.jdbc.PhoenixDriver";
    private static final String TBL = "OFFICIAL.SAMPLE_INFO";
    private static Properties connectionProperties;
    static {
        connectionProperties = new Properties();
        connectionProperties.setProperty(QueryServices.MAX_MUTATION_SIZE_ATTRIB, "500000"); //commit或rollback前，一次批量处理的最大的行数,默认500000
        connectionProperties.setProperty(QueryServices.IS_NAMESPACE_MAPPING_ENABLED, "true"); //开启schema与namespace的对应关系
        connectionProperties.setProperty(QueryServices.ZOOKEEPER_QUORUM_ATTRIB, "192.168.1.118,192.168.1.119,192.168.1.120:2181"); //Zookeeper URL
        connectionProperties.setProperty("skipNormalizingIdentifier", "true"); //跳过规范化检查
    }
    private static int bufferSize = 1024 *1024;
    public static void main(String[] args) throws IOException {

        String filePath = "C:\\Users\\HR\\Desktop\\pressureTestData\\sha1_3000_3.csv";
        File file = new File(filePath);

        FileChannel outChannel = FileChannel.open(Paths.get("C:\\Users\\HR\\Desktop\\JavaIO\\result.txt"), StandardOpenOption.CREATE,StandardOpenOption.WRITE);
        ByteBuffer outBuffer = ByteBuffer.allocate(bufferSize);

        long fileSize = fileCnt(file); // 总量
        logger.warn("数据量" + fileSize);
        int numThreads = 10; // 线程数量
        long perThreadSize = fileSize / numThreads; // 每个线程要处理的数量
        long remainSize = fileSize % numThreads;
        //创建线程池
        ExecutorService threadPool = Executors.newFixedThreadPool(numThreads);
            for (int i = 0; i < numThreads; i++) {
                long start = i * perThreadSize;
                long end = (i + 1) * perThreadSize - 1 ;
                //最后一个线程特殊处理
                if (i == numThreads - 1){
                    end += remainSize;
                }
                // 线程开始执行
                threadPool.execute(new FileReadTask(file, start, end,outChannel,outBuffer));
            }
        threadPool.shutdown();
    }

    // 读文件
    static class FileReadTask implements Runnable {
        private File file;
        private long start;
        private long end;
        private FileChannel fileChannel;
        private ByteBuffer byteBuffer;

        public FileReadTask(File file, long start, long end, FileChannel fileChannel, ByteBuffer byteBuffer) {
            this.file = file;
            this.start = start;
            this.end = end;
            this.fileChannel = fileChannel;
            this.byteBuffer = byteBuffer;
        }

        public FileReadTask(File file, long start, long end) {
            this.file = file;
            this.start = start;
            this.end = end;
        }

        @Override
        public void run() {
            String threadName = Thread.currentThread().getName();
            StopWatch watch = new StopWatch();
            watch.start();
            Connection conn = null;
            FileReader fileReader = null;
            BufferedReader bufferedReader = null;
            StringBuffer stringBuffer = new StringBuffer("SELECT * FROM " + TBL + " WHERE \"rk\"=");
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
                            stringBuffer.append(" OR \"rk\"='".concat(str).concat("'"));
                        }
                        if (rowCnt == end){
                            // System.out.println(stringBuffer);
                            // 查询数据库
                            conn = DriverManager.getConnection(PHOENIX_JDBC_URL,connectionProperties);
                            getData(stringBuffer.toString(), conn,fileChannel,byteBuffer);
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
    public static void getData(String sql, Connection conn,FileChannel fileChannel,ByteBuffer byteBuffer) {
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
        // 写入文件
        synchronized (byteBuffer){
            byteBuffer.clear();
            byteBuffer.put(jsonObject.toString().getBytes(StandardCharsets.UTF_8));
            byteBuffer.flip();
            try {
                fileChannel.write(byteBuffer);
            } catch (IOException e) {
                System.out.println("文件写入失败! "+e.getMessage());
            }
        }
        logger.warn("文件写入成功！");
    }
}
