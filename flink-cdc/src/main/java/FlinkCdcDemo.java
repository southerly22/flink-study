import com.ververica.cdc.connectors.mysql.source.MySqlSource;
import com.ververica.cdc.connectors.mysql.table.StartupOptions;
import com.ververica.cdc.debezium.JsonDebeziumDeserializationSchema;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.configuration.RestOptions;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

/**
 * @author lzx
 * @date 2023/2/20 10:03
 * @description: TODO flink mysql cdc
 */
public class FlinkCdcDemo {
    public static void main(String[] args) throws Exception {
        Configuration conf = new Configuration();
        conf.setString(RestOptions.BIND_PORT, "8081"); // 指定访问端口

        // 1.Flink-CDC将读取binlog的位置信息以状态的方式保存在CK,如果想要做到断点续传,需要从Checkpoint或者Savepoint启动程序
        // 设置 ck每 5s做一次
        // env.enableCheckpointing(5000L);
        //
        // // 3.设置ck 的一致性语义
        // env.getCheckpointConfig().setCheckpointingMode(CheckpointingMode.EXACTLY_ONCE);
        //
        // // 4.设置任务关闭时 保存最后一次ck 数据
        // env.getCheckpointConfig().enableExternalizedCheckpoints(CheckpointConfig.ExternalizedCheckpointCleanup.RETAIN_ON_CANCELLATION);
        //
        // // 5.指定从 ck 自动重启策略
        // env.setRestartStrategy(RestartStrategies.fixedDelayRestart(3,2000L));
        //
        // // 6.设置状态后端
        // env.setStateBackend(new FsStateBackend("hdfs://hadoop102:8020/flinkCDC"));
        //
        // System.setProperty("HADOOP_USER_NAME","hadoop");

        // 创建 flink-mysql-cdc
        MySqlSource<String> mysqlSource = MySqlSource.<String>builder()
                .hostname("localhost")
                .port(3306)
                .serverTimeZone("UTC")
                // .scanNewlyAddedTableEnabled(true) // 启用扫描新添加的表功能
                .username("root")
                .password("123456")
                .databaseList("test")
                .tableList("test.student") //使用"db.table"的方式
                .startupOptions(StartupOptions.initial())
                .deserializer(new JsonDebeziumDeserializationSchema()) //反序列化器
                .build();

        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment(conf);

        env.enableCheckpointing(3000);
        env.getCheckpointConfig().setCheckpointStorage("file:///D:\\WorkPlace\\flink-study\\flink-cdc\\src\\ck");
        // 使用cdcSource 从mysql 读取数据
        env.fromSource(mysqlSource, WatermarkStrategy.noWatermarks(), "mysql_source")
                .setParallelism(4)
                .print().setParallelism(1);

        env.execute();
    }
}
