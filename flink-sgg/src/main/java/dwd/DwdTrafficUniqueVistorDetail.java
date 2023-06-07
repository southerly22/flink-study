package dwd;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.api.common.functions.RichFilterFunction;
import org.apache.flink.api.common.functions.RichFlatMapFunction;
import org.apache.flink.api.common.functions.RuntimeContext;
import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.api.common.state.StateTtlConfig;
import org.apache.flink.api.common.state.ValueState;
import org.apache.flink.api.common.state.ValueStateDescriptor;
import org.apache.flink.api.common.time.Time;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.connector.kafka.source.KafkaSource;
import org.apache.flink.connector.kafka.source.enumerator.initializer.OffsetsInitializer;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.datastream.KeyedStream;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.ProcessFunction;
import org.apache.flink.util.Collector;
import org.apache.flink.util.OutputTag;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.OffsetResetStrategy;
import utils.DateFormatUtil;

/**
 * @author lzx
 * @date 2023/6/7 11:27
 * @description: TODO
 * 需求：流量域独立访客事务事实表，独立访客：last_page_id ==null & 末次登陆日期为Null
 * 技术：状态编程 + 侧输出流
 * 数据格式：
 * {"common":{"ar":"370000","ba":"Xiaomi","ch":"web","is_new":"0","md":"Xiaomi 10 Pro","mid":"mid_2190279","os":"Android 11.0","uid":"688","vc":"v2.1.134"},"page":{"during_time":8224,"last_page_id":"good_detail","page_id":"cart"},"ts":1651303990000}
 * {"common":{"ar":"370000","ba":"Xiaomi","ch":"web","is _new":"0","md":"Xiaomi 10 Pro","mid":"mid_2190279","os":"Android 11.0","uid":"688","vc":"v2.1.134"},"page":{"during_time":11863,"item":"34,27,14","item_type":"sku_ids","last_page_id":"cart","page_id":"trade"},"ts":1651303991000}
 * {"common":{"ar":"370000","ba":"Xiaomi","ch":"web","is new":"0","md":"Xiaomi 10 Pro","mid":"mid 2190279","os":"Android 11.0","uid":"688","vc":"v2.1.134"},"page":{"during_time":19061,"item":"35,26","item type":"sku_ids","last_page_id":"trade","page_id":"payment"},"ts":1651303992000}
 */
public class DwdTrafficUniqueVistorDetail {
    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        KafkaSource<String> kafkaSource = KafkaSource.<String>builder()
                .setBootstrapServers("192.168.1.56:9092,192.168.1.61:9092,192.168.1.58:9092,192.168.3.71:9092,192.168.3.178:9092")
                .setTopics("test-sgg")
                .setGroupId("test0607")
                .setProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "false")
                .setProperty("commit.offsets.on.checkpoint", "false") // 指定是否在进行 checkpoint 时将消费位点提交至 Kafka broker
                .setValueOnlyDeserializer(new SimpleStringSchema())
                .setStartingOffsets(OffsetsInitializer.committedOffsets(OffsetResetStrategy.LATEST))
                .build();

        DataStreamSource<String> kafkaDS = env.fromSource(kafkaSource, WatermarkStrategy.noWatermarks(), "kafkaSource");

        // todo 1.kafka拿到数据 过滤last_page_id为空的,侧输出流输出错误数据
        OutputTag<String> illegal_str = new OutputTag<String>("illegal_str", TypeInformation.of(String.class));
        SingleOutputStreamOperator<JSONObject> pageIdNotNllDS = kafkaDS.process(new ProcessFunction<String, JSONObject>() {
            @Override
            public void processElement(String value, ProcessFunction<String, JSONObject>.Context ctx, Collector<JSONObject> out) throws Exception {
                try {
                    JSONObject jsonObject = JSON.parseObject(value);
                    // 过滤
                    if (jsonObject.getJSONObject("page").getString("last_page_id") == null) {
                        out.collect(jsonObject);
                    }
                } catch (Exception e) {
                    // 错误数据输出到侧输出流处理
                    ctx.output(illegal_str, value);
                    e.printStackTrace();
                }
            }
        });

        // todo 1.2 处理错误数据
        pageIdNotNllDS.getSideOutput(illegal_str).print("error_data");

        // todo 2.按照mid 分组
        KeyedStream<JSONObject, String> keyedStream = pageIdNotNllDS.keyBy(json -> json.getJSONObject("common").getString("mid"));

        //todo 3.使用状态编程实现按照 mid去重
        SingleOutputStreamOperator<JSONObject> filterDS = keyedStream.filter(new RichFilterFunction<JSONObject>() {
            // 单值状态
            ValueState<String> lastVisitState;

            @Override
            public void open(Configuration parameters) throws Exception {
                // 状态过期时间 1天，CreateAndWrite更新状态
                StateTtlConfig ttlConfig = StateTtlConfig.newBuilder(Time.days(1)).updateTtlOnCreateAndWrite().useProcessingTime().build();
                ValueStateDescriptor<String> stateDescriptor = new ValueStateDescriptor<>("logDate", String.class);
                stateDescriptor.enableTimeToLive(ttlConfig);
                lastVisitState = getRuntimeContext().getState(stateDescriptor);
            }

            @Override
            public boolean filter(JSONObject value) throws Exception {
                String curDate = DateFormatUtil.toDate(value.getLong("ts"));
                String last_login = lastVisitState.value();
                System.out.println(last_login);
                // 上次登录为空 或者 上次登录日期不是今天 --> 即为今日日活
                if (last_login == null || !curDate.equals(last_login)) {
                    lastVisitState.update(curDate);
                    return true;
                } else {
                    return false;
                }
            }
        });

        // todo 4.写入kafka
        // filterDS.addSink()
        filterDS.print("unique_visit");
        env.execute("DwdTrafficUniqueVistorDetail");
    }
}
