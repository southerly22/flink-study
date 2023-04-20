package flink_core.hbase;

import org.apache.flink.api.common.ExecutionConfig;
import org.apache.flink.api.common.ExecutionMode;
import org.apache.flink.api.java.ExecutionEnvironment;
import org.apache.flink.streaming.api.CheckpointingMode;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.table.api.EnvironmentSettings;
import org.apache.flink.table.api.SqlDialect;
import org.apache.flink.table.api.Table;
import org.apache.flink.table.api.TableResult;
import org.apache.flink.table.api.bridge.java.StreamTableEnvironment;
import org.apache.flink.types.Row;

/**
 * @author lzx
 * @date 2023/4/13 16:04
 * @description: TODO flink拉取kafka数据 写入Hbase
 */
public class FlinkKafkaHbase {
    public static void main(String[] args) throws Exception {
        //获取 流式运行环境
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        ExecutionConfig config = env.getConfig();
        config.setExecutionMode(ExecutionMode.BATCH);
        env.enableCheckpointing(1000L*60, CheckpointingMode.EXACTLY_ONCE);
        env.getCheckpointConfig().setCheckpointStorage("file:///D:\\WorkPlace\\flink-study\\flink-java\\ck\\");

        //获取流式表运行环境 blink
        EnvironmentSettings settings = EnvironmentSettings.newInstance().useBlinkPlanner().build();
        StreamTableEnvironment tEnv = StreamTableEnvironment.create(env, settings);

        //获取kafka流
        tEnv.getConfig().setSqlDialect(SqlDialect.DEFAULT);
        tEnv.executeSql("drop table if exists KafkaSourceTable");

        String createSourceTable = "create table KafkaSourceTaskMapping(" +
                "taskId BigInt," +
                "taskType String," +
                "sha1 String," +
                "taskCreateTime BigInt" +
                ") WITH(" +
                "'connector'='kafka'," +
                "'topic'='task_mapping'," +
                "'properties.bootstrap.servers' = '192.168.1.189:9092'," +
                "'properties.group.id' = 'flink-test-group'," +
                "'format' = 'json'," +
                "'scan.startup.mode' = 'earliest-offset'," +
                "'json.ignore-parse-errors' = 'true'," +
                "'scan.startup.mode' = 'group-offsets'" +
                ")";
        Table resTable = tEnv.sqlQuery(createSourceTable);
        DataStream<Row> resStream = tEnv.toDataStream(resTable);
        resStream.print();
        env.execute();
    }
}
