package flink_core.sink;

import flink_core.source.EventLog;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.RandomUtils;
import org.apache.flink.streaming.api.functions.source.ParallelSourceFunction;
import org.apache.flink.streaming.api.functions.source.SourceFunction;

import java.util.HashMap;

/**
 * @author lzx
 * @date 2023/4/20 16:07
 * @description: TODO
 */
class LzxSourceFunction implements ParallelSourceFunction<EventLog> {

    volatile boolean flag = true;
    @Override
    public void run(SourceContext<EventLog> sourceContext) throws Exception {
        EventLog eventLog = new EventLog();
        String[] events = {"appLaunch","pageLoad","adShow","adClick","itemShare","itemCollect","putBack","wakeUp","appClose"};
        HashMap<String,String> eventInfoMap =  new HashMap<>();

        while (flag) {
            eventLog.setGuid(RandomUtils.nextLong(1,1000));
            eventLog.setSessionId(RandomStringUtils.randomAlphabetic(12).toUpperCase());
            eventLog.setTimeStamp(System.currentTimeMillis());
            eventLog.setEventId(events[RandomUtils.nextInt(0,events.length)]);

            eventInfoMap.put(RandomStringUtils.randomAlphabetic(1),RandomStringUtils.randomAlphabetic(2));
            eventLog.setEventInfo(eventInfoMap);

            sourceContext.collect(eventLog);

            eventInfoMap.clear();

            Thread.sleep(RandomUtils.nextInt(200,1500));
        }
    }

    @Override
    public void cancel() {
        flag = false;
    }
}
