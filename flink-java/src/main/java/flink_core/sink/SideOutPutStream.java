package flink_core.sink;

import flink_core.source.EventLog;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.CheckpointingMode;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.ProcessFunction;
import org.apache.flink.util.Collector;
import org.apache.flink.util.OutputTag;

/**
 * @author lzx
 * @date 2023/4/20 18:12
 * @description: TODO 侧输出流
 */
public class SideOutPutStream {
    public static void main(String[] args) throws Exception {
        Configuration configuration = new Configuration();
        configuration.setInteger("rest.port",8085);
        StreamExecutionEnvironment env = StreamExecutionEnvironment.createLocalEnvironmentWithWebUI(configuration);
        env.setParallelism(1);

        //开启ck
        env.enableCheckpointing(5000, CheckpointingMode.EXACTLY_ONCE);
        env.getCheckpointConfig().setCheckpointStorage("file:///D:\\WorkPlace\\flink-study\\flink-java\\ck\\");

        // 构造一个流
        DataStreamSource<EventLog> streamSource = env.addSource(new LzxSourceFunction());

        /***
         * @Author: lzx
         * @Description: 需求：将行为事件流进行分流
         *              appLaunch事件 分到一个流
         *              putBack事件  分到一个流
         *              其他事件 保持主流
         **/
        SingleOutputStreamOperator<EventLog> outputStreamOperator = streamSource.process(new ProcessFunction<EventLog, EventLog>() {
            /***
             * @Author: lzx
             * @Description:
             * @Date: 2023/4/20
             * @Param eventLog: 输入数据
             * @Param context: 上下文，提供 "侧输出流" 功能
             * @Param collector:  主流输出收集器
             **/
            @Override
            public void processElement(EventLog eventLog, ProcessFunction<EventLog, EventLog>.Context context, Collector<EventLog> collector) throws Exception {
                String eventId = eventLog.getEventId();
                if (eventId.contains("appLaunch")) {
                    context.output(new OutputTag<EventLog>("launch", TypeInformation.of(EventLog.class)),
                            eventLog);
                } else if (eventId.contains("putBack")) {
                    context.output(new OutputTag<>("back", TypeInformation.of(EventLog.class)),
                            eventLog);
                }
                collector.collect(eventLog);
            }
        });

        // 获取 launch 侧输出流数据
        DataStream<EventLog> launchStream = outputStreamOperator.getSideOutput(new OutputTag<>("launch", TypeInformation.of(EventLog.class)));

        // 获取 back 侧输出流数据
        DataStream<EventLog> backStream = outputStreamOperator.getSideOutput(new OutputTag<>("back", TypeInformation.of(EventLog.class)));

        launchStream.print("launch");
        backStream.print("back");
        outputStreamOperator.print("main");
        env.execute("SideOutPutStream");
    }
}
