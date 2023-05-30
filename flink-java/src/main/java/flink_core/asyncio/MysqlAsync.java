package flink_core.asyncio;

import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.api.datastream.AsyncDataStream;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.table.planner.expressions.In;

import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * flink读取Mysql异步IO的方式
 *
 * @author lzx
 * @date 2023/05/23 11:34
 **/
public class MysqlAsync {
    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        //DataStreamSource<String> source = env.socketTextStream("localhost", 9999);

        ArrayList<String> arrayList = new ArrayList<>();
        for (int i = 0; i < 1000; i++) {
                arrayList.add(String.valueOf(new Random().nextInt(999)));
        }
        DataStreamSource<String> source = env.fromCollection(arrayList);

        source.print();

        SingleOutputStreamOperator<Tuple2<String, String>> asyncDS = AsyncDataStream.orderedWait(
                source,
                new MyAsyncMysqlFunc(10),
                3000,
                TimeUnit.MILLISECONDS,
                10  //最大异步并发请求数量
        );

        asyncDS.print();
        env.execute("mysqlAsync");
    }
}
