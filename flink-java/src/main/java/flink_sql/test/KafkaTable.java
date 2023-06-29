package flink_sql.test;

import org.apache.flink.contrib.streaming.state.EmbeddedRocksDBStateBackend;
import org.apache.flink.streaming.api.CheckpointingMode;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.table.api.bridge.java.StreamTableEnvironment;

/**
 * @author lzx
 * @date 2023/6/29 14:55
 * @description: TODO
 */
public class KafkaTable {
    public static void main(String[] args) {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(1);
        env.enableCheckpointing(3000, CheckpointingMode.EXACTLY_ONCE);
        env.getCheckpointConfig().setCheckpointStorage("file:///D:\\WorkPlace\\flink-study\\flink-java\\ck");
        StreamTableEnvironment tEnv = StreamTableEnvironment.create(env);

        String paymentFlowTableSql =
                "CREATE TABLE sink_table (  \n" +
                "      id BIGINT,  \n" +
                "      s_params STRING,  \n" +
                "      c_params STRING \n" +
                "    ) WITH( \n" +
                "        'connector' = 'kafka',\n" +
                "        'topic'='test0629',\n" +
                "        'properties.bootstrap.servers'='192.168.1.56:9092,192.168.1.61:9092,192.168.1.58:9092,192.168.3.71:9092,192.168.3.178:9092',\n" +
                "        'properties.group.id' = 'gid0629',\n" +
                "        'scan.startup.mode' = 'earliest-offset',\n" +
                "        'format' = 'json',\n" +
                "        'json.fail-on-missing-field' = 'false',\n" +
                "        'json.ignore-parse-errors' = 'true'\n" +
                "    )";

        tEnv.executeSql(paymentFlowTableSql);
        tEnv.executeSql("select * from sink_table").print();
    }
}
