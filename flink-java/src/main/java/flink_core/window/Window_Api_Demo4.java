package flink_core.window;

import org.apache.flink.annotation.VisibleForTesting;
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
import org.apache.flink.streaming.api.windowing.evictors.Evictor;
import org.apache.flink.streaming.api.windowing.evictors.TimeEvictor;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.streaming.api.windowing.triggers.Trigger;
import org.apache.flink.streaming.api.windowing.triggers.TriggerResult;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.streaming.api.windowing.windows.Window;
import org.apache.flink.streaming.runtime.operators.windowing.TimestampedValue;
import org.apache.flink.util.Collector;

import java.time.Duration;
import java.util.Iterator;

/**
 * @author lzx
 * @date 2023/05/05 22:54
 * @description 窗口触发机制--trigger，窗口驱逐机制--evictor
 *  * 测试数据
 * 1,e01,10000,p01,10  [10,20)
 * 1,e02,11000,p02,20
 * 1,e02,12000,p03,40
 * 1,e0x,13000,p03,40 ==> 这里会触发一次窗口
 * 1,e04,16000,p05,50
 * 1,e03,20000,p02,10  [20,30)  ==> 时间到达也会触发一次窗口
 **/
public class Window_Api_Demo4 {
    public static void main(String[] args) throws Exception {
        Configuration configuration = new Configuration();
        configuration.setInteger("rest.port", 8085);
        StreamExecutionEnvironment env = StreamExecutionEnvironment.createLocalEnvironmentWithWebUI(configuration);

        DataStreamSource<String> source = env.socketTextStream("localhost", 9999);
        env.setParallelism(1);

        SingleOutputStreamOperator<Tuple2<EventBean, Integer>> beanStream = source.map(s -> {
            String[] split = s.split(",");
            EventBean bean = new EventBean(Long.parseLong(split[0]), split[1], Long.parseLong(split[2]), split[3], Integer.parseInt(split[4]));
            return Tuple2.of(bean, 1);
        }).returns(TypeInformation.of(new TypeHint<Tuple2<EventBean, Integer>>() {
        }));

        // 分配watermark 以推进事件时间
        WatermarkStrategy<Tuple2<EventBean, Integer>> beanStreamWatermarkStrategy = WatermarkStrategy.<Tuple2<EventBean, Integer>>forBoundedOutOfOrderness(Duration.ZERO)
                .withTimestampAssigner(new SerializableTimestampAssigner<Tuple2<EventBean, Integer>>() {
                    @Override
                    public long extractTimestamp(Tuple2<EventBean, Integer> element, long recordTimestamp) {
                        return element.f0.getTimeStamp();
                    }
                });

        // 分配watermark
        SingleOutputStreamOperator<Tuple2<EventBean, Integer>> beanStreamWithWaterMark = beanStream.assignTimestampsAndWatermarks(beanStreamWatermarkStrategy);

        // trigger 流
        SingleOutputStreamOperator<String> resultStream = beanStreamWithWaterMark.keyBy(tp -> tp.f0.getGuid())
                .window(TumblingEventTimeWindows.of(Time.seconds(10)))
                .trigger(MyEventTimeTrigger.create())
                .evictor(MyTimeEvictor.of(Time.seconds(10)))
                .apply(new WindowFunction<Tuple2<EventBean, Integer>, String, Long, TimeWindow>() {
                    @Override
                    public void apply(Long key, TimeWindow window, Iterable<Tuple2<EventBean, Integer>> input, Collector<String> out) throws Exception {
                        int cnt = 0;
                        for (Tuple2<EventBean, Integer> tuple2 : input) {
                            cnt++;
                        }
                        out.collect(window.getStart() + "," + "," + window.getEnd() + "," + cnt);
                    }
                });

        resultStream.print("主流");
        env.execute();
    }
}

// todo 自定义窗口触发器 仿写 EventTimeTrigger
class MyEventTimeTrigger extends Trigger<Tuple2<EventBean, Integer>, TimeWindow> {

