package common;

/**
 * @author lzx
 * @date 2023/08/29 10:45
 * 全局 配置类
 **/
public class CommonConfig {
    // Phoenix库名
    public static final String HBASE_SCHEMA = "REALTIME";

    // Phoenix驱动
    public static final String PHOENIX_DRIVER = "org.apache.phoenix.jdbc.PhoenixDriver";

    // Phoenix连接参数
    public static final String PHOENIX_SERVER = "jdbc:phoenix:hadoop102,hadoop103,hadoop104:2181";

}
