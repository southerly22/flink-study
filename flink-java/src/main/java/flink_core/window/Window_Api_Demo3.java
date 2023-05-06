package flink_core.window;

import org.apache.flink.api.common.eventtime.SerializableTimestampAssigner;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.typeinfo.TypeHint;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.windowing.WindowFunction;
import org.apache.flink.streaming.api.windowing.assigners.TumblingEventTimeWindows;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.util.Collector;
import org.apache.flink.util.OutputTag;

import java.time.Duration;

/**
 * @author lzx
 * @date 2023/05/05 22:54
 * @description 窗口允许迟到数据
 **/
public class Window_Api_Demo3 {
    public static void main(String[] args) throws Exception {
        Configuration configuration = new Configuration();
        configuration.setInteger("rest.port", 8085);
        StreamExecutionEnvironment env = StreamExecutionEnvironment.createLocalEnvironmentWithWebUI(configuration);

        DataStreamSource<String> source = env.socketTextStream("localhost", 9999);
        env.setParallelism(1);

        SingleOutputStreamOperator<EventBean> beanStream = source.map(s -> {
            String[] split = s.split(",");
            return new EventBean(Long.parseLong(split[0]), split[1], Long.parseLong(split[2]), split[3], Integer.parseInt(split[4]));
        }).returns(EventBean.class);

        // 分配watermark 以推进事件时间
        WatermarkStrategy<EventBean> eventBeanWatermarkStrategy = WatermarkStrategy.<EventBean>forBoundedOutOfOrderness(Duration.ZERO)
                .withTimestampAssigner(new SerializableTimestampAssigner<EventBean>() {
                    @Override
                    public long extractTimestamp(EventBean element, long recordTimestamp) {
                        return element.getTimeStamp();
                    }
                });
        // 定义侧输出流
        OutputTag<EventBean> lateTag = new OutputTag<EventBean>("late_tag", TypeInformation.of(new TypeHint<EventBean>() {}));

        SingleOutputStreamOperator<EventBean> watermarkedBeanStream = beanStream.assignTimestampsAndWatermarks(eventBeanWatermarkStrategy);

        SingleOutputStreamOperator<String> resultStream = watermarkedBeanStream.keyBy(EventBean::getGuid)
                .window(TumblingEventTimeWindows.of(Time.seconds(10)))
                .allowedLateness(Time.seconds(2))
                .sideOutputLateData(lateTag)
                .apply(new WindowFunction<EventBean, String, Long, TimeWindow>() {
                    @Override
                    public void apply(Long key, TimeWindow window, Iterable<EventBean> input, Collector<String> out) throws Exception {
                        int cnt = 0;
                        for (EventBean bean : input) {
                            cnt++;
                        }
                        out.collect(window.getStart() + "," + window.getEnd() + "," + cnt);
                    }
                });

        resultStream.print("主流");

        // 迟到数据 ： 输出条件：当窗口推进到 窗口时间+迟到时间时，此时再来该窗口内的数据时此时会输出到迟到流中
        resultStream.getSideOutput(lateTag).print("迟到数据");

        env.execute();
    }
}