    private MyEventTimeTrigger() {
    }

    /**
     * 来一条数据时，需要检查watermark 是否超过窗口结束时间点，超过就触发
     */
    @Override
    public TriggerResult onElement(Tuple2<EventBean, Integer> element, long timestamp, TimeWindow window,
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
        long windowMaxTimestamp = window.maxTimestamp();
        if (windowMaxTimestamp > ctx.getCurrentWatermark()) {
            ctx.registerEventTimeTimer(windowMaxTimestamp);
        }
    }

    public static MyEventTimeTrigger create() {
        return new MyEventTimeTrigger();
    }
}

// todo 自定义驱逐器 仿写 TimeEvictor
class MyTimeEvictor<W extends Window> implements Evictor<Object, W> {
    private static final long serialVersionUID = 1L;

    private final long windowSize;
    private final boolean doEvictAfter;

    // 构造函数
    public MyTimeEvictor(long windowSize) {
        this.windowSize = windowSize;
        this.doEvictAfter = false;
    }

    public MyTimeEvictor(long windowSize, boolean doEvictAfter) {
        this.windowSize = windowSize;
        this.doEvictAfter = doEvictAfter;
    }

    // 得到类的实例 单例
    public static <W extends Window> MyTimeEvictor<W> of(Time windowSize) {
        return new MyTimeEvictor<>(windowSize.toMilliseconds());
    }

    public static <W extends Window> MyTimeEvictor<W> of(Time windowSize, boolean doEvictAfter) {
        return new MyTimeEvictor<>(windowSize.toMilliseconds(), doEvictAfter);
    }
    /**
     * 窗口触发前调用
     */
    @Override
    public void evictBefore(
            Iterable<TimestampedValue<Object>> elements, int size, W window, EvictorContext ctx) {
        if (!doEvictAfter) {
            evict(elements, size, ctx);
        }
    }

    /**
     * 窗口触发后调用
     */
    @Override
    public void evictAfter(
            Iterable<TimestampedValue<Object>> elements, int size, W window, EvictorContext ctx) {
        if (doEvictAfter) {
            evict(elements, size, ctx);
        }
    }

    /**
     * 驱逐元素的核心逻辑
     */
    private void evict(Iterable<TimestampedValue<Object>> elements, int size, EvictorContext ctx) {
        if (!hasTimestamp(elements)) {
            return;
        }

        long currentTime = getMaxTimestamp(elements);
        long evictCutoff = currentTime - windowSize;

        for (Iterator<TimestampedValue<Object>> iterator = elements.iterator(); iterator.hasNext(); ) {
            TimestampedValue<Object> record = iterator.next();
            Tuple2<EventBean,Integer> t2  = (Tuple2<EventBean, Integer>) record.getValue();

            // 移除元素:如果数据包含 "e0x" 则移除数据
            if (record.getTimestamp() <= evictCutoff || "e0x".equals(t2.f0.getEventId())) {
                iterator.remove();
            }
        }
    }

    private boolean hasTimestamp(Iterable<TimestampedValue<Object>> elements) {
        Iterator<TimestampedValue<Object>> it = elements.iterator();
        if (it.hasNext()) {
            return it.next().hasTimestamp();
        }
        return false;
    }

    private long getMaxTimestamp(Iterable<TimestampedValue<Object>> elements) {
        long currentTime = Long.MIN_VALUE;
        for (Iterator<TimestampedValue<Object>> iterator = elements.iterator();
             iterator.hasNext(); ) {
            TimestampedValue<Object> record = iterator.next();
            currentTime = Math.max(currentTime, record.getTimestamp());
        }
        return currentTime;
    }

    @Override
    public String toString() {
        return "TimeEvictor(" + windowSize + ")";
    }

    @VisibleForTesting
    public long getWindowSize() {
        return windowSize;
    }

}