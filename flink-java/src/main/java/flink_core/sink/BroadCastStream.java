package flink_core.sink;

import org.apache.flink.api.common.state.BroadcastState;
import org.apache.flink.api.common.state.MapStateDescriptor;
import org.apache.flink.api.common.state.ReadOnlyBroadcastState;
import org.apache.flink.api.common.typeinfo.TypeHint;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.api.java.tuple.Tuple3;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.CheckpointingMode;
import org.apache.flink.streaming.api.datastream.BroadcastConnectedStream;
import org.apache.flink.streaming.api.datastream.BroadcastStream;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.co.BroadcastProcessFunction;
import org.apache.flink.util.Collector;

/**
 * Flink广播流
 *
 * @author lzx
 * @date 2023/04/22 18:15
 **/
public class BroadCastStream {
    public static void main(String[] args) throws Exception {

        Configuration conf = new Configuration();
        conf.setInteger("rest.port", 8085);

        StreamExecutionEnvironment env = StreamExecutionEnvironment.createLocalEnvironmentWithWebUI(conf);
        env.setParallelism(1);

        // 开启 CK
        env.enableCheckpointing(3000, CheckpointingMode.EXACTLY_ONCE);
        env.getCheckpointConfig().setCheckpointStorage("file:///Users\\liuzhixin\\codeplace\\flink-study\\flink-java\\ck");

        // id eventId 日志信息  （1，132）
        DataStreamSource<String> stream1 = env.socketTextStream("localhost", 8888);

        SingleOutputStreamOperator<Tuple2<String, String>> s1 = stream1.map(str -> {
            String[] arr = str.split(",");
            return new Tuple2<String, String>(arr[0], arr[1]);
        }).returns(new TypeHint<Tuple2<String, String>>() {
        });

        // id age city 维表信息 （1，22，beijing）
        DataStreamSource<String> stream2 = env.socketTextStream("localhost", 9999);

        SingleOutputStreamOperator<Tuple3<String, String, String>> s2 = stream2.map(str -> {
            String[] arr = str.split(",");
            return new Tuple3<String, String, String>(arr[0], arr[1], arr[2]);
        }).returns(new TypeHint<Tuple3<String, String, String>>() {
        });

        /**
         *
         * @author lzx
         *  需求：
         *  s1 用户的事件流，一个人会反复出现
         *  s2 用户的维表信息，只会出现一次 且 时间不定（作为广播流）
         *  需要加工 流s1，把用户信息打宽，利用广播流技术
         */

        // 创建状态,结构类似于HashMap，这里key为id , value为（age,city）
        MapStateDescriptor<String, Tuple2<String, String>> userInfoStateDesc = new MapStateDescriptor<>(
                "userInfoStateDesc",
                TypeInformation.of(String.class),
                TypeInformation.of(new TypeHint<Tuple2<String, String>>() {
                })
        );

        // 传入状态，将字典流 s2 转为广播流
        BroadcastStream<Tuple3<String, String, String>> s2BroadcastStream = s2.broadcast(userInfoStateDesc);

        // 那个流要用到广播流 那个流就要去connect 广播流
        BroadcastConnectedStream<Tuple2<String, String>, Tuple3<String, String, String>> connectedStream = s1.connect(s2BroadcastStream);

        /**
         *  对连接了广播流的 'connectedStream'处理。
         *  核心思想：在processBroadcastElement 获取广播流数据，插入到广播状态里面
         *          在processElement 获取广播状态里数据，链接主流 把数据打宽
         *
         * @author lzx
         * @date 2023-04-22 18:35
         */
        SingleOutputStreamOperator<String> res = connectedStream.process(new BroadcastProcessFunction<Tuple2<String, String>, Tuple3<String, String, String>, String>() {
            @Override
            public void processElement(Tuple2<String, String> value, BroadcastProcessFunction<Tuple2<String, String>, Tuple3<String, String, String>, String>.ReadOnlyContext ctx, Collector<String> out) throws Exception {

                // 通过 ReadOnlyContext ctx 取到的广播状态对象，是一个 “只读 ” 的对象；
                ReadOnlyBroadcastState<String, Tuple2<String, String>> state = ctx.getBroadcastState(userInfoStateDesc);

                if (state != null) {
                    Tuple2<String, String> userInfo = state.get(value.f0);
                    out.collect(value.f0 + "," + value.f1 + "," + (userInfo == null ? null : userInfo.f0) + "," + (userInfo == null ? null : userInfo.f1));
                } else {
                    out.collect(value.f0 + "," + value.f1 + "(" + null + ")");
                }
            }

            @Override
            public void processBroadcastElement(Tuple3<String, String, String> value, BroadcastProcessFunction<Tuple2<String, String>, Tuple3<String, String, String>, String>.Context ctx, Collector<String> out) throws Exception {
                // 从上下文中 获取广播状态对象（可读可写）
                BroadcastState<String, Tuple2<String, String>> state = ctx.getBroadcastState(userInfoStateDesc);

                // 将获得的这条 广播流数据 填入到广播状态里面
                state.put(value.f0, Tuple2.of(value.f1, value.f2));
            }
        });

        s1.print("s1");
        s2.print("s2");
        res.print();

        env.execute("BroadCastStream");
    }
}
