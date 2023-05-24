package flink_core.asyncio;

import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.common.typeinfo.TypeHint;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.api.datastream.AsyncDataStream;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

import java.time.Instant;
import java.util.concurrent.TimeUnit;

/**
 * @author lzx
 * @date 2023/05/24 18:07
 **/
public class DorisAsync {
    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        DataStreamSource<String> source = env.fromElements("1", "2", "3", "4", "5", "6", "7", "8","9","10");

        SingleOutputStreamOperator<Tuple2<String, String>> asyncDS = AsyncDataStream.orderedWait(
                source,
                new MyAsyncDorisFunc(8),
                30000,
                TimeUnit.MILLISECONDS,
                10  //最大异步并发请求数量
        );

        asyncDS.map(t2 -> {
                    return "outPut:" + t2.f0 + "," +t2.f1 + Instant.now();
                }).returns(TypeInformation.of(new TypeHint<String>() {
                }))
                .print();
        //asyncDS.print();
        env.execute("mysqlAsync");
    }
}
