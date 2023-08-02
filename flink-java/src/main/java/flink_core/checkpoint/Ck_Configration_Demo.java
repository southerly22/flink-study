package flink_core.checkpoint;

import org.apache.flink.configuration.Configuration;
import org.apache.flink.core.fs.Path;
import org.apache.flink.streaming.api.CheckpointingMode;
import org.apache.flink.streaming.api.environment.CheckpointConfig;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

import java.time.Duration;

/**
 * checkpoint参数配置大全
 *
 * @author lzx
 * @date 2023/05/12 18:39
 **/
public class Ck_Configration_Demo {
    public static void main(String[] args) {
        Configuration configuration = new Configuration();

        // 设置从检查点(ck) 恢复数据
        configuration.setString("execution.savepoint.path", "D:\\WorkPlace\\flink-study\\flink-java\\ck\\82c266361f12ff1ced5ffb222d93de96\\chk-61");
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment(configuration);

        // CheckpointingMode.AT_LEAST_ONCE barrier不对齐  CheckpointingMode.EXACTLY_ONCE barrier对齐
        env.enableCheckpointing(3000, CheckpointingMode.EXACTLY_ONCE); //传入两个参数 1，ck生成间隔 2，ck模式
        CheckpointConfig checkpointConfig = env.getCheckpointConfig();

        checkpointConfig.setCheckpointStorage("file:///D:/checkpoint/"); // 存在本地
        checkpointConfig.setCheckpointStorage(new Path("hdfs://doit01:8020/ckpt")); // 存储到远程hdfs系统上

        checkpointConfig.setAlignedCheckpointTimeout(Duration.ofMillis(3000)); // 设置ck 对齐的超时时间
        checkpointConfig.setCheckpointingMode(CheckpointingMode.EXACTLY_ONCE); //同上 设置ck barrier算法模式
        checkpointConfig.setCheckpointInterval(2000); //ck 生成间隔

        checkpointConfig.setCheckpointIdOfIgnoredInFlightData(5); //用于非对齐算法模式下，在job恢复时让各个算子自动抛弃掉ck-5中飞行数据
        checkpointConfig.setForceUnalignedCheckpoints(false); // 是否强制使用 非对齐的ck 模式
        checkpointConfig.setMaxConcurrentCheckpoints(5); //设置ck的并行度 （允许在系统中同时存在飞行（未完成）的ck 数量）
        checkpointConfig.setMinPauseBetweenCheckpoints(2000); // 设置两次ck之间的最小时间间隔，用于防止ck过多占用算子处理时间
        checkpointConfig.setCheckpointTimeout(3000); // 一个算子在一次checkpoint 执行过程中的总耗费时长超时上限
        checkpointConfig.setTolerableCheckpointFailureNumber(10); //容忍 ck 失败最大次数

        // CheckpointConfig.ExternalizedCheckpointCleanup.DELETE_ON_CANCELLATION:当外部作用被取消时，删除外部 checkpoint（默认值）
        // CheckpointConfig.ExternalizedCheckpointCleanup.RETAIN_ON_CANCELLATION:当外部作用被取消时，保留外部 checkpoint
        env.getCheckpointConfig().setExternalizedCheckpointCleanup(CheckpointConfig.ExternalizedCheckpointCleanup.RETAIN_ON_CANCELLATION);

    }
}
