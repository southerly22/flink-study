package flink_core.test;

import org.apache.flink.api.common.typeinfo.Types;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.util.Collector;

import java.util.Arrays;
import java.util.Collection;

/**
 * s
 *
 * @author lzx
 * @date 2023/08/18 16:21
 **/

public class test {

    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(1);

        //DataStreamSource<String> source = env.socketTextStream("localhost", 9999);

        DataStreamSource<String> source2 = env.fromElements("a b c d");

        source2.flatMap((String in, Collector<Tuple2<String, Integer>> out)->{
            Arrays.stream(in.split(" ")).map(s-> Tuple2.of(s,1)).forEach(t2-> out.collect(t2));
        }).returns(Types.TUPLE(Types.STRING, Types.INT));

        //source2.flatMap((FlatMapFunction<String, Tuple2<String, Integer>>) (value, out) -> {
        //    String[] str = value.split(" ");
        //    for (int i = 0; i < str.length; i++) {
        //        out.collect(Tuple2.of(str[i],1));
        //    }
        //}).keyBy(s->s.f0).sum(1).print();


        env.execute();
    }
}
