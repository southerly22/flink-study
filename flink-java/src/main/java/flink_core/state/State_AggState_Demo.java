package flink_core.state;

import org.apache.flink.api.common.functions.AggregateFunction;
import org.apache.flink.api.common.functions.ReduceFunction;
import org.apache.flink.api.common.functions.RichMapFunction;
import org.apache.flink.api.common.functions.RuntimeContext;
import org.apache.flink.api.common.restartstrategy.RestartStrategies;
import org.apache.flink.api.common.state.*;
import org.apache.flink.api.common.typeinfo.TypeHint;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.CheckpointingMode;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;

/**
 * 状态数据结构
 *
 * @author lzx
 * @date 2023/05/10 15:06
 **/
public class State_AggState_Demo {
    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(1);
        env.enableCheckpointing(1000, CheckpointingMode.EXACTLY_ONCE);
        env.getCheckpointConfig().setCheckpointStorage("file:////Users/liuzhixin/codeplace/flink-study/flink-java/ck");

        env.setRestartStrategy(RestartStrategies.fixedDelayRestart(3, 2000));
        DataStreamSource<String> source = env.socketTextStream("localhost", 9999);

        /**
         *  需求：使用map 算子达到一个效果，每来一条数据输出该条字符串拼接此前到达过的字符串
         * @author lzx
         * @date 2023-05-10 15:09
         */
        SingleOutputStreamOperator<Double> aggStateDS =
                source.map(Integer::valueOf)
                        .keyBy(s -> "a")
                        .map(new RichMapFunction<Integer, Double>() {

                            AggregatingState<Integer, Double> aggState;

                            @Override
                            public void open(Configuration parameters) throws Exception {
                                RuntimeContext runtimeContext = getRuntimeContext();

                                // 插入整数 返回平均值
                                aggState = runtimeContext.getAggregatingState(
                                        new AggregatingStateDescriptor<Integer, Tuple2<Integer, Integer>, Double>("aggState", new AggregateFunction<Integer, Tuple2<Integer, Integer>, Double>() {
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
                                                return Tuple2.of(a.f0 + b.f0,a.f1 + b.f1);
                                            }
                                        }, TypeInformation.of(new TypeHint<Tuple2<Integer, Integer>>() {
                                        }) // acc中间聚合状态类型
                                        ));
                            }

                            @Override
                            public Double map(Integer value) throws Exception {
                                aggState.add(value);
                                return aggState.get();
                            }
                        });

        aggStateDS.print("agg >>");
        env.execute();
    }
}
