package source;

import org.apache.flink.api.common.typeinfo.Types;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.util.Collector;

public class StreamWC_Local {
    public static void main(String[] args) throws Exception {

        // flink本地环境
        Configuration conf = new Configuration();
        conf.setInteger("rest.port",8081);
        StreamExecutionEnvironment env = StreamExecutionEnvironment.createLocalEnvironmentWithWebUI(conf);

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
