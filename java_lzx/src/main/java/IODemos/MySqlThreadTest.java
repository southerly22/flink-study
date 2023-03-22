package IODemos;

import java.sql.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author lzx
 * @date 2023/3/22 13:06
 * @description: TODO 多线程读文件 -> 拼接sql -> 查询数据库
 */
public class MySqlThreadTest {
    private static final String JDBC_URL = "";
    private static final String USER = "";
    private static final String PASSWD = "";

    public static void main(String[] args) {
        ExecutorService threadPool = Executors.newFixedThreadPool(10);

        //启动10个线程
        for (int i = 0; i < 10; i++) {
            threadPool.execute(new MySqlThread());
        }

        //关闭线程池
        threadPool.shutdown();
    }

    //线程方法
    static class MySqlThread implements Runnable {

        @Override
        public void run() {
            Connection conn = null;
            PreparedStatement stmt = null;
            ResultSet rs = null;
            try {
                //获取链接
                conn = DriverManager.getConnection(JDBC_URL,USER,PASSWD);

            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}

