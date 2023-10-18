import org.apache.flink.api.common.eventtime.Watermark;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.sink.RichSinkFunction;
import org.apache.flink.streaming.api.functions.sink.SinkFunction;

public class VerticaSink extends RichSinkFunction<Object> {
    @Override
    public void open(Configuration parameters) throws Exception {
        super.open(parameters);
    }

    @Override
    public void invoke(Object value, Context context) throws Exception {
        super.invoke(value, context);
    }

    @Override
    public void close() throws Exception {
        super.close();
    }
}
