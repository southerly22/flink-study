package flink_core.sink;

import org.apache.commons.lang.StringUtils;
import org.apache.flink.api.common.functions.Function;
import org.apache.flink.api.common.functions.JoinFunction;
import org.apache.flink.api.common.typeinfo.TypeHint;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.api.java.tuple.Tuple3;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.CheckpointingMode;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.windowing.assigners.TumblingProcessingTimeWindows;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.util.Collector;

import java.util.Arrays;


/**
 * @author lzx
 * @date 2023/4/21 10:41
 * @description: TODO Inner Join函数实现
 */
public class InnerJoinpStream {
    public static void main(String[] args) throws Exception {
        Configuration configuration = new Configuration();
        configuration.setInteger("rest.port", 8085);
        StreamExecutionEnvironment env = StreamExecutionEnvironment.createLocalEnvironmentWithWebUI(configuration);
        env.setParallelism(1);

        //开启ck
        env.enableCheckpointing(5000, CheckpointingMode.EXACTLY_ONCE);
        env.getCheckpointConfig().setCheckpointStorage("file:///D:\\WorkPlace\\flink-study\\flink-java\\ck\\");

        // 创建2个流
        // DataStreamSource<String> stream1 = env.fromElements("1,aa,m,18", "2,bb,m,28", "3,cc,f,38");
        // DataStreamSource<String> stream2 = env.fromElements("1:aa:m:18", "2:bb:m:28", "4:cc:f:38");

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

        DataStream<String> resDS = s1.join(s2)
                .where(tp -> tp.f0)
                .equalTo(tp -> tp.f0)
                .window(TumblingProcessingTimeWindows.of(Time.seconds(10))) //实质上join只能在窗口内执行
                .apply(new JoinFunction<Tuple2<String, String>, Tuple3<String, String, String>, String>() {
                    @Override
                    public String join(Tuple2<String, String> first, Tuple3<String, String, String> second) throws Exception {
                        return StringUtils.join(Arrays.asList(
                                first.f0,
                                first.f1,
                                second.f0,
                                second.f1,
                                second.f2
                        ), '\t');
                    }
                });

        resDS.print();
        env.execute("Stream");
    }
}
