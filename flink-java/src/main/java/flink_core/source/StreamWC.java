package flink_core.source;

import org.apache.flink.api.common.typeinfo.Types;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.api.java.utils.ParameterTool;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.util.Collector;

public class StreamWC {
    public static void main(String[] args) throws Exception {

        //解析参数
        ParameterTool parameterTool = ParameterTool.fromArgs(args);

        String host = parameterTool.get("host");
        int port = parameterTool.getInt("port");

        System.out.println("host = " + host);
        System.out.println("port = " + port);


        // 创建流式 api 执行环境
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        DataStreamSource<String> lineStream = env.socketTextStream("hadooptest02", 7777);

        lineStream.flatMap((String line, Collector<Tuple2<String, Long>> out) -> {
                    for (String word : line.split(" ")) {
                        out.collect(Tuple2.of(word, 1L));
                    }
                }).returns(Types.TUPLE(Types.STRING, Types.LONG))
                .keyBy(data -> data.f0)
                .sum(1)
                .print();

        //启动
        env.execute();
    }
}
