package flink_core.window;

import org.apache.flink.api.common.eventtime.SerializableTimestampAssigner;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.typeinfo.TypeHint;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.windowing.WindowFunction;
import org.apache.flink.streaming.api.windowing.assigners.TumblingEventTimeWindows;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.streaming.api.windowing.triggers.Trigger;
import org.apache.flink.streaming.api.windowing.triggers.TriggerResult;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.util.Collector;
import org.apache.flink.util.OutputTag;

import java.time.Duration;

/**
 * @author lzx
 * @date 2023/05/05 22:54
 * @description 窗口触发机制--trigger，窗口驱逐机制--evictor
 **/
public class Window_Api_Demo4 {
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

// todo 自定义窗口触发器
class MyEventTimeTrigger extends Trigger<Tuple2<EventBean,Integer>,TimeWindow>{

    private MyEventTimeTrigger() {}

    /**
     * 来一条数据时，需要检查watermark 是否超过窗口结束时间点，超过就触发
     */
    @Override
    public TriggerResult onElement(Tuple2<EventBean,Integer> element, long timestamp, TimeWindow window,
                                   TriggerContext ctx) throws Exception {

        // 如果窗口结束点 <= 当前的watermark
        if (window.maxTimestamp() <= ctx.getCurrentWatermark()) {
            return TriggerResult.FIRE;
        } else {
            // 注册定时器，定时器的触发时间为：窗口的结束点时间
            ctx.registerEventTimeTimer(window.maxTimestamp());

            // 判断当前数据的用户事件id 是都等于e0x，如果等于则触发
            if ("e0x".equals(element.f0.getEventId())) {
                return TriggerResult.FIRE;
            }
            return TriggerResult.CONTINUE;
        }
    }

    /**
     * 当事件时间定时器的触发时间（窗口结束时间）到达了，检查是否满足触发条件
     * 下面的方法是定时器在调用
     */
    @Override
    public TriggerResult onEventTime(long time, TimeWindow window, TriggerContext ctx) {
        return time == window.maxTimestamp() ? TriggerResult.FIRE : TriggerResult.CONTINUE;
    }

    /**
     * 当处理时间定时器的触发时间（窗口结束时间）到达了，检查是否满足触发条件
     */
    @Override
    public TriggerResult onProcessingTime(long time, TimeWindow window, TriggerContext ctx)
            throws Exception {
        return TriggerResult.CONTINUE;
    }

    @Override
    public void clear(TimeWindow window, TriggerContext ctx) throws Exception {
        ctx.deleteEventTimeTimer(window.maxTimestamp());
    }

    @Override
    public boolean canMerge() {
        return true;
    }

    @Override
    public void onMerge(TimeWindow window, OnMergeContext ctx) {
        // only register a timer if the watermark is not yet past the end of the merged window
        // this is in line with the logic in onElement(). If the watermark is past the end of
        // the window onElement() will fire and setting a timer here would fire the window twice.
        long windowMaxTimestamp = window.maxTimestamp();
        if (windowMaxTimestamp > ctx.getCurrentWatermark()) {
            ctx.registerEventTimeTimer(windowMaxTimestamp);
        }
    }

    @Override
    public String toString() {
        return "EventTimeTrigger()";
    }

    /**
     * Creates an event-time trigger that fires once the watermark passes the end of the window.
     *
     * <p>Once the trigger fires all elements are discarded. Elements that arrive late immediately
     * trigger window evaluation with just this one element.
     * @return
     */
    public static MyEventTimeTrigger create(){
       return new MyEventTimeTrigger();
    }
}
