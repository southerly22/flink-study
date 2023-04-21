package flink_core.sink;

import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.CheckpointingMode;
import org.apache.flink.streaming.api.datastream.ConnectedStreams;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.co.CoFlatMapFunction;
import org.apache.flink.streaming.api.functions.co.CoMapFunction;
import org.apache.flink.util.Collector;

/**
 * @author lzx
 * @date 2023/4/21 9:47
 * @description: TODO connect可以将两个数据类型一样、不一样的流连接在一起，与Union方法不同的是，
 *                    新的流ConnectedStream内部两个流依然是相互独立的。好处是两个流可以共享 State
 */
public class ConnectedStream_2 {
    public static void main(String[] args) throws Exception {
        Configuration configuration = new Configuration();
        configuration.setInteger("rest.port",8085);
        StreamExecutionEnvironment env = StreamExecutionEnvironment.createLocalEnvironmentWithWebUI(configuration);

        //开启ck
        env.enableCheckpointing(5000, CheckpointingMode.EXACTLY_ONCE);
        env.getCheckpointConfig().setCheckpointStorage("file:///D:\\WorkPlace\\flink-study\\flink-java\\ck\\");

        // 创建两个流
        DataStreamSource<String> wordDS = env.fromElements("a b c", "d e f");
        DataStreamSource<String> numberDS = env.fromElements("1,2,3", "4,5,6");

        // 将两个DS 连接在一起
        ConnectedStreams<String, String> connectedStreams = wordDS.connect(numberDS);

        // 对ConnectedStreams中的两个流分别调用不同逻辑的map方法
        /***
         * @Author: lzx
         * @Description: CoFlatMapFunction传入3个泛型
         *              1.第一个输入DS的类型
         *              2.第二个输入DS的类型
         *              3.返回结果的数据类型
         **/
        connectedStreams.flatMap(new CoFlatMapFunction<String,String,String>(){

            @Override
            public void flatMap1(String value, Collector<String> out) throws Exception {
                for (String s : value.split(" ")) {
                    out.collect(s);
                }
            }

            @Override
            public void flatMap2(String value, Collector<String> out) throws Exception {
                for (String n : value.split(",")) {
                    out.collect(n);
                }
            }
        }).print("connectedStreams");

        env.execute("Connect_Union_Stream_2");
    }
}
