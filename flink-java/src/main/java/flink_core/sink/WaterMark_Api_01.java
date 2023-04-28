package flink_core.sink;

import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

import java.time.Duration;

/**
 * @author lzx
 * @date 2023/04/26 18:19
 * @desc watermark生成设置相关代码演示
 *  *       及单并行度情况下的watermark推进观察
 *  *
 *  *   ==> 在socket端口依次输入如下两条数据：
 *  *   1,e06,3000,page02
 *  *   1,e06,3000,page02
 **/
public class WaterMark_Api_01 {
    public static void main(String[] args) throws Exception {
        Configuration configuration = new Configuration();
        configuration.setInteger("rest.port", 8085);
        StreamExecutionEnvironment env = StreamExecutionEnvironment.createLocalEnvironmentWithWebUI(configuration);

        DataStreamSource<String> stream = env.socketTextStream("localhost", 9999);

        // 策略4： WatermarkStrategy.forMonotonousTimestamps()   单调，紧跟最大事件时间
        // 策略3： WatermarkStrategy.forGenerator()              自定义watermark 生成策略
        // 策略2： WatermarkStrategy.forBoundedOutOfOrderness()  有界无序，允许乱序watermark生成策略
        // 策略1： WatermarkStrategy.noWatermarks()              不生成watermark 禁用事件时间的推进机制

        /**
         * @author lzx
         * @date 2023-04-26 18:27
         * 示例一 ：从源头算子生成watermark
         */

        // 1.构造一个watermark 生成策略对象（算法策略 以及事件时间抽取方法）
        WatermarkStrategy<String> watermarkStrategy = WatermarkStrategy
                .<String>forBoundedOutOfOrderness(Duration.ZERO) //允许乱序的算法策略
                .withTimestampAssigner((element, recordTimestamp) -> Long.parseLong(element.split(",")[2]));

        // 2.将构造好的watermark 策略对象，分配给流（Source算子）
        stream.assignTimestampsAndWatermarks(watermarkStrategy);

        env.execute("WaterMark_Api_01");
    }
}
