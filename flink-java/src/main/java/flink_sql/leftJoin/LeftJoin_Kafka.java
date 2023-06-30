package flink_sql.leftJoin;

import org.apache.flink.streaming.api.CheckpointingMode;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.table.api.bridge.java.StreamTableEnvironment;

/**
 * @author lzx
 * @date 2023/6/29 14:48
 * @description: TODO
 */
public class LeftJoin_Kafka {
    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(1);
        env.enableCheckpointing(3000, CheckpointingMode.EXACTLY_ONCE);
        env.getCheckpointConfig().setCheckpointStorage("file:///D:\\WorkPlace\\flink-study\\flink-java\\ck");
        StreamTableEnvironment tEnv = StreamTableEnvironment.create(env);

        tEnv.executeSql(
                "  create table show_log_table(  \n" +
                        "  	log_id BIGINT,  \n" +
                        "  	show_params STRING  \n" +
                        "  )WITH(  \n" +
                        "  	'connector' = 'datagen',  \n" +
                        "  	'rows-per-second' = '1',  \n" +
                        "  	'fields.show_params.length' = '3',  \n" +
                        "  	'fields.log_id.min' = '1',  \n" +
                        "  	'fields.log_id.max' = '50'  \n" +
                        "  )  "
        );

        tEnv.executeSql(
                "  CREATE TABLE click_log_table (  \n" +
                        "    log_id BIGINT,  \n" +
                        "    click_params     STRING  \n" +
                        "  )  \n" +
                        "  WITH (  \n" +
                        "    'connector' = 'datagen',  \n" +
                        "    'rows-per-second' = '1',  \n" +
                        "    'fields.click_params.length' = '3',  \n" +
                        "    'fields.log_id.min' = '1',  \n" +
                        "    'fields.log_id.max' = '50'  \n" +
                        "  )  "
        );

        // 输出到 kafka
        tEnv.executeSql(
                "  CREATE TABLE sink_table (  \n" +
                        "      id BIGINT,  \n" +
                        "      s_params STRING,  \n" +
                        "      c_params STRING,  \n" +
                        "      PRIMARY KEY(id) NOT ENFORCED \n" + // 回撤流要有主键
                        "    ) WITH( \n" +
                        "        'connector' = 'upsert-kafka',\n" + //left join有回撤流，只可以使用upsert-kafka，同时还要指定key的format
                        "        'topic'='test0629',\n" +
                        "        'properties.bootstrap.servers'='192.168.1.56:9092,192.168.1.61:9092,192.168.1.58:9092,192.168.3.71:9092,192.168.3.178:9092',\n" +
                        "        'key.format' = 'json',\n" +
                        "        'value.format' = 'json'\n" +
                        "    )"
        );

        // 往kafka写数据
        tEnv.executeSql(
                "  INSERT INTO sink_table  \n"+
                        "  SELECT  \n"+
                        "      show_log_table.log_id as id,  \n"+
                        "      show_log_table.show_params as s_params,  \n"+
                        "      click_log_table.click_params as c_params  \n"+
                        "  FROM show_log_table  \n"+
                        "  LEFT JOIN click_log_table ON show_log_table.log_id = click_log_table.log_id "
        );

        tEnv.executeSql("select * from sink_table").print();
    }
}
