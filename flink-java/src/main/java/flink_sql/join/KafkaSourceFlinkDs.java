package flink_sql.join;

import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.table.api.Schema;
import org.apache.flink.table.api.Table;
import org.apache.flink.table.api.bridge.java.StreamTableEnvironment;
import org.apache.flink.table.connector.ChangelogMode;
import org.apache.flink.types.Row;

import java.time.ZoneId;

/**
 * 将FlinkSql的结果 转换为 DataStream数据流
 *
 * @author lzx
 * @date 2023/04/19 10:12
 **/
public class KafkaSourceFlinkDs {
    public static void main(String[] args) throws Exception {
        Configuration conf = new Configuration();
        conf.setInteger("rest.port", 8085);

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


        // 结果计算  // groupby 在回撤老数据时采用的是-U  join用的是 -D
        String res = "select age,count(1) as cnt \n" +
                "from kafka_source\n" +
                "group by age";

        // 转换流
        Table resTable = tEnv.sqlQuery(res);
        DataStream<Row> resDS = tEnv.toChangelogStream(resTable,
                Schema.newBuilder().build(),
                ChangelogMode.all()
        );

        // 打印
        resDS.print();

        // 执行
        env.execute("flinkSql_2_flinkDs");

    }
}
