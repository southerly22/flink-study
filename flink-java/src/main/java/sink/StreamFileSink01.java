package sink;

import com.alibaba.fastjson.JSON;
import org.apache.flink.api.common.serialization.SimpleStringEncoder;
import org.apache.flink.connector.file.sink.FileSink;
import org.apache.flink.core.fs.Path;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.sink.filesystem.OutputFileConfig;
import org.apache.flink.streaming.api.functions.sink.filesystem.bucketassigners.DateTimeBucketAssigner;
import org.apache.flink.streaming.api.functions.sink.filesystem.rollingpolicies.DefaultRollingPolicy;
import source.EventLog;
import source.MyRichSourceFunction;


/***
 * @Author: lzx
 * @Description: 写数据到文件中
 * @Date: 2022/12/20
 **/
public class StreamFileSink01 {
    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        //添加source
        DataStreamSource<EventLog> streamSource = env.addSource(new MyRichSourceFunction());

        //构建一个fileSink
        FileSink<String> fileSink = FileSink.forRowFormat(new Path("D:\\testdata\\aa"), new SimpleStringEncoder<String>("UTF-8"))
                .withRollingPolicy(
                        DefaultRollingPolicy //文件滚动策略
                                .builder()
                                .withRolloverInterval(1000)
                                .withMaxPartSize(1024 * 1024)
                                .build()

                )
                // 分桶的策略 子文件夹 (每小时一个文件夹)
                .withBucketAssigner(new DateTimeBucketAssigner<String>())
                .withBucketCheckInterval(5) //桶检查间隔
                .withOutputFileConfig( // 输出文件名配置
                        OutputFileConfig.builder()
                                .withPartPrefix("l")
                                .withPartSuffix("zx")
                                .build()
                ).build();

        streamSource.map(JSON::toJSONString)
                // .print();
                        .sinkTo(fileSink);



        env.execute();
    }
}
