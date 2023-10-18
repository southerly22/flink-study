import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.ververica.cdc.connectors.mysql.source.MySqlSource;
import com.ververica.cdc.connectors.mysql.table.StartupOptions;
import com.ververica.cdc.debezium.JsonDebeziumDeserializationSchema;
import com.ververica.cdc.debezium.StringDebeziumDeserializationSchema;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.common.restartstrategy.RestartStrategies;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.connector.jdbc.JdbcConnectionOptions;
import org.apache.flink.connector.jdbc.JdbcExecutionOptions;
import org.apache.flink.connector.jdbc.JdbcSink;
import org.apache.flink.connector.jdbc.JdbcStatementBuilder;
import org.apache.flink.runtime.state.hashmap.HashMapStateBackend;
import org.apache.flink.streaming.api.CheckpointingMode;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.CheckpointConfig;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.sink.SinkFunction;
import utils.CustomStringDebeziumDeserializationSchema;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Properties;

/**
 * @author lzx
 * @date 2023/7/14 13:19
 * @description: TODO
 */
public class Mysql_Cdc_Doris {
    public static void main(String[] args) throws Exception {
        Configuration conf = new Configuration();
        conf.setInteger("rest.port",8085);
        StreamExecutionEnvironment env = StreamExecutionEnvironment.createLocalEnvironmentWithWebUI(conf);
        env.setParallelism(1);

        env.enableCheckpointing(3000, CheckpointingMode.EXACTLY_ONCE);
        env.getCheckpointConfig().setCheckpointStorage("file:///D:\\CodePlace_JH\\flink-study\\flink-cdc\\src\\ck");
//        env.getCheckpointConfig().setTolerableCheckpointFailureNumber(3);//容忍 ck 失败最大次数
//        env.setRestartStrategy(RestartStrategies.fixedDelayRestart(3,3000));

        Properties prop = new Properties();
        prop.setProperty("useSSL","false");

        // 创建 flink-mysql-cdc
        MySqlSource<String> mysqlSource = MySqlSource.<String>builder()
                .hostname("localhost")
                .port(3306)
//                 .serverTimeZone("UTC")
                 .scanNewlyAddedTableEnabled(true) // 启用扫描新添加的表功能
                .username("root")
                .password("123456")
                .jdbcProperties(prop)
                .databaseList("lzxtest")
                .tableList("lzxtest.test_cdc") //使用"db.table"的方式
                .startupOptions(StartupOptions.initial())
//                .startupOptions(StartupOptions.earliest())
//                .startupOptions(StartupOptions.latest())
                .deserializer(new JsonDebeziumDeserializationSchema())
                .build();

        SingleOutputStreamOperator<JSONObject> mysqlDs = env.fromSource(mysqlSource, WatermarkStrategy.noWatermarks(), "mysql-Cdc")
                .map(JSON::parseObject);
        // {"name":"测试flink CDC","id":1212}

        mysqlDs.addSink()

        mysqlDs.map(
                jsonObj -> {
                    String op = jsonObj.getString("op");
                    if ("c".equals(op)) { //插入
                        jsonObj.get("after");
                    } else if ("u".equals(op)) { //更新
//                jsonObj.getString("before")+","+jsonObj.getString("after");
                    } else if ("d".equals(op)) { //删除
                        jsonObj.getString("before");
                    }
                    return "";
                });

        // todo 构建普通 sink
        SinkFunction<EventLog> commonSink = JdbcSink.sink(
                "insert into EventLog values (?,?,?,?,?);",
                new JdbcStatementBuilder<EventLog>() {
                    @Override
                    public void accept(PreparedStatement ps, EventLog e) throws SQLException {
                        ps.setLong(1, e.getGuid());
                        ps.setString(2, e.getSessionId());
                        ps.setString(3, e.getEventId());
                        ps.setTimestamp(4, new Timestamp(e.getTimeStamp()));
                        ps.setString(5, e.getEventInfo().toString());
                        System.out.println(ps);
                    }
                },
                JdbcExecutionOptions.builder()
                        .withBatchSize(10)
                        .withMaxRetries(0)
                        .build(),
                new JdbcConnectionOptions.JdbcConnectionOptionsBuilder()
                        .withUrl("jdbc:mysql://localhost:3306/test?useUnicode=true&characterEncoding=UTF-8")
                        .withDriverName("com.mysql.cj.jdbc.Driver")
                        .withUsername("root")
                        .withPassword("123456")
                        .build()
        );
        env.execute();
    }
}
