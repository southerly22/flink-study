package flink_core.sink;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.apache.flink.api.common.eventtime.SerializableTimestampAssigner;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.ProcessFunction;
import org.apache.flink.util.Collector;

import java.time.Duration;

/**
 * @author lzx
 * @date 2023/04/26 18:19
 * @desc watermark生成设置相关代码演示
 *  *       及单并行度情况下的watermark推进观察
 *  *   ==> 在socket端口依次输入如下两条数据：
 *  *   1,e06,3000,page02  watermark : -Long.MaxValue
 *  *   1,e06,4000,page02  watermark : 2999
 **/
public class WaterMark_Api_02 {
    public static void main(String[] args) throws Exception {
        Configuration configuration = new Configuration();
        configuration.setInteger("rest.port", 8085);
        StreamExecutionEnvironment env = StreamExecutionEnvironment.createLocalEnvironmentWithWebUI(configuration);
        env.setParallelism(1);

        DataStreamSource<String> stream = env.socketTextStream("192.168.1.58", 9999);

        // 策略4： WatermarkStrategy.forMonotonousTimestamps()   单调，紧跟最大事件时间
        // 策略3： WatermarkStrategy.forGenerator()              自定义watermark 生成策略
        // 策略2： WatermarkStrategy.forBoundedOutOfOrderness()  有界无序，允许乱序watermark生成策略
        // 策略1： WatermarkStrategy.noWatermarks()              不生成watermark 禁用事件时间的推进机制

        // todo 示例一：从源头算子开始 生成 water mark
        /**
         * 1. 构造一个watermark 生成策略对象
         * （watermark算法策略 以及事件时间抽取方法）
         */
        WatermarkStrategy<String> watermarkStrategy = WatermarkStrategy
                .<String>forBoundedOutOfOrderness(Duration.ofMillis(0)) // 允许乱序的算法策略
                .withTimestampAssigner(new SerializableTimestampAssigner<String>() {
                    //抽取数据流中得时间戳
                    @Override
                    public long extractTimestamp(String element, long recordTimestamp) {
                        return Long.parseLong(element.split(",")[2]);
                    }
                });

        // 2.将构造好的watermark 策略对象，分配给流（Source算子）
        // stream.assignTimestampsAndWatermarks(watermarkStrategy);

        // todo 示例二：从中间算子开始生成 water mark。注意（如果在源头生成了，就不要再下游生成了）
        SingleOutputStreamOperator<EventBean> s2 = stream.map(new MapFunction<String, EventBean>() {
            @Override
            public EventBean map(String value) throws Exception {
                String[] split = value.split(",");
                return new EventBean(Long.parseLong(split[0]), split[1], Long.parseLong(split[2]), split[3]);
            }
        }).assignTimestampsAndWatermarks(
                WatermarkStrategy.<EventBean>forMonotonousTimestamps()
                        .withTimestampAssigner((element, recordTimestamp) -> element.getTimeStamp())
        );

        // 打印 watermark
        s2.process(new ProcessFunction<EventBean, Object>() {
            @Override
            public void processElement(EventBean eventBean, ProcessFunction<EventBean, Object>.Context ctx, Collector<Object> out) throws Exception {
                long processingTime = ctx.timerService().currentProcessingTime();
                long watermark = ctx.timerService().currentWatermark();
                System.out.println("当前数据：" + eventBean.toString());
                System.out.println("当前数据时间：" + eventBean.getTimeStamp());
                System.out.println("当前处理时间：" + processingTime);
                System.out.println("当前水位线时间：" + watermark);
            }
        }).print();

        env.execute("WaterMark_Api_01");
    }
}
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
class EventBean{
    private long guid;
    private String eventId;
    private long timeStamp;
    private String pageId;
}
