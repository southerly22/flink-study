package flink_core.state;

import org.apache.flink.api.common.RuntimeExecutionMode;
import org.apache.flink.api.common.functions.RichMapFunction;
import org.apache.flink.api.common.functions.RuntimeContext;
import org.apache.flink.api.common.restartstrategy.RestartStrategies;
import org.apache.flink.api.common.state.ListState;
import org.apache.flink.api.common.state.ListStateDescriptor;
import org.apache.flink.api.common.state.StateTtlConfig;
import org.apache.flink.api.common.time.Time;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.CheckpointingMode;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

/**
 * @author lzx
 * @date 2023/5/11 18:11
 * @description: TODO 状态 TTL
 */
public class State_TTL_Demo {
    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(1);
        env.setRuntimeMode(RuntimeExecutionMode.STREAMING);

        env.enableCheckpointing(3000, CheckpointingMode.EXACTLY_ONCE);
        env.getCheckpointConfig().setCheckpointStorage("file:///D:\\WorkPlace\\flink-study\\flink-java\\ck\\");
        env.setRestartStrategy(RestartStrategies.fixedDelayRestart(3,3000));

        DataStreamSource<String> source = env.socketTextStream("192.168.1.76", 9999);

        source.keyBy(s->"0").map(new RichMapFunction<String, String>() {

            ListState<String> listState;

            @Override
            public void open(Configuration parameters) throws Exception {
                RuntimeContext runtimeContext = getRuntimeContext();

                // 1.定义状态TTL配置
                StateTtlConfig ttlConfig = StateTtlConfig.newBuilder(Time.milliseconds(1000))
                        .setTtl(Time.milliseconds(3000)) // 配置数据存活时长

                        // .updateTtlOnReadAndWrite() // 读、写 都导致该条数据 ttl 重新计时，延迟清理时限
                         .setUpdateType(StateTtlConfig.UpdateType.OnReadAndWrite) // 同上
                        // .updateTtlOnCreateAndWrite() // 插入、更新 都导致该条数据 ttl 重新计时，延迟清理时限
                        //
                        // .setStateVisibility(StateTtlConfig.StateVisibility.NeverReturnExpired) // 不允许返回ttl到期，但还未清理的数据
                        // .setStateVisibility(StateTtlConfig.StateVisibility.ReturnExpiredIfNotCleanedUp) // 允许返回ttl到期，但还未清理的数据

                        // .setTtlTimeCharacteristic(StateTtlConfig.TtlTimeCharacteristic.ProcessingTime)// ttl计时的时间语义，处理时间
                        .useProcessingTime() // ttl计时的时间语义，处理时间

                         // 下面三种 过期检查清除策略，不是覆盖关系，而是添加关系
                        .cleanupIncrementally(5,false) //默认策略
                        .cleanupIncrementally(1000,true) //增量清除（每当一条数据被访问，则会检查这条数据的ttl是否超时，是就删除）
                        .cleanupFullSnapshot() // 全量快照清除策略（在checkpoint 的时候 保存到快照中的只包含未过期的状态数据，但是他并不会清理算子本地状态
                        .cleanupInRocksdbCompactFilter(1000) // 在rockdb的compact机制中添加过期数据过滤器 以在compact过程中清理掉过期状态数据

                        .build();

                // 2.声明状态描述器
                ListStateDescriptor<String> listStateDescriptor = new ListStateDescriptor<>("listState", String.class);

                // 3.状态描述器 设置TTL
                listStateDescriptor.enableTimeToLive(ttlConfig);

                // 4.创建ListState状态存储器
                listState = runtimeContext.getListState(listStateDescriptor);

            }

            @Override
            public String map(String value) throws Exception {
                listState.add(value);
                StringBuilder builder = new StringBuilder();
                for (String s : listState.get()) {
                    builder.append(s);
                }
                return builder.toString();
            }
        }).print();

        env.execute();
    }
}
