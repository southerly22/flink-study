package flink_core.source;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.RandomUtils;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.source.ParallelSourceFunction;
import org.apache.flink.streaming.api.functions.source.RichParallelSourceFunction;
import org.apache.flink.streaming.api.functions.source.SourceFunction;

import java.util.HashMap;

public class CustomSourceFunction {

    public static void main(String[] args) throws Exception {

        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        DataStreamSource<EventLog> streamSource = env.addSource(new MySourceFunction());
        // DataStreamSource<EventLog> streamSource = env.addSource(new MyRichSourceFunction());
        streamSource.print();

        env.execute();
    }
}

/**
 * 自定义source
 * 可以实现 ourceFunction 或者   RichSourceFunction , 这两者都是非并行的  source 算子
 * 也 可 实 现ParallelSourceFunction 或 者  RichParallelSourceFunction , 这 两 者 都 是 可 并 行 的

 * source 算子
 * -- 带 Rich 的，都拥有   open() ,close() ,getRuntimeContext() 生命周期方法
 * -- 带 Parallel 的，都可多实例并行执行
 */

// todo 方式一
class MySourceFunction implements SourceFunction<EventLog>{

    volatile boolean flag = true;

    // 生产数据
    @Override
    public void run(SourceContext<EventLog> sourceContext) throws Exception {
        EventLog eventLog = new EventLog();
        HashMap<String, String> map = new HashMap<>();
        while (flag){
            eventLog.setGuid(RandomUtils.nextLong(1,100));
            eventLog.setSessionId(RandomStringUtils.randomAlphanumeric(12).toUpperCase());
            eventLog.setEventId(RandomStringUtils.randomAlphabetic(5));
            eventLog.setTimeStamp(System.currentTimeMillis());
            map.put(String.valueOf(RandomUtils.nextInt(1,100)),RandomStringUtils.randomAlphabetic(3));
            eventLog.setEventInfo(map);

            sourceContext.collect(eventLog); //生成数据加载到流里面
            map.clear();

            Thread.sleep(3000L);
        }
    }

    //取消 job
    @Override
    public void cancel() {
        flag = false;
    }
}

// todo 方式二
class MyParaSourceFunction implements ParallelSourceFunction<EventLog>{
    @Override
    public void run(SourceContext<EventLog> sourceContext) throws Exception {

    }

    @Override
    public void cancel() {

    }
}

class MyRichParallelSourceFunction extends RichParallelSourceFunction<EventLog> {

    @Override
    public void run(SourceContext<EventLog> sourceContext) throws Exception {

    }

    @Override
    public void cancel() {

    }
}
