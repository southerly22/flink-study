package flink_core.state;

import org.apache.flink.api.common.functions.AggregateFunction;
import org.apache.flink.api.common.functions.ReduceFunction;
import org.apache.flink.api.common.functions.RichMapFunction;
import org.apache.flink.api.common.functions.RuntimeContext;
import org.apache.flink.api.common.functions.util.RuntimeUDFContext;
import org.apache.flink.api.common.restartstrategy.RestartStrategies;
import org.apache.flink.api.common.state.*;
import org.apache.flink.api.common.typeinfo.TypeHint;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.CheckpointingMode;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.table.planner.expressions.In;
import org.apache.kafka.common.protocol.types.Field;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;

/**
 * 状态数据结构
 *
 * @author lzx
 * @date 2023/05/10 15:06
 **/
public class State_DataStruct_Demo {
    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(1);
        env.enableCheckpointing(1000, CheckpointingMode.EXACTLY_ONCE);
        env.getCheckpointConfig().setCheckpointStorage("/Users/liuzhixin/codeplace/flink-study/flink-java/ck");

        env.setRestartStrategy(RestartStrategies.fixedDelayRestart(3, 2000));
        DataStreamSource<String> source = env.socketTextStream("localhost", 9999);

        /**
         *  需求：使用map 算子达到一个效果，每来一条数据输出该条字符串拼接此前到达过的字符串
         * @author lzx
         * @date 2023-05-10 15:09
         */
        source.keyBy(s -> "0")
                .map(new RichMapFunction<String, String>() {

                    ValueState<String> valueState;
                    ListState<String> listState;
                    MapState<String, String> mapState;
                    ReducingState<String> reduceState;
                    AggregatingState<Integer, Double> aggState;

                    @Override
                    public void open(Configuration parameters) throws Exception {
                        RuntimeContext runtimeContext = getRuntimeContext();
                        // 获取一个 单值 结构的状态存储器
                        valueState = runtimeContext.getState(new ValueStateDescriptor<String>("valueState", String.class));

                        // 获取一个 list 结构的状态存储器
                        listState = runtimeContext.getListState(new ListStateDescriptor<String>("listState", String.class));

                        // 获取一个 Map 结构的状态存储器
                        mapState = runtimeContext.getMapState(new MapStateDescriptor<String, String>("mapState", String.class, String.class));

                        // 获取一个 reduce 聚合状态
                        reduceState = runtimeContext.getReducingState(new ReducingStateDescriptor<String>("reduceState", new ReduceFunction<String>() {
                            @Override
                            public String reduce(String value1, String value2) throws Exception {
                                return value1 + "-" + value2;
                            }
                        }, String.class));

                        // 插入整数 返回平均值
                        aggState = runtimeContext.getAggregatingState(new AggregatingStateDescriptor<Integer, Tuple2<Integer, Integer>, Double>(
                                        "aggState", new AggregateFunction<Integer, Tuple2<Integer, Integer>, Double>() {
                                    @Override
                                    public Tuple2<Integer, Integer> createAccumulator() {
                                        return Tuple2.of(0, 0);
                                    }

                                    @Override
                                    public Tuple2<Integer, Integer> add(Integer value, Tuple2<Integer, Integer> accumulator) {
                                        accumulator.f0 += value; // sum
                                        accumulator.f1 += 1;     // cnt
                                        return accumulator;
                                    }

                                    @Override
                                    public Double getResult(Tuple2<Integer, Integer> accumulator) {
                                        return (double) (accumulator.f0 / accumulator.f1);
                                    }

                                    @Override
                                    public Tuple2<Integer, Integer> merge(Tuple2<Integer, Integer> a, Tuple2<Integer, Integer> b) {
                                        a.f0 += b.f0;
                                        a.f1 += b.f1;
                                        return a;
                                    }
                                }, TypeInformation.of(new TypeHint<Tuple2<Integer, Integer>>() {}) // acc中间聚合状态类型
                                )
                        );
                    }

                    @Override
                    public String map(String value) throws Exception {
                        /**
                         * ValueState 状态操作Api
                         */
                        String str = valueState.value(); // 获取状态里面的值
                        valueState.update(value); // 更新状态里面的值

                        /**
                         * listState 状态操作Api
                         */
                        Iterable<String> list = listState.get(); //拿到整个listState的迭代器
                        Iterator<String> iterator = list.iterator();
                        while (iterator.hasNext()) {
                            String s = iterator.next();
                            System.out.println(s);
                        }

                        listState.add(value);
                        listState.addAll(Arrays.asList("1", "2", "3")); // 一次放入多个
                        listState.update(Arrays.asList("a", "b", "c")); // 一次更新多个

                        /**
                         * mapState 状态操作Api
                         */
                        String s1 = mapState.get("value");
                        boolean b = mapState.contains(value);
                        Iterable<Map.Entry<String, String>> entryIterable = mapState.entries(); // 拿到mapstate的entry的 Iterable（内含迭代器）
                        Iterator<Map.Entry<String, String>> entryIterator = mapState.iterator();// 拿到mapstate的entry的 内含迭代器
                        mapState.remove(value);

                        /**
                         * agg State 状态操作Api
                         */
                        aggState.add(10);
                        aggState.add(20);

                        Double aDouble = aggState.get(); // 获取状态值
                        return null;
                    }
                }).setParallelism(2)
                .print().setParallelism(2);
        env.execute();
    }
}
