package flink_core.checkpoint;

import org.apache.flink.api.common.restartstrategy.RestartStrategies;
import org.apache.flink.api.common.time.Time;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

/**
 * task容错机制，task 失败重启策略
 *
 * @author lzx
 * @date 2023/05/15 09:47
 **/
public class Tolerance_Configuration_Demo {
    public static void main(String[] args) {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        RestartStrategies.RestartStrategyConfiguration restartStrategy = null;

        // 固定 延迟重启（参数1：故障重启最大次数，参数2：两次重启之间的间隔）
        restartStrategy = RestartStrategies.fixedDelayRestart(3, 2000);

        // 默认的故障重启策略，不重启。只要有task 失败，job也就失败
        restartStrategy = RestartStrategies.noRestart();

        /**
         *  本策略：故障越频繁，两次重启间的惩罚间隔就越长
         *
         *  initialBackoff 重启间隔惩罚时长的初始值 ： 1s
         *  maxBackoff 重启间隔最大惩罚时长 : 60s
         *  backoffMultiplier 重启间隔时长的惩罚倍数: 2（ 每多故障一次，重启延迟惩罚就在 上一次的惩罚时长上 * 倍数）
         *  resetBackoffThreshold 重置惩罚时长的平稳运行时长阈值（平稳运行达到这个阈值后，如果再故障，则故障重启延迟时间重置为了初始值：1s）
         *  jitterFactor 取一个随机数来加在重启时间点上，让每次重启的时间点呈现一定随机性
         *     job1: 9.51   9.53+2*0.1    9.57   ......
         *     job2: 9.51   9.53+2*0.15   9.57   ......
         *     job3: 9.51   9.53+2*0.8    9.57   ......
         */
        restartStrategy = RestartStrategies.exponentialDelayRestart(Time.seconds(1), Time.seconds(60),
                2.0, Time.hours(1), 1.0);

        /**
         * failureRate   最大重启次数在指定的 {@code failureInterval} 时间时长里面
         * failureInterval 指定的时间时长
         * delayInterval 两次重启之间的时间间隔
         */
        restartStrategy = RestartStrategies.failureRateRestart(5, Time.hours(1), Time.seconds(10));

        /**
         *  本策略就是退回到配置文件所配置的策略
         *  常用于自定义 RestartStrategy
         *  用户自定义了重启策略类，常常配置在 flink-conf.yaml 文件中
         */
        restartStrategy = RestartStrategies.fallBackRestart();

        env.setRestartStrategy(restartStrategy);

    }
}
