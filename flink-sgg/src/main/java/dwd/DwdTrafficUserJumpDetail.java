package dwd;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.apache.flink.api.common.eventtime.SerializableTimestampAssigner;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.cep.CEP;
import org.apache.flink.cep.PatternSelectFunction;
import org.apache.flink.cep.PatternStream;
import org.apache.flink.cep.PatternTimeoutFunction;
import org.apache.flink.cep.pattern.Pattern;
import org.apache.flink.cep.pattern.conditions.SimpleCondition;
import org.apache.flink.connector.kafka.source.KafkaSource;
import org.apache.flink.connector.kafka.source.enumerator.initializer.OffsetsInitializer;
import org.apache.flink.streaming.api.CheckpointingMode;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.datastream.KeyedStream;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.ProcessFunction;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.util.Collector;
import org.apache.flink.util.OutputTag;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.OffsetResetStrategy;

import java.time.Duration;
import java.util.List;
import java.util.Map;

/**
 * @author lzx
 * @date 2023/6/7 16:37
 * @description: TODO 跳出事件：1.用户 ->首页 ->首页。 2.用户访问首页 超过10秒没其他页面
 * 技术：CEP
 * 数据：
 * {"common":{"ar":"370000","ba":"Xiaomi","ch":"web","is_new":"0","md":"Xiaomi 10 Pro","mid":"mid_2190279","os":"Android 11.0","uid":"688","vc":"v2.1.134"},"page":{"during_time":8224,"last_page_id":"good_detail","page_id":"cart"},"ts":1651303990000}
 */
public class DwdTrafficUserJumpDetail {
    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(1);
        env.enableCheckpointing(1000, CheckpointingMode.EXACTLY_ONCE);
        env.getCheckpointConfig().setCheckpointStorage("file:///D:\\WorkPlace\\flink-study\\flink-sgg\\ck");

        KafkaSource<String> kafkaSource = KafkaSource.<String>builder()
                .setBootstrapServers("192.168.1.56:9092,192.168.1.61:9092,192.168.1.58:9092,192.168.3.71:9092,192.168.3.178:9092")
                .setTopics("test-sgg")
                .setGroupId("test0607")
                .setProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "false")
                .setProperty("commit.offsets.on.checkpoint", "false") // 指定是否在进行 checkpoint 时将消费位点提交至 Kafka broker
                .setValueOnlyDeserializer(new SimpleStringSchema())
                .setStartingOffsets(OffsetsInitializer.committedOffsets(OffsetResetStrategy.LATEST))
                .build();
        // todo 1.分配水位线
        DataStreamSource<String> kafkaDS = env.fromSource(
                kafkaSource,
                WatermarkStrategy.noWatermarks(),
                "kafkaSource");

        // todo 2.拿到kafka数据 转为jsonObj，按照mid分组 （设备id分组）
        KeyedStream<JSONObject, String> keyedStream = kafkaDS.map(JSON::parseObject)
                .assignTimestampsAndWatermarks(
                        WatermarkStrategy.<JSONObject>forBoundedOutOfOrderness(Duration.ofSeconds(2))
                                .withTimestampAssigner(new SerializableTimestampAssigner<JSONObject>() {
                                    @Override
                                    public long extractTimestamp(JSONObject element, long recordTimestamp) {
                                        return element.getLong("ts");
                                    }
                                })
                )
                .keyBy(json -> json.getJSONObject("common").getString("mid"));

        keyedStream.process(new ProcessFunction<JSONObject, String>() {
            @Override
            public void processElement(JSONObject value, ProcessFunction<JSONObject, String>.Context ctx, Collector<String> out) throws Exception {
                System.out.println("currentWatermark: " + ctx.timerService().currentWatermark());
                out.collect(value.toJSONString());
            }
        }).print();


        // todo 3.定义CEP规则
        Pattern<JSONObject, JSONObject> pattern = Pattern.<JSONObject>begin("start").where(new SimpleCondition<JSONObject>() {
            @Override
            public boolean filter(JSONObject value) throws Exception {
                return value.getJSONObject("page").getString("last_page_id") == null;
            }
        }).next("next").where(new SimpleCondition<JSONObject>() {
            @Override
            public boolean filter(JSONObject value) throws Exception {
                return value.getJSONObject("page").getString("last_page_id") == null;
            }
        }).within(Time.seconds(10));//超时时间

        // Pattern.<JSONObject>begin("start").where(new SimpleCondition<JSONObject>() {
        //     @Override
        //     public boolean filter(JSONObject value) throws Exception {
        //         return value.getJSONObject("page").getString("last_page_id") == null;
        //     }
        // }).times(2)
        //         .consecutive() // 严格连续
        //         .within(Time.seconds(10));

        //todo 4.将pattern 作用到流上
        PatternStream<JSONObject> patternDS = CEP.pattern(keyedStream, pattern);

        // todo 5.提取事件（匹配上的事件 和 超时事件）
        OutputTag<String> timeOut = new OutputTag<String>("timeout") {
        };

        SingleOutputStreamOperator<String> selectDS = patternDS.select(timeOut, new PatternTimeoutFunction<JSONObject, String>() {
            @Override
            public String timeout(Map<String, List<JSONObject>> map, long timeoutTimestamp) throws Exception {
                return map.get("start").get(0).toJSONString();
            }
        }, new PatternSelectFunction<JSONObject, String>() {
            @Override
            public String select(Map<String, List<JSONObject>> map) throws Exception {
                return map.get("start").get(0).toJSONString();
            }
        });
        DataStream<String> timeoutDS = selectDS.getSideOutput(timeOut);

        selectDS.union(timeoutDS);
        // todo 6.输出2条流
        selectDS.print("select>>>");
        timeoutDS.print("timeout>>>");

        // todo 7.合并2条流
        // selectDS.union(timeoutDS);
        env.execute("DwdTrafficUserJumpDetail");
    }
}
