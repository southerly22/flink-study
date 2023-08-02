package flink_core.state;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.flink.api.common.eventtime.SerializableTimestampAssigner;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.common.state.MapState;
import org.apache.flink.api.common.state.MapStateDescriptor;
import org.apache.flink.api.common.state.ValueState;
import org.apache.flink.api.common.state.ValueStateDescriptor;
import org.apache.flink.api.common.time.Time;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.KeyedProcessFunction;
import org.apache.flink.streaming.api.functions.timestamps.BoundedOutOfOrdernessTimestampExtractor;
import org.apache.flink.table.planner.expressions.In;
import org.apache.flink.table.runtime.operators.window.TimeWindow;
import org.apache.flink.util.Collector;

import java.time.Duration;

/**
 * @author lzx
 * @date 2023/6/13 18:32
 * @description: TODO Map 去重案例
 */
public class MapStateDemo {
    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        DataStreamSource<String> source = env.socketTextStream("localhost", 9999);
        SingleOutputStreamOperator<ADate> aDateWithWaterMark = source.map(new MapFunction<String, ADate>() {
            @Override
            public ADate map(String value) throws Exception {
                String[] split = value.split(",");
                return new ADate(Integer.parseInt(split[0]), split[1], Long.parseLong(split[2]));
            }
        }).assignTimestampsAndWatermarks(
                WatermarkStrategy
                        .<ADate>forBoundedOutOfOrderness(Duration.ZERO)
                        .withTimestampAssigner(new SerializableTimestampAssigner<ADate>() {
                            @Override
                            public long extractTimestamp(ADate element, long recordTimestamp) {
                                return element.getTime();
                            }
                        }));

        aDateWithWaterMark.keyBy(s->{
            long endTime = TimeWindow.getWindowStartWithOffset(s.getTime(), 0, Time.hours(1).toMilliseconds()) + Time.hours(1).toMilliseconds();
            return new AdKey(s.getId(), endTime);
        });
        env.execute();
    }

    //  另外一个ValueState,存储当前MapState的数据量，是由于mapstate只能通过迭代方式获得数据量大小，每次获取都需要进行迭代，这种方式可以避免每次迭代。
    static class DistinctProcessFunction extends KeyedProcessFunction<AdKey,ADate,Void>{
        MapState<String, Integer> devIdState;
        ValueState<Long> countState;

        @Override
        public void open(Configuration parameters) throws Exception {
            MapStateDescriptor<String, Integer> devIdStateDesc = new MapStateDescriptor<>("devIdState", String.class, Integer.class);
            devIdState = getRuntimeContext().getMapState(devIdStateDesc);

            ValueStateDescriptor<Long> countStateDesc = new ValueStateDescriptor<>("countState", Long.class);
            countState = getRuntimeContext().getState(countStateDesc);

        }

        @Override
        public void processElement(ADate value, KeyedProcessFunction<AdKey, ADate, Void>.Context ctx, Collector<Void> out) throws Exception {

        }
    }
    // 广告数据
    @AllArgsConstructor
    @NoArgsConstructor
    @Data
    static class ADate {
        int id;
        String devId;
        long time;
    }

    // 分组数据
    @AllArgsConstructor
    @NoArgsConstructor
    @Data
    static class AdKey {
        int id;
        long time;
    }
}
