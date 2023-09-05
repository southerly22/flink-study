package flink_core.skew;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.common.functions.ReduceFunction;
import org.apache.flink.api.common.typeinfo.Types;
import org.apache.flink.api.java.functions.KeySelector;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.api.java.tuple.Tuple3;
import org.apache.flink.contrib.streaming.state.EmbeddedRocksDBStateBackend;
import org.apache.flink.contrib.streaming.state.RocksDBStateBackend;
import org.apache.flink.streaming.api.CheckpointingMode;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.windowing.ProcessWindowFunction;
import org.apache.flink.streaming.api.windowing.assigners.TumblingEventTimeWindows;
import org.apache.flink.streaming.api.windowing.assigners.TumblingProcessingTimeWindows;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.util.Collector;
import org.rocksdb.BlockBasedTableConfig;
import org.rocksdb.BloomFilter;

import java.time.Duration;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

/**
 * @author lzx
 * @date 2023/6/8 18:05
 * @description: TODO
 * <p>
 * {"mid":"lzx","ts":1000}
 */
public class SkewDemo2 {
    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        EmbeddedRocksDBStateBackend rocksDBStateBackend = new EmbeddedRocksDBStateBackend();

        //rocksDBStateBackend.setRocksDBOptions();

        BlockBasedTableConfig tableConfig = new BlockBasedTableConfig();
        tableConfig.setFilterPolicy(new BloomFilter(10,false));


        env.setParallelism(1);
        env.enableCheckpointing(1000, CheckpointingMode.EXACTLY_ONCE);
        env.getCheckpointConfig().setCheckpointStorage("file:///D:\\WorkPlace\\flink-study\\flink-java\\ck");

        // 转换 （mid,1）
        SingleOutputStreamOperator<Tuple2<String, Long>> t2DS = env.socketTextStream("192.168.1.76", 9999)
                .map(JSON::parseObject)
                .assignTimestampsAndWatermarks(
                        WatermarkStrategy.<JSONObject>forBoundedOutOfOrderness(Duration.ZERO)
                                .withTimestampAssigner((json, l) -> json.getLong("ts"))
                )
                .map(json -> Tuple2.of(json.getString("mid"), 1L))
                .returns(Types.TUPLE(Types.STRING, Types.LONG));


        // 拼接随机数打散，第一次聚合（窗口聚合）
        SingleOutputStreamOperator<Tuple3<String, Long, Long>> firstAgg = t2DS
                .map(new MapFunction<Tuple2<String, Long>, Tuple2<String, Long>>() {
                    Random random = new Random();

                    @Override
                    public Tuple2<String, Long> map(Tuple2<String, Long> value) throws Exception {
                        return Tuple2.of(value.f0 + "-" + random.nextInt(3), 1L);
                    }
                }) // mid拼接随机数
                .keyBy(r -> r.f0) // 第一次按照 "mid|随机数" 分组
                .window(TumblingEventTimeWindows.of(Time.seconds(3)))
                .reduce(
                        (value1, value2) -> Tuple2.of(value1.f0, value1.f1 + value2.f1),
                        new ProcessWindowFunction<Tuple2<String, Long>, Tuple3<String, Long, Long>, String, TimeWindow>() {
                            @Override
                            public void process(String s, Context context, Iterable<Tuple2<String, Long>> elements, Collector<Tuple3<String, Long, Long>> out) throws Exception {
                                Tuple2<String, Long> midAndCount = elements.iterator().next();
                                long windowEndTs = context.window().getEnd();
                                out.collect(Tuple3.of(midAndCount.f0, midAndCount.f1, windowEndTs));
                            }
                        }
                );// 窗口聚合（第一次聚合），加上窗口结束时间的标记，方便第二次聚合汇总

        // 按照原来的 key和windowEnd分组，第二次聚合
        firstAgg
                .map(new MapFunction<Tuple3<String, Long, Long>, Tuple3<String, Long, Long>>() {
                    @Override
                    public Tuple3<String, Long, Long> map(Tuple3<String, Long, Long> value) throws Exception {
                        String originKey = value.f0.split("-")[0];
                        return Tuple3.of(originKey, value.f1, value.f2);
                    }
                }) // 去掉 拼接的随机数
                .keyBy(new KeySelector<Tuple3<String, Long, Long>, Tuple2<String, Long>>() {
                    @Override
                    public Tuple2<String, Long> getKey(Tuple3<String, Long, Long> value) throws Exception {
                        return Tuple2.of(value.f0, value.f2);
                    }
                }) // 按照 原来的 key和 窗口结束时间 分组
                .window(TumblingEventTimeWindows.of(Time.seconds(3)))
                .reduce((value1, value2) -> Tuple3.of(value1.f0, value1.f1 + value2.f1, value1.f2)) // 第二次真正聚合
                .print();

        env.execute();
    }
}
