package flink_core.window;

import org.apache.flink.api.common.RuntimeExecutionMode;
import org.apache.flink.api.common.eventtime.SerializableTimestampAssigner;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.windowing.AllWindowFunction;
import org.apache.flink.streaming.api.windowing.assigners.SlidingEventTimeWindows;
import org.apache.flink.streaming.api.windowing.assigners.TumblingEventTimeWindows;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.streaming.api.windowing.windows.GlobalWindow;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.util.Collector;

import java.time.Duration;

/**
 * @author lzx
 * @date 2023/5/5 18:08
 * @description: TODO
 */
public class Window_Api_Demo2 {
    public static void main(String[] args) {
        Configuration configuration = new Configuration();
        configuration.setInteger("rest.port", 8085);
        StreamExecutionEnvironment env = StreamExecutionEnvironment.createLocalEnvironmentWithWebUI(configuration);

        DataStreamSource<String> source = env.socketTextStream("192.168.1.76", 9999);
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
        SingleOutputStreamOperator<EventBean> watermarkedBeanStream = beanStream.assignTimestampsAndWatermarks(eventBeanWatermarkStrategy);

        /***
         * @Author: lzx
         * 各种全局窗口开窗Api
         **/
        // 全局 计数滚动窗口
        watermarkedBeanStream.countWindowAll(10) //10 条数据一个窗口
                .apply(new AllWindowFunction<EventBean, String, GlobalWindow>() {
                    @Override
                    public void apply(GlobalWindow window, Iterable<EventBean> values, Collector<String> out) throws Exception {

                    }
                });

        // 全局 计数滑动窗口
        watermarkedBeanStream.countWindowAll(10,2); //窗口长度10条数据，滑动步长2条数据

        // 全局 事件时间滚动窗口
        watermarkedBeanStream.windowAll(TumblingEventTimeWindows.of(Time.seconds(30))) // 窗口长度
                .apply(new AllWindowFunction<EventBean, String , TimeWindow>() {
                    @Override
                    public void apply(TimeWindow window, Iterable<EventBean> values, Collector<String> out) throws Exception {

                    }
                });
        // 全局 事件时间滑动窗口
        watermarkedBeanStream.windowAll(SlidingEventTimeWindows.of(Time.seconds(30),Time.seconds(10)))
                .apply(new AllWindowFunction<EventBean, String, TimeWindow>() {
                    @Override
                    public void apply(TimeWindow window, Iterable<EventBean> values, Collector<String> out) throws Exception {

                    }
                });


    }
}
