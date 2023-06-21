package dorisDemo;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.ververica.cdc.connectors.mysql.source.MySqlSource;
import com.ververica.cdc.debezium.JsonDebeziumDeserializationSchema;
import org.apache.doris.flink.cfg.DorisExecutionOptions;
import org.apache.doris.flink.cfg.DorisOptions;
import org.apache.doris.flink.cfg.DorisReadOptions;
import org.apache.doris.flink.sink.DorisSink;
import org.apache.doris.flink.sink.writer.SimpleStringSerializer;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.streaming.api.CheckpointingMode;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.ProcessFunction;
import org.apache.flink.util.Collector;
import org.apache.flink.util.OutputTag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

/**
 * @author lzx
 * @date 2023/6/21 14:44
 * @description: TODO Mysql 全库迁移 Doris
 */
public class DatabaseFullSync {
    private static String TABLE_A = "tableA";
    private static String TABLE_B = "tableB";
    private static OutputTag<String> tagA = new OutputTag<>(TABLE_A);
    private static OutputTag<String> tagB = new OutputTag<>(TABLE_B);
    private static final Logger logger = LoggerFactory.getLogger(DatabaseFullSync.class);

    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.enableCheckpointing(3000, CheckpointingMode.EXACTLY_ONCE);
        env.getCheckpointConfig().setCheckpointStorage("");

        MySqlSource<String> mySqlSource = MySqlSource.<String>builder()
                .hostname("")
                .port(3306)
                .databaseList("test")
                .tableList("test.*")
                .username("").password("")
                .deserializer(new JsonDebeziumDeserializationSchema())
                .build();

        DataStreamSource<String> cdcSource = env.fromSource(mySqlSource, WatermarkStrategy.noWatermarks(), "mysql-cdc-source");
        SingleOutputStreamOperator<String> process = cdcSource.process(new ProcessFunction<String, String>() {
            @Override
            public void processElement(String row, ProcessFunction<String, String>.Context ctx, Collector<String> out) throws Exception {
                JSONObject rowJson = JSON.parseObject(row);
                String op = rowJson.getString("op");
                JSONObject source = rowJson.getJSONObject("source");
                String table = source.getString("table");

                // 同步insert
                if ("c".equals(op)) {
                    String value = rowJson.getJSONObject("after").toJSONString();
                    if (TABLE_A.equals(table)) {
                        ctx.output(tagA, value);
                    } else if (TABLE_B.equals(table)) {
                        ctx.output(tagB, value);
                    } else {
                        logger.info("other operation....");
                    }
                }
            }
        });
        DataStream<String> tableADs = process.getSideOutput(tagA);
        DataStream<String> tableBDs = process.getSideOutput(tagB);

        // sink
        tableADs.sinkTo(buildDorisSink(TABLE_A));
        tableBDs.sinkTo(buildDorisSink(TABLE_B));

        env.execute();
    }
    public static DorisSink buildDorisSink(String table){
        DorisSink.Builder<String> builder = DorisSink.<String>builder();
        DorisOptions dorisOptions = DorisOptions.builder()
                .setFenodes("")
                .setTableIdentifier("")
                .setUsername("root")
                .setPassword("000000").build();

        Properties prop = new Properties();
        prop.setProperty("format","json");
        prop.setProperty("read_json_by_line","true");
        DorisExecutionOptions executionOptions = DorisExecutionOptions.builder()
                .setLabelPrefix("label-" + table)
                .setStreamLoadProp(prop).build();

        builder.setDorisReadOptions(DorisReadOptions.builder().build())
                .setDorisExecutionOptions(executionOptions)
                .setSerializer(new SimpleStringSerializer())
                .setDorisOptions(dorisOptions);

        return builder.build();
    }
}
