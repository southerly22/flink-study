package huorong.hr;

import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.sink.RichSinkFunction;
import org.apache.flink.streaming.api.functions.sink.SinkFunction;

/**
 * @author lzx
 * @date 2023/6/21 13:36
 * @description: TODO
 */
public class PhoenixSink extends RichSinkFunction<Sample> {

    @Override
    public void open(Configuration parameters) throws Exception {
        super.open(parameters);
    }

    @Override
    public void invoke(Sample value, Context context) throws Exception {
        super.invoke(value, context);
    }

    @Override
    public void close() throws Exception {
        super.close();
    }
}
