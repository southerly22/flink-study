package flink_core.cep;

import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

/** 动态cep 测试demo
 * @author lzx
 * @date 2023/06/12 14:45
 *
 *  案例描述：
 *    初始化规则为,同一userId连续出现两次状态为fail的情况,产生告警消息
 *    希望转换的规则为同一userId连续出现两次状态为success的情况,产生告警
 *    规则改变由本地文件控制
 **/
public class LoginFailWithCustomCEP {
    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(1);
        DataStreamSource<String> source = env.socketTextStream("localhost", 9999);
        source.print();



        env.execute();
    }
}
