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
import org.apache.flink.runtime.state.hashmap.HashMapStateBackend;
import org.apache.flink.streaming.api.CheckpointingMode;
import org.apache.flink.streaming.api.environment.CheckpointConfig;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import utils.CustomStringDebeziumDeserializationSchema;

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
        env.getCheckpointConfig().setCheckpointStorage("file:///D:\\WorkPlace\\flink-study\\flink-cdc\\src\\ck");
        env.getCheckpointConfig().setTolerableCheckpointFailureNumber(3);//容忍 ck 失败最大次数

        // 创建 flink-mysql-cdc
        MySqlSource<String> mysqlSource = MySqlSource.<String>builder()
                .hostname("localhost")
                .port(3306)
                // .serverTimeZone("UTC")
                // .scanNewlyAddedTableEnabled(true) // 启用扫描新添加的表功能
                .username("root")
                .password("123456")
                .databaseList("leet")
                .tableList("leet.exam_record_cdc") //使用"db.table"的方式
                .startupOptions(StartupOptions.initial())
                .deserializer(new JsonDebeziumDeserializationSchema())
                // .deserializer(new CustomStringDebeziumDeserializationSchema(3306,"localhost")) //反序列化器
                .build();

        env.fromSource(mysqlSource, WatermarkStrategy.noWatermarks(), "mysql-Cdc")
                .map(JSON::parseObject)
                .map(json->{
                    return json.getJSONObject("after") +","+json.getString("op");
                })
                .print();

        env.execute();
    }
}
