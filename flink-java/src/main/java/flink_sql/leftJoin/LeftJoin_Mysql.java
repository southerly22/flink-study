package flink_sql.leftJoin;

import org.apache.flink.streaming.api.CheckpointingMode;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.table.api.bridge.java.StreamTableEnvironment;

/**
 * @author lzx
 * @date 2023/6/29 13:21
 * @description: TODO left join回撤流
 */
public class LeftJoin_Mysql {
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
                        "  	'fields.log_id.max' = '10'  \n" +
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
                        "    'fields.log_id.max' = '10'  \n" +
                        "  )  "
        );

        // 创建 jdbc表
        tEnv.executeSql(
                " CREATE TABLE sink_table (  \n"+
                        "   s_id bigint,  \n"+
                        "   s_params STRING,  \n"+
                        "   a_params STRING,  \n"+
                        "   PRIMARY KEY (s_id) NOT ENFORCED  \n"+
                        " ) WITH (  \n"+
                        "    'connector' = 'jdbc',  \n"+
                        "    'url' = 'jdbc:mysql://localhost:3306/test',  \n"+
                        "    'password' = '123456',  \n"+
                        "    'username' = 'root',  \n"+
                        "    'table-name' = 'sink_table'  \n"+
                        " ) "
        );

        // 执行插入操作
        tEnv.executeSql(
                "INSERT INTO sink_table \n " +
                "  SELECT  \n" +
                        "      show_log_table.log_id as s_id,  \n" +
                        "      show_log_table.show_params as s_params,  \n" +
                        "      click_log_table.click_params as c_params  \n" +
                        "  FROM show_log_table  \n" +
                        "  LEFT JOIN click_log_table ON show_log_table.log_id = click_log_table.log_id "
        );


    }
}
