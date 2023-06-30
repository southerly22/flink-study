package flink_sql.leftJoin;

import org.apache.flink.streaming.api.CheckpointingMode;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.sink.SinkFunction;
import org.apache.flink.table.api.DataTypes;
import org.apache.flink.table.api.Schema;
import org.apache.flink.table.api.bridge.java.StreamTableEnvironment;
import org.apache.flink.types.Row;

/**
 * @author lzx
 * @date 2023/6/29 13:21
 * @description: TODO left join回撤流
 */
public class LeftJoin2 {
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

        // DataStream<Tuple2<Boolean, Row>> resDS1 = tEnv.toRetractStream(
        //         tEnv.sqlQuery(
        //                 "  SELECT  \n"+
        //                         "      show_log_table.log_id as s_id,  \n"+
        //                         "      show_log_table.show_params as s_params,  \n"+
        //                         "      click_log_table.log_id as c_id,  \n"+
        //                         "      click_log_table.click_params as c_params  \n"+
        //                         "  FROM show_log_table  \n"+
        //                         "  LEFT JOIN click_log_table ON show_log_table.log_id = click_log_table.log_id "
        //         ),
        //         TypeInformation.of(Row.class)
        // );
        //
        // resDS1.addSink(new SinkFunction<Tuple2<Boolean, Row>>() {
        //     @Override
        //     public void invoke(Tuple2<Boolean, Row> value, Context context) throws Exception {
        //         if (value.f0){
        //             System.out.println("rowKind-->" + value.f1.getKind().shortString());
        //             System.out.println("rowKind-->" + value.f1.getKind().toByteValue());
        //             // 处理UPDATE消息
        //             // value.f1 是一个 Row，表示新的结果
        //             // 在这里，你可以将新的结果写入数据库
        //             System.out.println("UPDATE_AFTER-->" + value.f1.toString());
        //         }else{
        //             // 处理delete消息
        //             // value.f1 是一个 Row，表示需要被撤销的旧结果
        //             // 在这里，你可以从数据库中删除旧的结果
        //             System.out.println("rowKind-->" + value.f1.getKind().shortString());
        //             System.out.println("delete-->" + value.f1);
        //         }
        //     }
        // });

        DataStream<Row> resDS2 = tEnv.toChangelogStream(
                tEnv.sqlQuery(
                        "  SELECT  \n" +
                                "      show_log_table.log_id as s_id,  \n" +
                                "      show_log_table.show_params as s_params,  \n" +
                                "      click_log_table.log_id as c_id,  \n" +
                                "      click_log_table.click_params as c_params  \n" +
                                "  FROM show_log_table  \n" +
                                "  LEFT JOIN click_log_table ON show_log_table.log_id = click_log_table.log_id "
                ),
                Schema.newBuilder()
                        .column("s_id", DataTypes.BIGINT())
                        .column("s_params", DataTypes.STRING())
                        .column("c_id", DataTypes.BIGINT())
                        .column("c_params", DataTypes.STRING())
                        .build()
        );

        resDS2.addSink(new SinkFunction<Row>() {
            @Override
            public void invoke(Row value, Context context) throws Exception {
                if (value.getKind().toByteValue() == 0){
                    // update
                    System.out.println("update-->" + value);
                }else if(value.getKind().toByteValue() == 2){

                    //update_after
                    System.out.println("update_after-->" + value);
                }
                else {
                    // delete
                    System.out.println("delete-->" + value);
                }
            }
        });

        env.execute();
    }
}
