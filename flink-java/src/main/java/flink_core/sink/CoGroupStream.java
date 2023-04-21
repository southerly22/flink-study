package flink_core.sink;

import org.apache.flink.api.common.functions.CoGroupFunction;
import org.apache.flink.api.common.typeinfo.TypeHint;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.api.java.tuple.Tuple3;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.CheckpointingMode;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.windowing.assigners.TumblingProcessingTimeWindows;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.util.Collector;


/**
 * @author lzx
 * @date 2023/4/21 10:41
 * @description: TODO CoGroup 是 join底层实现 函数
 */
public class CoGroupStream {
    public static void main(String[] args) throws Exception {
        Configuration configuration = new Configuration();
        configuration.setInteger("rest.port", 8085);
        StreamExecutionEnvironment env = StreamExecutionEnvironment.createLocalEnvironmentWithWebUI(configuration);
        env.setParallelism(1);

        //开启ck
        env.enableCheckpointing(5000, CheckpointingMode.EXACTLY_ONCE);
        env.getCheckpointConfig().setCheckpointStorage("file:///D:\\WorkPlace\\flink-study\\flink-java\\ck\\");

        // 创建2个流
        // ("1,aa,m,18", "2,bb,m,28", "3,cc,f,38");
        // ("1,aa,m,18", "2,bb,m,28", "4,cc,f,38");
        DataStreamSource<String> stream1 = env.socketTextStream("192.168.1.250", 8888);
        DataStreamSource<String> stream2 = env.socketTextStream("192.168.1.250", 9999);

        // id name
        SingleOutputStreamOperator<Tuple2<String, String>> s1 = stream1.map(s -> {
            String[] arr = s.split(",");
            return Tuple2.of(arr[0], arr[1]);
        }).returns(new TypeHint<Tuple2<String, String>>() {
        });

        // id name sex
        SingleOutputStreamOperator<Tuple3<String, String, String>> s2 = stream2.map(s -> {
            String[] arr = s.split(",");
            return Tuple3.of(arr[0], arr[1], arr[2]);
        }).returns(new TypeHint<Tuple3<String, String, String>>() {
        });

        s1.coGroup(s2)
                .where(tp -> tp.f0) // 左流 f0字段
                .equalTo(tp -> tp.f0) // 右流 f0 字段
                .window(TumblingProcessingTimeWindows.of(Time.seconds(20))) // 滚动窗口
                .apply(new CoGroupFunction<Tuple2<String, String>, Tuple3<String, String, String>, Tuple2<String, String>>() {
                    /**
                     *
                     * @param first  是协同组中的第一个流的数据
                     * @param second 是协同组中的第二个流的数据
                     * @param out 是处理结果的输出器
                     */
                    @Override
                    public void coGroup(Iterable<Tuple2<String, String>> first, Iterable<Tuple3<String, String, String>> second, Collector<Tuple2<String, String>> out) throws Exception {
                        // 在这里实现 left join
                        for (Tuple2<String, String> t1 : first) {
                            boolean flag = false;
                            for (Tuple3<String, String, String> t2 : second) {
                                out.collect(new Tuple2<>(t1.f0 + "," + t1.f1, t2.f0 + "," + t2.f1 + "," + t2.f2));
                                flag = true;
                            }
                            if (!flag) { // 右表无关联到的数据
                                out.collect(new Tuple2<>(t1.f0 + "," + t1.f1, null));
                            }
                        }
                    }
                }).print();

        env.execute("CoGroupStream");
    }
}
