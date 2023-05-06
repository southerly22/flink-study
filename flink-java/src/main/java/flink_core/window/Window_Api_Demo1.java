package flink_core.window;

import org.apache.flink.api.common.RuntimeExecutionMode;
import org.apache.flink.api.common.eventtime.SerializableTimestampAssigner;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.functions.AggregateFunction;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.api.java.tuple.Tuple3;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.windowing.ProcessWindowFunction;
import org.apache.flink.streaming.api.functions.windowing.WindowFunction;
import org.apache.flink.streaming.api.windowing.assigners.SlidingEventTimeWindows;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.util.Collector;

import java.time.Duration;
import java.util.*;

/**
 * @author lzx
 * @date 2023/5/4 15:07
 * @description: TODO
 * 测试数据  ：
 * 1,e01,10000,p01,10
 * 1,e02,11000,p02,20
 * 1,e02,12000,p03,40
 * 1,e03,20000,p02,10
 * 1,e01,21000,p03,50
 * 1,e04,22000,p04,10
 * 1,e06,28000,p05,60
 * 1,e07,30000,p02,10
 */
public class Window_Api_Demo1 {
    public static void main(String[] args) throws Exception {
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

        // watermarkedBeanStream.print();
        /***
         * @Author: lzx
         * @Description: 需求一 ： 每隔10s 统计最近 30s的数据中，每个用户的行为事件条数，使用aggregate算子实现
         * @Date: 2023/5/4
         **/
        SingleOutputStreamOperator<Integer> resultStream = watermarkedBeanStream.keyBy(EventBean::getGuid)
                .window(SlidingEventTimeWindows.of(Time.seconds(30), Time.seconds(10)))
                .aggregate(new AggregateFunction<EventBean, Integer, Integer>() {
                    /**
                     * 初始化累加器
                     **/
                    @Override
                    public Integer createAccumulator() {
                        return 0;
                    }

                    /**
                     * 滚动聚合的逻辑（拿到一条数据，该如何去更新累加器）
                     **/
                    @Override
                    public Integer add(EventBean value, Integer accumulator) {
                        return accumulator + 1;
                    }

                    /**
                     * 从累加器中，计算最终要输出的窗口计算结果
                     **/
                    @Override
                    public Integer getResult(Integer accumulator) {
                        return accumulator;
                    }

                    /**
                     *  批计算模式下，可能需要将多个上游的局部聚合累加器，放在下游进行全局聚合
                     *  因为需要对两个累加器进行合并
                     *  这里就是合并的逻辑
                     *  流计算模式下，不用实现！
                     */
                    @Override
                    public Integer merge(Integer a, Integer b) {
                        return a + b;
                    }
                });

        resultStream.print();

        /***
         * @Author: lzx
         * @Description: 需求二 ： 每隔10s 统计最近 30s的数据中，每个用户的平均每次行为时长
         * @Date: 2023/5/4
         **/
        SingleOutputStreamOperator<Double> resultStream2 = watermarkedBeanStream
                .keyBy(EventBean::getGuid)
                .window(SlidingEventTimeWindows.of(Time.seconds(30), Time.seconds(10)))
                // Tuple2<Integer, Integer> 1：存时长sum 2：存用户次数
                .aggregate(new AggregateFunction<EventBean, Tuple2<Integer, Integer>, Double>() {
                    @Override
                    public Tuple2<Integer, Integer> createAccumulator() {
                        return Tuple2.of(0, 0);
                    }

                    @Override
                    public Tuple2<Integer, Integer> add(EventBean eventBean, Tuple2<Integer, Integer> accumulator) {

                        return Tuple2.of(accumulator.f0 + eventBean.getActTimelong(), accumulator.f1 + 1);
                    }

                    @Override
                    public Double getResult(Tuple2<Integer, Integer> accumulator) {
                        return (double) (accumulator.f0 / accumulator.f1);
                    }

                    @Override
                    public Tuple2<Integer, Integer> merge(Tuple2<Integer, Integer> a, Tuple2<Integer, Integer> b) {

                        return Tuple2.of(a.f0 + b.f0, a.f1 + b.f1);
                    }
                });

        // resultStream2.print();

        /**
         * 全窗口计算api使用示例
         * 需求 三 ：  每隔10s，统计最近 30s 的数据中，每个用户的行为事件中，行为时长最长的前2条记录
         * 要求用 apply 或者  process 算子来实现
         *
         */
        SingleOutputStreamOperator<EventBean> resultStream3 = watermarkedBeanStream.keyBy(EventBean::getGuid)
                .window(SlidingEventTimeWindows.of(Time.seconds(30), Time.seconds(10)))
                // 泛型1：输入数据类型 泛型2：输出数据类型 泛型3：key的类型 泛型4：窗口类型
                .apply(new WindowFunction<EventBean, EventBean, Long, TimeWindow>() {
                    /***
                     * @Param key: 本次给咱们传入的窗口是属于那个key的
                     * @Param window: 本次传入的窗口的各种元数据信息（窗口起始时间，结束时间）
                     * @Param input: 本次传入的窗口中的所有数据的迭代器
                     * @Param out:  结果数据输出器
                     * @return: void
                     **/
                    @Override
                    public void apply(Long key, TimeWindow window, Iterable<EventBean> input, Collector<EventBean> out) throws Exception {
                        TreeSet<EventBean> treeSet = new TreeSet<>(new Comparator<EventBean>() {
                            @Override
                            public int compare(EventBean o1, EventBean o2) {
                                return o2.getActTimelong() - o1.getActTimelong();
                            }
                        });
                        // 取前两个
                        for (EventBean eventBean : input) {
                            treeSet.add(eventBean);
                            if (treeSet.size() > 3) {
                                treeSet.remove(treeSet.last());
                            }
                        }
                        // 结果数据输出
                        for (EventBean bean : treeSet) {
                            out.collect(bean);
                        }
                    }
                });
        // resultStream3.print("top3");

        SingleOutputStreamOperator<String> resultStream4 = watermarkedBeanStream.keyBy(EventBean::getGuid)
                .window(SlidingEventTimeWindows.of(Time.seconds(30), Time.seconds(10)))
                .process(new ProcessWindowFunction<EventBean, String, Long, TimeWindow>() {
                    @Override
                    public void process(Long aLong, ProcessWindowFunction<EventBean, String, Long, TimeWindow>.Context context, Iterable<EventBean> elements, Collector<String> out) throws Exception {
                        // 本次窗口的元信息
                        TimeWindow window = context.window();
                        long maxTimestamp = window.maxTimestamp(); //窗口的最大时间戳 [1000,2000) 最大时间戳即为1999，end为2000
                        long start = window.getStart();
                        long end = window.getEnd();

                        TreeSet<EventBean> treeSet = new TreeSet<>(new Comparator<EventBean>() {
                            @Override
                            public int compare(EventBean o1, EventBean o2) {
                                return o2.getActTimelong() - o1.getActTimelong();
                            }
                        });

                        for (EventBean element : elements) {
                            treeSet.add(element);
                            if (treeSet.size() > 2) treeSet.remove(treeSet.last());
                        }

                        for (EventBean eventBean : treeSet) {
                            out.collect("窗口start:" + start + "," + "窗口end:" + end + eventBean.toString());
                        }
                    }
                });
        // resultStream4.print();
        /**
         * TODO 补充练习 2
         * 需求 一 ：  每隔10s，统计最近 30s 的数据中，每个用户的最大行为时长
         * 滚动聚合api使用示例
         * 用max算子来实现
         */
        watermarkedBeanStream.keyBy(EventBean::getGuid)
                .window(SlidingEventTimeWindows.of(Time.seconds(30), Time.seconds(10)))
                .maxBy("actTimelong");
        // .print();

        /**
         * TODO 补充练习 4
         * 需求 一 ：  每隔10s，统计最近 30s 的数据中，每个页面上发生的行为中，平均时长最大的前2种事件及其平均时长
         * 用 process算子来实现
         */
        SingleOutputStreamOperator<Tuple3<String, String, Double>> resultStream5 = watermarkedBeanStream
                .keyBy(EventBean::getPageId)
                .window(SlidingEventTimeWindows.of(Time.seconds(30), Time.seconds(10)))
                // 泛型1：输入数据类型 泛型2：输出数据类型 泛型3：key的类型 泛型4：窗口类型
                .process(new ProcessWindowFunction<EventBean, Tuple3<String, String, Double>, String, TimeWindow>() {
                    /***
                     * @Param key: 传入的窗口是属于哪个key的
                     * @Param context: 上下文信息
                     * @Param elements: 传入窗口的数据的迭代器
                     * @Param out: 数据输出
                     **/
                    @Override
                    public void process(String key, ProcessWindowFunction<EventBean, Tuple3<String, String, Double>, String, TimeWindow>.Context context,
                                        Iterable<EventBean> elements, Collector<Tuple3<String, String, Double>> out) throws Exception {

                        // 构造一个hashMap来记录 每一个事件发生的总次数，和行为总时长
                        HashMap<String, Tuple2<Integer, Long>> hashMap = new HashMap<>();

                        for (EventBean element : elements) {
                            System.out.println(element);
                            String eventId = element.getEventId();
                            int actTimelong = element.getActTimelong();

                            Tuple2<Integer, Long> countAndTime = hashMap.getOrDefault(eventId, Tuple2.of(0, 0L));
                            System.out.println(countAndTime);
                            hashMap.put(eventId, Tuple2.of(countAndTime.f0 + 1, countAndTime.f1 + actTimelong));
                        }

                        System.out.println("hashMap");
                        System.out.println(hashMap);
                        System.out.println("hashMap");

                        // 定义treeSet 的排序规则
                        TreeSet<Tuple2<String, Double>> treeSet = new TreeSet<>(new Comparator<Tuple2<String, Double>>() {
                            @Override
                            public int compare(Tuple2<String, Double> o1, Tuple2<String, Double> o2) {
                                return Double.compare(o2.f1, o1.f1);
                            }
                        });

                        // 排序 取top2
                        for (Map.Entry<String, Tuple2<Integer, Long>> entry : hashMap.entrySet()) {
                            String eventId = entry.getKey();
                            Tuple2<Integer, Long> value = entry.getValue();
                            treeSet.add(Tuple2.of(eventId, (double) value.f1 / value.f0));
                            if (treeSet.size() > 2) treeSet.remove(treeSet.last());
                        }

                        // 输出
                        for (Tuple2<String, Double> set : treeSet) {
                            out.collect(Tuple3.of(key, set.f0, set.f1));
                        }
                    }
                });
        // resultStream5.print("r5");
        env.execute();
    }
}
