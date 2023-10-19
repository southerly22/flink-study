package utils;

import com.alibaba.druid.pool.DruidDataSource;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * @author lzx
 * @date 2023/10/18 22:02
 **/
public class DruidPoolUtil {
    private static DruidDataSource druidDataSource =  null;

    public static DruidDataSource getDruidDataSource(String sourceName){
        String propFile = "";
        InputStream stream = null;

        if ("vertica".equals(sourceName)) {
            propFile = String.format("%s.%s",sourceName,"properties");
        }else if("mysql".equals(sourceName)){
            propFile = String.format("%s.%s",sourceName,"properties");
        }

        if (propFile.isEmpty()) {
            throw new IllegalArgumentException("propFile为空");
        }else {
            stream = DruidPoolUtil.class.getClassLoader().getResourceAsStream(propFile);
        }

        Properties prop = new Properties();
        try {
            prop.load(stream);
        } catch (IOException e) {
            e.printStackTrace();
        }

        String driver_name = prop.getProperty("DRIVER_NAME");
        String jdbc_url = prop.getProperty("JDBC_URL");
        String user_name = prop.getProperty("USER_NAME");
        String password = prop.getProperty("PASSWORD");

        druidDataSource = new DruidDataSource();

        druidDataSource.setDriverClassName(driver_name);
        druidDataSource.setUrl(jdbc_url);
        druidDataSource.setUsername(user_name);
        druidDataSource.setPassword(password);
        // 设置初始化连接池时池中连接的数量;
        druidDataSource.setInitialSize(1);
        // 设置同时活跃的最大连接数;
        druidDataSource.setMaxActive(3);
        // 设置空闲时的最小连接数，必须介于 0 和最大连接数之间，默认为 0;
        druidDataSource.setMinIdle(1);
        // 设置没有空余连接时的等待时间，超时抛出异常，-1 表示一直等待;
        druidDataSource.setMaxWait(-1);
        // 验证连接是否可用使用的 SQL 语句;
        druidDataSource.setValidationQuery("select 1");
        // 指明连接是否被空闲连接回收器（如果有）进行检验，如果检测失败，则连接将被从池中去除;
        // 注意，默认值为 true，如果没有设置 validationQuery，则报错;
        // testWhileIdle is true, validationQuery not set;
        druidDataSource.setTestWhileIdle(true);
        // 借出连接时，是否测试，设置为 false，不测试，否则很影响 性能;
        druidDataSource.setTestOnBorrow(false);
        // 归还连接时，是否测试;
        druidDataSource.setTestOnReturn(false);
        // 设置空闲连接回收器每隔 30s 运行一次;
        druidDataSource.setTimeBetweenEvictionRunsMillis(30 * 1000L);
        // 设置池中连接空闲 30min 被回收，默认值即为 30 min;
        druidDataSource.setMinEvictableIdleTimeMillis(30 * 60 * 1000L);
        return druidDataSource;
    }
}
