package flink_core.waterMark;

import org.apache.flink.api.common.eventtime.SerializableTimestampAssigner;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.ProcessFunction;
import org.apache.flink.util.Collector;

/**
 * 水位线测试
 *
 * @author lzx
 * @date 2023/04/24 09:55
 **/
public class WaterMarkTest {
    public static void main(String[] args) throws Exception {

        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(1);

        // 水位线生成策略
        WatermarkStrategy<String> stringWatermarkStrategy = WatermarkStrategy.<String>forMonotonousTimestamps().withTimestampAssigner(new SerializableTimestampAssigner<String>() {
            @Override
            public long extractTimestamp(String element, long recordTimestamp) {
                return Long.parseLong(element.split(",")[1]);
            }
        });

        // 构造流
        DataStreamSource<String> stream1 = env.socketTextStream("localhost", 8888);
        SingleOutputStreamOperator<String> s1 = stream1.assignTimestampsAndWatermarks(stringWatermarkStrategy);

        DataStreamSource<String> stream2 = env.socketTextStream("localhost", 9999);
        SingleOutputStreamOperator<String> s2 = stream2.assignTimestampsAndWatermarks(stringWatermarkStrategy);

        // 两条单并行度流 合并到一条单并行度流
        DataStream<String> s3 = s1.union(s2);

        // 打印waterMark 观察watermark 推进情况
        s3.process(new ProcessFunction<String, String>() {
            @Override
            public void processElement(String value, ProcessFunction<String, String>.Context ctx, Collector<String> out) throws Exception {

                // 获取当前watermark （此处当前是指处理 当前数据前的watermark）
                long currentWatermark = ctx.timerService().currentWatermark();
                Long timestamp = ctx.timestamp();

                System.out.println("s" + ":" +timestamp + ":" +currentWatermark + "==>" + value);
                out.collect(value);
            }
        }).print();

        env.execute("WaterMarkTest");
    }
}
