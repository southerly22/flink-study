package flink_core.state;

/**
 * @author lzx
 * @date 2023/09/14 22:21
 **/

import org.apache.flink.api.common.state.ValueState;
import org.apache.flink.api.common.state.ValueStateDescriptor;
import org.apache.flink.api.common.typeinfo.Types;
import org.apache.flink.api.java.DataSet;
import org.apache.flink.api.java.ExecutionEnvironment;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.runtime.state.hashmap.HashMapStateBackend;
import org.apache.flink.state.api.BootstrapTransformation;
import org.apache.flink.state.api.ExistingSavepoint;
import org.apache.flink.state.api.OperatorTransformation;
import org.apache.flink.state.api.Savepoint;
import org.apache.flink.state.api.functions.KeyedStateBootstrapFunction;
import org.apache.flink.state.api.functions.KeyedStateReaderFunction;
import org.apache.flink.util.Collector;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
/**
 *
 * @author lzx
 * @date 2023-09-14 22:56
 * @describe  从ck 读取状态数据
 */
public class FlinkReadAndUpdateState {
    private static final String ckPath = "file:////Users/liuzhixin/codeplace/flink-study/flink-java/ck/34c6fdf8e8e7c5b72df54eda361c0fcd/chk-6";
    private static final Collection<KeyedState> data =
            Arrays.asList(new KeyedState("hive", 1L), new KeyedState("JasonLee1", 100L), new KeyedState("hhase", 3L));

    public static void main(String[] args) throws Exception {
        stateRead(ckPath);
        //stateWrite("");
    }

    /**
     * 从 ck 读取状态数据
     * @param ckPath
     * @throws Exception
     */
    public static void stateRead(String ckPath) throws Exception {
        ExecutionEnvironment bEnv = ExecutionEnvironment.getExecutionEnvironment();
        bEnv.setParallelism(1);
        ExistingSavepoint savepoint = Savepoint.load(bEnv, ckPath, new HashMapStateBackend());
        DataSet<KeyedState> keyedState = savepoint.readKeyedState("my-uid", new ReaderFunction());
        List<KeyedState> keyedStates = keyedState.collect();
        for (KeyedState ks: keyedStates) {
            System.out.println(String.format("key: %s, value: %s", ks.key, ks.value));
        }
    }

    /**
     * 初始化状态数据
     * @param ckPath
     */
    public static void stateWrite(String ckPath) throws Exception {
        int maxParallelism = 128;

        ExecutionEnvironment bEnv = ExecutionEnvironment.getExecutionEnvironment();

        DataSet<KeyedState> dataKeyedState = bEnv.fromCollection(data);

        BootstrapTransformation<KeyedState> transformation = OperatorTransformation
                .bootstrapWith(dataKeyedState)
                .keyBy(k -> k.key)
                .transform(new WriterFunction());

        Savepoint
                .create(new HashMapStateBackend(), maxParallelism)
                .withOperator("uid-test", transformation)
                .write("file:///Users/jasonlee/flink-1.14.0/checkpoint/init_state");

        bEnv.execute();
    }

    public static class WriterFunction extends KeyedStateBootstrapFunction<String, KeyedState> {
        ValueState<Long> state;
        @Override
        public void open(Configuration parameters) throws Exception {

            ValueStateDescriptor<Long> stateDescriptor = new ValueStateDescriptor<>("state", Types.LONG);
            state = getRuntimeContext().getState(stateDescriptor);
        }

        @Override
        public void processElement(KeyedState value, KeyedStateBootstrapFunction<String, KeyedState>.Context ctx) throws Exception {
            state.update(value.value);
        }
    }

    public static class ReaderFunction extends KeyedStateReaderFunction<String, KeyedState> {
        ValueState<Long> state;

        @Override
        public void open(Configuration parameters) {
            ValueStateDescriptor<Long> stateDescriptor = new ValueStateDescriptor<>("state", Types.LONG);
            state = getRuntimeContext().getState(stateDescriptor);
        }

        @Override
        public void readKey(
                String key,
                Context ctx,
                Collector<KeyedState> out) throws Exception {

            KeyedState data = new KeyedState();
            data.key = key;
            data.value = state.value();
            out.collect(data);
        }
    }

    public static class KeyedState {
        public String key;
        public Long value;

        public KeyedState(String key, Long value) {
            this.key = key;
            this.value = value;
        }

        public KeyedState() {}
    }
}
