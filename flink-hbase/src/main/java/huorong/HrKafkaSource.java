package huorong;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.commons.lang3.time.StopWatch;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.restartstrategy.RestartStrategies;
import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.api.java.functions.KeySelector;
import org.apache.flink.api.java.tuple.Tuple;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.connector.jdbc.JdbcConnectionOptions;
import org.apache.flink.connector.jdbc.JdbcExecutionOptions;
import org.apache.flink.connector.jdbc.JdbcSink;
import org.apache.flink.connector.jdbc.JdbcStatementBuilder;
import org.apache.flink.connector.kafka.source.KafkaSource;
import org.apache.flink.connector.kafka.source.enumerator.initializer.OffsetsInitializer;
import org.apache.flink.runtime.state.hashmap.HashMapStateBackend;
import org.apache.flink.streaming.api.CheckpointingMode;
import org.apache.flink.streaming.api.datastream.AsyncDataStream;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.CheckpointConfig;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.ProcessFunction;
import org.apache.flink.streaming.api.functions.windowing.AllWindowFunction;
import org.apache.flink.streaming.api.functions.windowing.ProcessWindowFunction;
import org.apache.flink.streaming.api.functions.windowing.WindowFunction;
import org.apache.flink.streaming.api.windowing.assigners.TumblingEventTimeWindows;
import org.apache.flink.streaming.api.windowing.assigners.TumblingProcessingTimeWindows;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.streaming.api.windowing.windows.GlobalWindow;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.table.runtime.operators.window.assigners.CountTumblingWindowAssigner;
import org.apache.flink.util.Collector;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.TopicPartition;

import javax.validation.constraints.Digits;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author lzx
 * @date 2023/5/26 13:59
 * @description: TODO 构建kafka source
 */
public class HrKafkaSource {
    public static void main(String[] args) throws Exception {
        // 解析参数
        //ParameterTool parameterTool = ParameterTool.fromArgs(args);
        //String topic = parameterTool.get("t");
        //String gid = parameterTool.get("gid");
        //System.out.println(String.format("%s,%s", topic, gid));

        Configuration configuration = new Configuration();
        configuration.setInteger("rest.port", 8085);

        // 设置从检查点(ck) 恢复数据
        // configuration.setString("execution.savepoint.path", "D:\\WorkPlace\\flink-study\\flink-java\\ck\\82c266361f12ff1ced5ffb222d93de96\\chk-61");
        StreamExecutionEnvironment env = StreamExecutionEnvironment.createLocalEnvironmentWithWebUI(configuration);

        // 检查点
        env.enableCheckpointing(3000, CheckpointingMode.EXACTLY_ONCE);
        env.getCheckpointConfig().setCheckpointStorage("file:///D:\\WorkPlace\\flink-study\\flink-hbase\\ck");
        env.getCheckpointConfig().setTolerableCheckpointFailureNumber(3);//容忍 ck 失败最大次数
        env.getCheckpointConfig().setExternalizedCheckpointCleanup(CheckpointConfig.ExternalizedCheckpointCleanup.DELETE_ON_CANCELLATION);
        // 容错
        env.setRestartStrategy(RestartStrategies.noRestart());
        // 状态后端
        env.setStateBackend(new HashMapStateBackend());

        // 指定分区
        HashMap<TopicPartition, Long> topicMap = new HashMap<>();
        topicMap.put(new TopicPartition("task_mapping", 0), 129630118L);
        topicMap.put(new TopicPartition("task_mapping", 1), 129630606L);
        topicMap.put(new TopicPartition("task_mapping", 2), 129630453L);
        topicMap.put(new TopicPartition("task_mapping", 3), 129630300L);
        topicMap.put(new TopicPartition("task_mapping", 4), 129630667L);
        topicMap.put(new TopicPartition("task_mapping", 5), 129630380L);

        KafkaSource<String> kafkaSource = KafkaSource.<String>builder()
                .setBootstrapServers("192.168.1.56:9092,192.168.1.61:9092,192.168.1.58:9092,192.168.3.71:9092,192.168.3.178:9092")
                .setTopics("task_mapping")
                .setGroupId("test0526")
                .setProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "false")
                .setProperty("commit.offsets.on.checkpoint", "false") // 指定是否在进行 checkpoint 时将消费位点提交至 Kafka broker
                .setValueOnlyDeserializer(new SimpleStringSchema())
                // 如果没有可用的之前记录的offsets，则用OffsetResetStrategy.LATEST 读取最新的offsets 开始消费
                // .setStartingOffsets(OffsetsInitializer.committedOffsets(OffsetResetStrategy.EARLIEST))

                // 3:129630300;2:129630453;5:129630380;4:129630667;1:129630606;0:129630118
                .setStartingOffsets(OffsetsInitializer.offsets(topicMap))

                .build();

        DataStreamSource<String> kafkaStream = env.fromSource(kafkaSource, WatermarkStrategy.noWatermarks(), "kafkaSource");

        System.out.println("System.currentTimeMillis() = " + System.currentTimeMillis());

        SingleOutputStreamOperator<SampleTaskMappingInfo> filterDS = kafkaStream
                .setParallelism(6)
                .map(s -> JSON.parseObject(s, SampleTaskMappingInfo.class))
                .filter(sample -> "U".equals(sample.getTask_type()) && sample.getSha1() != null && !"".equals(sample.getSha1().trim()));

        // filterDS.print();

        SingleOutputStreamOperator<JSONObject> resDS = AsyncDataStream.unorderedWait(
                filterDS,
                new HrAsyncPhoenixFunc(),
                60,
                TimeUnit.SECONDS
        ).startNewChain();
        // countWindowAll 只有一个并行度
        // SingleOutputStreamOperator<List<JSONObject>> windowDS = resDS.countWindowAll(1000L).apply(new AllWindowFunction<JSONObject, List<JSONObject>, GlobalWindow>() {
        //     @Override
        //     public void apply(GlobalWindow window, Iterable<JSONObject> values, Collector<List<JSONObject>> out) throws Exception {
        //
        //         ArrayList<JSONObject> jsonObjects = Lists.newArrayList(values);
        //
        //         if (jsonObjects.size() > 0) {
        //             System.out.println("收集到的数据大小" + jsonObjects.size());
        //         }
        //         out.collect(jsonObjects);
        //     }
        // });
        SingleOutputStreamOperator<List<JSONObject>> winKeyDs = resDS.keyBy(new KeySelector<JSONObject, Integer>() {
                    @Override
                    public Integer getKey(JSONObject value) throws Exception {
                        return value.getString("sha1").hashCode()% Runtime.getRuntime().availableProcessors();
                    }
                })
                .window(TumblingProcessingTimeWindows.of(Time.seconds(10)))
                .apply(new WindowFunction<JSONObject, List<JSONObject>, Integer, TimeWindow>() {
                    @Override
                    public void apply(Integer s, TimeWindow window, Iterable<JSONObject> input, Collector<List<JSONObject>> out) throws Exception {
                        ArrayList<JSONObject> jsonObjects = Lists.newArrayList(input);
                        // if (jsonObjects.size() > 0) {
                        //     System.out.println("收集到的数据大小" + jsonObjects.size());
                        // }
                        out.collect(jsonObjects);
                    }
                });
        // windowDS.print();
        //resDS.print();

        //写入
        // windowDS.addSink(new PhoenixSink(" USDP", "SAMPLE_WIDTH_TABLE_9999"));
        winKeyDs.addSink(new PhoenixSink(" USDP", "SAMPLE_WIDTH_TABLE_9999"));
        env.execute();
    }
}
