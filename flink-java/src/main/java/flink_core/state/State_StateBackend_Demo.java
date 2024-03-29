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
import org.apache.flink.contrib.streaming.state.EmbeddedRocksDBStateBackend;
import org.apache.flink.runtime.state.hashmap.HashMapStateBackend;
import org.apache.flink.streaming.api.CheckpointingMode;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

/**
 * @author lzx
 * @date 2023/5/11 18:11
 * @description: 状态后端 demo
 */
public class State_StateBackend_Demo {
    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(1);
        env.setRuntimeMode(RuntimeExecutionMode.STREAMING);
        /**
         *  声明所要使用的 状态后端（管理存储状态的一个工具）
         *  演示代码中设置，一般都在配置文件里面声明
         */
        // 存储在java heap内存 以java对象的方式存储，过大也会溢出磁盘，但是取得时候会很慢
        HashMapStateBackend hashMapStateBackend = new HashMapStateBackend();
        env.setStateBackend(hashMapStateBackend);

        // rocksDB存储在内存+磁盘  kv类型存储，底层序列化成字节数组
        EmbeddedRocksDBStateBackend rocksDBStateBackend = new EmbeddedRocksDBStateBackend();
        env.setStateBackend(rocksDBStateBackend);

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
                        // .setUpdateType(StateTtlConfig.UpdateType.OnReadAndWrite) // 同上
                        // .updateTtlOnCreateAndWrite() // 插入、更新 都导致该条数据 ttl 重新计时，延迟清理时限
                        //
                        // .setStateVisibility(StateTtlConfig.StateVisibility.NeverReturnExpired) // 不允许返回ttl到期，但还未清理的数据
                        // .setStateVisibility(StateTtlConfig.StateVisibility.ReturnExpiredIfNotCleanedUp) // 允许返回ttl到期，但还未清理的数据

                        // .setTtlTimeCharacteristic(StateTtlConfig.TtlTimeCharacteristic.ProcessingTime)// ttl计时的时间语义，处理时间
                        .useProcessingTime()

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
