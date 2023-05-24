package flink_core.asyncio;

import jdk.nashorn.internal.objects.annotations.Where;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.streaming.api.datastream.AsyncDataStream;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.source.SourceFunction;

import java.util.concurrent.TimeUnit;

/**
 * 异步IO示例
 *
 * @author lzx
 * @date 2023/05/24 14:33
 * todo 生成6条数据，模拟异步查询之后加上时间戳输出
 **/
public class AsyncIoDemo {
    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(1);

        final int maxCount = 6;
        final int taskNum = 1;
        final long timeout = 40000;

        SampleAsyncFunc sampleAsyncFunc = new SampleAsyncFunc();

        DataStreamSource<Integer> inputStream = env.addSource(new SimpleSource(maxCount));

        SingleOutputStreamOperator<String> res = AsyncDataStream.unorderedWait(
                inputStream,
                sampleAsyncFunc,
                timeout,
                TimeUnit.MILLISECONDS,
                10
        ).setParallelism(taskNum);

        res.map(new MapFunction<String, String>() {
            @Override
            public String map(String value) throws Exception {
                return value + "," + System.currentTimeMillis();
            }
        }).print("异步查询输出 >>");

        env.execute();
    }

    static class SimpleSource implements SourceFunction<Integer>{
        private volatile boolean isRunning = true;
        private int counter = 0;
        private int start =0;

        public SimpleSource(int maxNum) {
            this.counter = maxNum;
        }

        @Override
        public void run(SourceContext<Integer> ctx) throws Exception {
            while ((start < counter || counter == -1) && isRunning){
                synchronized (ctx.getCheckpointLock()){
                    System.out.println("send data " + start);
                    ctx.collect(start);
                    ++start;
                }
               Thread.sleep(10L);
            }
        }

        @Override
        public void cancel() {
            isRunning = false;
        }
    }
}
