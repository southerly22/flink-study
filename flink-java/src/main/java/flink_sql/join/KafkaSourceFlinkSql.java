package flink_sql.join;

import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.table.api.bridge.java.StreamTableEnvironment;

import java.time.ZoneId;

/**
 * kafka source
 *
 * @author lzx
 * @date 2023/04/19 10:12
 **/
public class KafkaSourceFlinkSql {
    public static void main(String[] args) {
        Configuration conf = new Configuration();
        conf.setInteger("rest.port",8085);

        // 可以基于现有的 StreamExecutionEnvironment 创建 StreamTableEnvironment 来与 DataStream API 进行相互转换
        StreamExecutionEnvironment env = StreamExecutionEnvironment.createLocalEnvironmentWithWebUI(conf);
        StreamTableEnvironment tEnv = StreamTableEnvironment.create(env);

        // 指定国内时区
        tEnv.getConfig().setLocalTimeZone(ZoneId.of("Asia/Shanghai"));

        // 创建输入表

        String inTable = "CREATE TABLE kafka_source( \n" +
                "    name STRING,\n" +
                "    age INT\n" +
                "    ) WITH( \n" +
                "        'connector' = 'kafka',\n" +
                "        'topic'='kafka_source',\n" +
                "        'properties.bootstrap.servers'='localhost:9094,localhost:9092,localhost:9093',\n" +
                "        'properties.group.id' = 'gid-sql-kafkasource',\n" +
                "        'scan.startup.mode' = 'latest-offset',\n" +
                "        'format' = 'json',\n" +
                "        'json.fail-on-missing-field' = 'false',\n" +
                "        'json.ignore-parse-errors' = 'true'\n" +
                "    )";
        tEnv.executeSql(inTable);

        // 创建输出表
        String outTable = "CREATE TABLE kafka_sink( \n" +
                "    age INT NOT NULL,\n" +
                "    cnt BIGINT,\n" +
                "    PRIMARY KEY(age) NOT ENFORCED\n" +
                "    ) WITH( \n" +
                "        'connector' = 'upsert-kafka',\n" +
                "        'topic'='kafka_source_out',\n" +
                "        'properties.bootstrap.servers'='localhost:9094,localhost:9092,localhost:9093',\n" +
                "        'key.format' = 'json',\n" +
                "        'value.format' = 'json'\n" +
                "    )";
        tEnv.executeSql(outTable);

        // 结果计算
        String res = "insert into kafka_sink\n" +
                "select age,count(1) as cnt \n" +
                "from kafka_source\n" +
                "group by age";
        tEnv.executeSql(res);
    }
}
