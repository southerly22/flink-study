package huorong;


import org.apache.phoenix.query.QueryServices;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * phoenix连接池 自定义的 测试 不准
 *
 * @author lzx
 * @date 2023/05/29 15:14
 **/
public class PhoenixConnPool {
    Logger logger = LoggerFactory.getLogger(PhoenixConnPool.class);
    private static Properties prop;
    private static final String driverClass = "org.apache.phoenix.jdbc.PhoenixDriver";
    private static final String jdbcUrl = "jdbc:phoenix:192.168.1.118:2181";

    static {
        prop = new Properties();
        prop.setProperty(QueryServices.IS_NAMESPACE_MAPPING_ENABLED, "true");
        prop.setProperty(QueryServices.AUTO_COMMIT_ATTRIB, "false");
        prop.setProperty(QueryServices.ZOOKEEPER_QUORUM_ATTRIB, "192.168.1.118,192.168.1.119,192.168.1.120:2181");
        prop.setProperty(QueryServices.KEEP_ALIVE_MS_ATTRIB, "600000");
    }


    private int min; // The number of connections of initial pool
    private int max;
    private int use;
    private Queue<Connection> pool = null;


    public PhoenixConnPool(int min, int use,int max) {
        this.min = min;
        this.max = max;
        this.use = use;
        this.pool = new ConcurrentLinkedQueue<Connection>();
        init();
    }

    public void init() {
        if (min < max) {
            try {
                Class.forName(driverClass);
                for (int i = 0; i < use; i++) {
                    Connection conn = null;
                    try {
                        conn = DriverManager.getConnection(jdbcUrl, prop);
                        pool.add(conn);
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        } else {
            throw new IllegalArgumentException("min is bigger than max!");
        }
    }

    public Connection getConn() {
        Connection conn = null;
        if (use > min){
            conn = pool.poll();
            use--;
        } else if (pool.size() > 0 && use <= max) {
            conn = pool.poll();
            new Thread(() -> {
                try {
                    Connection newConn = DriverManager.getConnection(jdbcUrl, prop);
                    pool.add(newConn);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }).start();
            use++;
        } else if (use > max) {
            System.out.println("没有资源再去创建链接了！");
        }
        return conn;
    }

    public void realease(Connection conn) {
        if (conn != null) {
            try {
                conn.close();
                use--;
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public void destroyPool() {
        while (pool.size() > 0) {
            try {
                pool.poll().close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        logger.warn("destroyPool,pool size=" + pool.size());
    }
}
