package huorong.async;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import huorong.PhoenixSink;
import huorong.entity.SampleUserdefinedScan;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.restartstrategy.RestartStrategies;
import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.connector.kafka.source.KafkaSource;
import org.apache.flink.connector.kafka.source.enumerator.initializer.OffsetsInitializer;
import org.apache.flink.runtime.state.hashmap.HashMapStateBackend;
import org.apache.flink.streaming.api.CheckpointingMode;
import org.apache.flink.streaming.api.datastream.AsyncDataStream;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.CheckpointConfig;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.windowing.WindowFunction;
import org.apache.flink.streaming.api.windowing.assigners.TumblingProcessingTimeWindows;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.util.Collector;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.TopicPartition;
import scala.tools.nsc.doc.model.Val;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author lzx
 * @date 2023/06/05 14:37
 **/
public class TestAsyncDemo {
    public static void main(String[] args) throws Exception {
        Configuration configuration = new Configuration();
        configuration.setInteger("rest.port", 8085);
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
                .setStartingOffsets(OffsetsInitializer.offsets(topicMap))
                .build();

        DataStreamSource<String> kafkaStream = env.fromSource(kafkaSource, WatermarkStrategy.noWatermarks(), "kafkaSource");

        SingleOutputStreamOperator<SampleUserdefinedScan> filterDS = kafkaStream
                .setParallelism(6)
                .map(s -> JSON.parseObject(s, SampleUserdefinedScan.class))
                .filter(sample -> "U".equals(sample.getTask_type()) && sample.getSha1() != null);

        // 关联维度信息 -- pad
        SingleOutputStreamOperator<SampleUserdefinedScan> padAsyncDS = AsyncDataStream.unorderedWait(
                filterDS, new CustomAsyncCommon<SampleUserdefinedScan>("SAMPLE_PAD_SCAN_LATEST") {
                    @Override
                    public String getKey(SampleUserdefinedScan input) {
                        return input.getSha1();
                    }

                    @Override
                    public void join(SampleUserdefinedScan input, List<JSONObject> dimInfoList) throws InvocationTargetException, IllegalAccessException {
                        for (JSONObject info : dimInfoList) {
                            input.setRk(input.getSha1());
                            input.setScan_time(info.getLong("scan_time"));
                            String engine_name = info.getString("engine_name").concat("_");
                            BeanUtils.setProperty(input, engine_name + "id", info.getString("scan_id"));
                            BeanUtils.setProperty(input, engine_name + "name", StringEscapeUtils.escapeSql(info.getString("scan_name")));
                            BeanUtils.setProperty(input, engine_name + "virus_name", info.getString("virus_name"));
                            BeanUtils.setProperty(input, engine_name + "virus_platform", info.getString("virus_platform"));
                            BeanUtils.setProperty(input, engine_name + "virus_tech", info.getString("virus_tech"));
                            BeanUtils.setProperty(input, engine_name + "virus_type", info.getString("virus_type"));
                        }
                    }
                }, 100, TimeUnit.SECONDS
        ).startNewChain();

        //关联 src
        SingleOutputStreamOperator<SampleUserdefinedScan> srcWithPadAsyncDS = AsyncDataStream.unorderedWait(
                padAsyncDS,
                new CustomAsyncCommon<SampleUserdefinedScan>("SAMPLE_SRC") {
                    @Override
                    public String getKey(SampleUserdefinedScan input) {
                        return input.getSha1();
                    }

                    @Override
                    public void join(SampleUserdefinedScan input, List<JSONObject> dimInfoList) {
                        StringBuilder stringBuilder = new StringBuilder();
                        for (JSONObject info : dimInfoList) {
                            stringBuilder.append(info.getString("src_name")).append(",");
                        }
                        String src_list = stringBuilder.deleteCharAt(stringBuilder.length() - 1).toString();
                        input.setSrc_list(src_list);
                    }
                },
                100, TimeUnit.SECONDS
        );

        //关联 info
        SingleOutputStreamOperator<SampleUserdefinedScan> infoWithSrcWithPadAsyncDS = AsyncDataStream.unorderedWait(
                srcWithPadAsyncDS,
                new CustomAsyncCommon<SampleUserdefinedScan>("SAMPLE_INFO") {
                    @Override
                    public String getKey(SampleUserdefinedScan input) {
                        return input.getSha1();
                    }

                    @Override
                    public void join(SampleUserdefinedScan input, List<JSONObject> dimInfoList) {
                        for (JSONObject info : dimInfoList) {
                            input.setId(info.getLong("id"));
                            input.setFdfs_path(info.getString("fdfs_path"));
                            input.setFilesize(info.getLong("filesize"));
                            input.setMd5(info.getString("md5"));
                            input.setSimhash(info.getString("simhash"));
                            input.setHashsig(info.getString("hashsig"));
                            input.setHashsig_pe(info.getString("hashsig_pe"));
                            input.setFiletype(info.getString("filetype"));
                            input.setDie(StringEscapeUtils.escapeSql(info.getString("die")));
                            input.setSha256(info.getString("sha256"));
                            input.setSha512(info.getString("sha512"));
                            input.setAddtime(info.getLong("addtime"));
                            input.setLastaddtime(info.getLong("lastaddtime"));
                        }
                    }
                },
                100, TimeUnit.SECONDS
        );

        // keyBy + 开窗
        SingleOutputStreamOperator<List<JSONObject>> windowDS = infoWithSrcWithPadAsyncDS.keyBy(s -> {
            return s.getRk().hashCode() % Runtime.getRuntime().availableProcessors();
        }).window(TumblingProcessingTimeWindows.of(Time.seconds(20))).apply(new WindowFunction<SampleUserdefinedScan, List<JSONObject>, Integer, TimeWindow>() {
            @Override
            public void apply(Integer integer, TimeWindow window, Iterable<SampleUserdefinedScan> input, Collector<List<JSONObject>> out) throws Exception {
                ArrayList<JSONObject> arrayList = new ArrayList<>();
                Iterator<SampleUserdefinedScan> iterator = input.iterator();
                while (iterator.hasNext()) {
                    SampleUserdefinedScan userdefinedScan = iterator.next();
                    JSONObject jsonObject = (JSONObject)JSON.toJSON(userdefinedScan);
                    jsonObject.remove("task_id");
                    jsonObject.remove("task_type");
                    arrayList.add(jsonObject);
                }
                out.collect(arrayList);
            }
        });

        //sink 持久化
        windowDS.addSink(new PhoenixSink(" USDP", "SAMPLE_WIDTH_TABLE_9999"));
        env.execute("ASYNC");
    }
}
