package flink_core.skew;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.functions.ReduceFunction;
import org.apache.flink.api.common.typeinfo.Types;
import org.apache.flink.api.java.functions.KeySelector;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.api.java.tuple.Tuple3;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.windowing.ProcessWindowFunction;
import org.apache.flink.streaming.api.windowing.assigners.TumblingEventTimeWindows;
import org.apache.flink.streaming.api.windowing.assigners.TumblingProcessingTimeWindows;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.util.Collector;

import java.time.Duration;
import java.util.concurrent.ThreadLocalRandom;

/**
 * @author lzx
 * @date 2023/6/8 18:05
 * @description: TODO
 */
public class SkewDemo1 {
    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        // 转换 （mid,1）
        SingleOutputStreamOperator<Tuple2<String, Integer>> t2DS = env.socketTextStream("localhost", 9999)
                .map(JSON::parseObject)
                .assignTimestampsAndWatermarks(
                        WatermarkStrategy.<JSONObject>forBoundedOutOfOrderness(Duration.ZERO)
                                .withTimestampAssigner((json, l) -> json.getLong("ts"))
                )
                .map(json -> Tuple2.of(json.getString("mid"), 1))
                .returns(Types.TUPLE(Types.STRING, Types.INT));

        // 加前缀聚合
        t2DS.map(t2 -> Tuple2.of(t2.f0 + "-" + ThreadLocalRandom.current().nextInt(3), 1))
                .keyBy(t2 -> t2.f0)
                .window(TumblingEventTimeWindows.of(Time.seconds(3)))
                .reduce(new ReduceFunction<Tuple2<String, Integer>>() {
                    @Override
                    public Tuple2<String, Integer> reduce(Tuple2<String, Integer> value1, Tuple2<String, Integer> value2) throws Exception {
                        return Tuple2.of(value1.f0, value1.f1 + value2.f1);
                    }
                }).map(t2 -> Tuple2.of(t2.f0.split("-")[0], t2.f1))
                .keyBy(t2 -> t2.f0)
                .reduce(new ReduceFunction<Tuple2<String, Integer>>() {
                    @Override
                    public Tuple2<String, Integer> reduce(Tuple2<String, Integer> value1, Tuple2<String, Integer> value2) throws Exception {
                        return Tuple2.of(value1.f0, value1.f1 + value2.f1);
                    }
                }).print();

        env.execute();
    }
}
