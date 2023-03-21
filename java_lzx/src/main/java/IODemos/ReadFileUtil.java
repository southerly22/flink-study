package IODemos;

import org.apache.commons.lang3.time.StopWatch;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.phoenix.query.QueryServices;

import java.io.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * @author lzx
 * @date 2023/3/21 11:13
 * @description: TODO java 按照批次读文件 -> 拼接sql -> 批次查询数据库 -> 收集所有结果写文件
 */
public class ReadFileUtil {
    private static final Log logger = LogFactory.getFactory().getInstance(ReadFileUtil.class);
    private static final int BATCH_SIZE = 200;
    private static String PHOENIX_JDBC_URL  = "jdbc:phoenix:192.168.1.118:2181";
    private static String PHOENIX_JDBC_DRIVER  = "org.apache.phoenix.jdbc.PhoenixDriver";

    public static void main(String[] args) throws SQLException {

        String filePath = "C:\\Users\\HR\\Desktop\\sha1";

        ReadFileUtil readFileUtil = new ReadFileUtil();

        readFileUtil.fileReadAndSave(filePath);
    }

    //主要逻辑
    public void fileReadAndSave(String filePath) throws SQLException {
        String sql = "SELECT \"id\" FROM OFFICIAL.SAMPLE_INFO WHERE \"rk\" =";
        StringBuilder builder = new StringBuilder(sql);

        List<String> resList = new ArrayList<>();

        Connection conn = DriverManager.getConnection(PHOENIX_JDBC_URL,getPor());
        FileReader fileReader;
        BufferedReader bufferedReader;
        try {
            int startNum = 0; //记录行数
            fileReader = new FileReader(filePath);
            bufferedReader = new BufferedReader(fileReader);
            String s;
            //判断是否到一行字符串
            while ((s = bufferedReader.readLine()) != null) {
                startNum++;
                if (startNum ==1){
                    builder.append("'".concat(s).concat("'"));
                }else {
                    builder.append(" OR \"rk\"='".concat(s).concat("'"));
                }
                if (startNum == BATCH_SIZE){
                    // 数据库查询
                    logger.info("数据库查询");
                    List<String> data = getData(builder.toString(), conn);
                    resList.addAll(data);
                    // System.out.println(builder);
                    // 清空builder
                    builder.setLength(0);
                    builder.append(sql);
                    startNum = 0;
                }
            }
            //关闭 IO流
            logger.info("关闭 IO流");
            bufferedReader.close();
            fileReader.close();

            //批次不足的 也查
            if (startNum > 0){
                // 查询数据库
                List<String> data = getData(builder.toString(), conn);
                resList.addAll(data);
                System.out.println("批次不足");
                System.out.println(builder);
            }

            //写文件
            logger.info("开始写文件...");
            writeFile(resList,"C:\\Users\\HR\\Desktop\\javaOutLong.txt");
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            if (conn!=null){
                conn.close();
            }
        }
    }

    //读取数据库 获取结果
    public List<String> getData(String sql , Connection conn){
        List<String> list = new ArrayList<>();
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        PreparedStatement preparedStatement =null;
        try {
            Class.forName(PHOENIX_JDBC_DRIVER);
            preparedStatement = conn.prepareStatement(sql);
            ResultSet rs = preparedStatement.executeQuery();
            while (rs.next()) {
                long res = rs.getLong(1);
                if (res != 0) {
                    list.add (res + "");
                }
            }
            stopWatch.stop();
            logger.info("批次 查询耗时："+stopWatch.getTime());
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }finally {
            if (preparedStatement!=null){
                try {
                    preparedStatement.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
        return list;
    }

    //数据库参数
    public Properties getPor(){
        Properties connectionProperties = new Properties();
        connectionProperties.setProperty(QueryServices.MAX_MUTATION_SIZE_ATTRIB, "500000"); //commit或rollback前，一次批量处理的最大的行数,默认500000
        connectionProperties.setProperty(QueryServices.IS_NAMESPACE_MAPPING_ENABLED, "true"); //开启schema与namespace的对应关系
        connectionProperties.setProperty(QueryServices.ZOOKEEPER_QUORUM_ATTRIB, "192.168.1.118,192.168.1.119,192.168.1.120:2181"); //Zookeeper URL
        connectionProperties.setProperty("skipNormalizingIdentifier", "true"); //跳过规范化检查
        return connectionProperties;
    }

    // 写文件
    public void writeFile(List<String> list,String filePath){
        StopWatch watch = new StopWatch();
        watch.start();
        BufferedWriter bufferedWriter;
        try {
            bufferedWriter = new BufferedWriter(new FileWriter(filePath));
            for (int i = 0; i < list.size(); i++) {
                bufferedWriter.write(list.get(i));
                bufferedWriter.newLine();
                bufferedWriter.flush();
            }
            bufferedWriter.close();
            watch.stop();
            logger.info("写文件完成,写文件耗时："+watch.getTime());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
