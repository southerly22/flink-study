package flink_core.topN;

import flink_core.topN.TopN_Test.OrderAmt;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.apache.flink.api.common.eventtime.SerializableTimestampAssigner;
import org.apache.flink.api.common.eventtime.WatermarkGenerator;
import org.apache.flink.api.common.eventtime.WatermarkGeneratorSupplier;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.functions.AggregateFunction;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.common.state.ListState;
import org.apache.flink.api.common.state.ListStateDescriptor;
import org.apache.flink.api.common.typeinfo.TypeHint;
import org.apache.flink.api.common.typeinfo.Types;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.configuration.RestOptions;
import org.apache.flink.streaming.api.CheckpointingMode;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.datastream.KeyedStream;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.CheckpointConfig;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.KeyedProcessFunction;
import org.apache.flink.streaming.api.functions.source.datagen.DataGeneratorSource;
import org.apache.flink.streaming.api.functions.source.datagen.RandomGenerator;
import org.apache.flink.streaming.api.functions.windowing.WindowFunction;
import org.apache.flink.streaming.api.windowing.assigners.SlidingEventTimeWindows;
import org.apache.flink.streaming.api.windowing.assigners.SlidingProcessingTimeWindows;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.util.Collector;

import java.time.Duration;
import java.util.*;

/**
 * @author lzx
 * @date 2023/08/27 11:15
 * Flink  实现topN
 **/
public class TopN_Test {
    public static void main(String[] args) throws Exception {
        Configuration configuration = new Configuration();
        configuration.set(RestOptions.ENABLE_FLAMEGRAPH, true); //开启火焰图
        StreamExecutionEnvironment env = StreamExecutionEnvironment.createLocalEnvironmentWithWebUI(configuration);
        env.setParallelism(1);
        env.disableOperatorChaining(); //禁用算子链
        env.enableCheckpointing(3000, CheckpointingMode.EXACTLY_ONCE);
        env.getCheckpointConfig().setCheckpointStorage("file:////Users/liuzhixin/codeplace/flink-study/flink-java/ck");

        WatermarkStrategy<OrderAmt> orderAmtWatermarkStrategy = WatermarkStrategy.<OrderAmt>forBoundedOutOfOrderness(Duration.ZERO)
                .withTimestampAssigner((element, recordTimestamp) -> element.getTs());

        SingleOutputStreamOperator<OrderAmt> orderAmtDs = env.socketTextStream("localhost", 9999)
                .map(new MapFunction<String, OrderAmt>() {
                    @Override
                    public OrderAmt map(String value) throws Exception {
                        String[] split = value.split(",");
                        int id = Integer.parseInt(split[0]);
                        double amt = Double.parseDouble(split[1]);
                        long ts = Long.parseLong(split[2]);
                        return new OrderAmt(id, amt, ts);
                    }
                }).assignTimestampsAndWatermarks(orderAmtWatermarkStrategy);


        SingleOutputStreamOperator<OrderAmtSum> orderAmtAggDs = orderAmtDs.keyBy(OrderAmt::getId)
                .window(SlidingEventTimeWindows.of(Time.seconds(5), Time.seconds(3)))
                .aggregate(new SumAgg(), new WindowResult());

        orderAmtAggDs.print();

        KeyedStream<OrderAmtSum, Long> keyedStream = orderAmtAggDs.keyBy(OrderAmtSum::getWin_end);
        keyedStream.process(new SumTopSize(2));

        env.execute();
    }

   private static class SumAgg implements AggregateFunction<OrderAmt, Double, Double> {

        @Override
        public Double createAccumulator() {
            return 0.0;
        }

        @Override
        public Double add(OrderAmt value, Double accumulator) {
            return accumulator + value.getAmt();
        }

        @Override
        public Double getResult(Double accumulator) {
            return accumulator;
        }

        @Override
        public Double merge(Double a, Double b) {
            return a + b;
        }
    }

    private static class WindowResult implements WindowFunction<Double,OrderAmtSum,Integer, TimeWindow>{

        @Override
        public void apply(Integer id, TimeWindow window, Iterable<Double> input, Collector<OrderAmtSum> out) throws Exception {
            long end = window.getEnd();
            long start = window.getStart();
            Double amt = input.iterator().next();
            out.collect(new OrderAmtSum(id,amt,start,end));
        }
    }

    private static class SumTopSize extends KeyedProcessFunction<Long, OrderAmtSum, String> {

        private Integer topSize;
        private ListState<OrderAmtSum> listState;

        public SumTopSize(Integer topSize) {
            this.topSize = topSize;
        }

        @Override
        public void open(Configuration parameters) throws Exception {
            ListStateDescriptor<OrderAmtSum> listStateDescriptor = new ListStateDescriptor<>("order_sum", OrderAmtSum.class);
            listState = getRuntimeContext().getListState(listStateDescriptor);
        }

        @Override
        public void processElement(OrderAmtSum value, KeyedProcessFunction<Long, OrderAmtSum, String>.Context ctx, Collector<String> out) throws Exception {
            listState.add(value);
            // 注册定时器 当时间 > 注册时间的时候 触发onTimer 方法
            ctx.timerService().registerProcessingTimeTimer(value.getWin_end() + 1000);
            //ctx.timerService().registerEventTimeTimer();
        }

        @Override
        public void onTimer(long timestamp, KeyedProcessFunction<Long, OrderAmtSum, String>.OnTimerContext ctx, Collector<String> out) throws Exception {
            // 倒序的优先队列
            PriorityQueue<MyCompare> myCompares = new PriorityQueue<>(topSize, new Comparator<MyCompare>() {
                @Override
                public int compare(MyCompare o1, MyCompare o2) {
                    return o2.getAmtSum().compareTo(o1.getAmtSum());
                }
            });
            StringBuilder stringBuilder = new StringBuilder();
            for (OrderAmtSum orderAmtSum : listState.get()) {

                MyCompare myCompare = new MyCompare(orderAmtSum.getId(), orderAmtSum.getAmtSum(),orderAmtSum.getWin_end());
                myCompares.add(myCompare);
            }

            //myCompares
            //System.out.println(stringBuilder.toString());
            //Thread.sleep(3000);
            out.collect(stringBuilder.toString());
        }
    }

    @NoArgsConstructor
    @AllArgsConstructor
    @Data
    @ToString
    static class OrderAmtSum{
        private Integer id;
        private Double amtSum;
        private Long win_start;
        private Long win_end;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @ToString
    static class MyCompare{
        private Integer id;
        private Double amtSum;
        private Long win_start;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    static class OrderAmt{
        private Integer id;
        private Double amt;
        private Long ts;
    }
}
