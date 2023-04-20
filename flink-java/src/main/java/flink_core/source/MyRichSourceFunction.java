package flink_core.source;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.RandomUtils;
import org.apache.flink.api.common.functions.RuntimeContext;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.source.RichSourceFunction;

import java.util.HashMap;

public class MyRichSourceFunction extends RichSourceFunction<EventLog> {
    volatile boolean flag = true;

    @Override
    public void open(Configuration parameters) throws Exception {
        RuntimeContext runtimeContext = getRuntimeContext();
        System.out.println("程序开始执行");
        System.out.println("runtimeContext.getJobId() = " + runtimeContext.getJobId());
        System.out.println("runtimeContext.getTaskName() = " + runtimeContext.getTaskName());
    }

    @Override
    public void run(SourceContext<EventLog> sourceContext) throws Exception {
        EventLog eventLog = new EventLog();
        HashMap<String, String> map = new HashMap<>();
        while (flag) {
            eventLog.setGuid(RandomUtils.nextLong(1, 100));
            eventLog.setSessionId(RandomStringUtils.randomAlphanumeric(1024).toUpperCase());
            eventLog.setEventId(RandomStringUtils.randomAlphabetic(5));
            eventLog.setTimeStamp(System.currentTimeMillis());
            map.put(String.valueOf(RandomUtils.nextInt(1, 100)), RandomStringUtils.randomAlphabetic(3));
            eventLog.setEventInfo(map);

            sourceContext.collect(eventLog); //生成数据加载到流里面
            map.clear();

            Thread.sleep(1000L);
        }
    }

    @Override
    public void cancel() {
        flag = false;
    }

    @Override
    public void close() throws Exception {
        super.close();
        System.out.println("程序关闭");
    }
}
