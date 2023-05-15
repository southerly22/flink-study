package flink_core.state;

import org.apache.commons.lang3.RandomUtils;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.common.restartstrategy.RestartStrategies;
import org.apache.flink.api.common.state.ListState;
import org.apache.flink.api.common.state.ListStateDescriptor;
import org.apache.flink.api.common.state.OperatorStateStore;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.runtime.state.FunctionInitializationContext;
import org.apache.flink.runtime.state.FunctionSnapshotContext;
import org.apache.flink.streaming.api.CheckpointingMode;
import org.apache.flink.streaming.api.checkpoint.CheckpointedFunction;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

/**
 * @author lzx
 * @date 2023/05/07 19:32
 **/
public class State_Operator_Demo {
    public static void main(String[] args) throws Exception {
        Configuration conf = new Configuration();
        conf.setInteger("rest.port",8085);
        StreamExecutionEnvironment env = StreamExecutionEnvironment.createLocalEnvironmentWithWebUI(conf);
        env.setParallelism(1);

        //开启ck  快照的周期，快照的模式
        env.enableCheckpointing(3000L, CheckpointingMode.EXACTLY_ONCE);
        env.getCheckpointConfig().setCheckpointStorage("file:////Users/liuzhixin/codeplace/flink-study/flink-java/ck");

        // 开启 Task级别的故障自动 failover
        //env.setRestartStrategy(RestartStrategies.noRestart());  默认是不会自动failover；一个task故障了，整个job就失败了

        // 使用策略是是：固定重启上限和重启时间间隔
        env.setRestartStrategy(RestartStrategies.fixedDelayRestart(3,1000));

        DataStreamSource<String> source = env.socketTextStream("localhost", 9999);

        source.map(new MyStateMapFunction()).print();


        env.execute();
    }
}
class MyStateMapFunction implements MapFunction<String,String> , CheckpointedFunction{

    ListState<String> listState;

    /**
     * 正常的map 方法
     */
    @Override
    public String map(String value) throws Exception {

        /**
         * 故意埋一个异常，来测试task级别的容错
         */
        if ("x".equals(value) && RandomUtils.nextInt(1,10) % 2 == 0) {
            throw new Exception("出错了");
        }

        // 每来一条数据，存放到State中
        listState.add(value);

        // 新来的数据 拼接历史的字符串
        Iterable<String> strings = listState.get();
        StringBuilder sb = new StringBuilder();
        for (String s : strings) {
            sb.append(s);
        }
        return sb.toString();
    }


    /**
     * 系统对状态数据做快照（持久化）时，会调用此方法，可以利用这个方法在状态数据持久化前做一些操作
     */
    @Override
    public void snapshotState(FunctionSnapshotContext context) throws Exception {
        System.out.println("CK 触发了,CheckpointId为"+context.getCheckpointId());
    }

    /**
     * 算子任务在启动之初，会调用下面的方法，为用户进行状态的数据的初始化
     */
    @Override
    public void initializeState(FunctionInitializationContext context) throws Exception {
        OperatorStateStore operatorStateStore = context.getOperatorStateStore();
        ListStateDescriptor<String> stateDescriptor = new ListStateDescriptor<>("string", String.class);

        // getListState方法在task任务失败后，task任务重启时会帮助用户自动加载最新一份的状态数据
        // 如果是Job重启，则不会自动加载此前的快照的状态数据
         listState = operatorStateStore.getListState(stateDescriptor);

         /**
          * unionListState和listState的区别
          * @author lzx
          * @date 2023-05-12 11:08
          */
        ListState<String> unionListState = operatorStateStore.getUnionListState(stateDescriptor);
    }
}
