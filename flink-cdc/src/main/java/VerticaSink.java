import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.druid.pool.DruidPooledConnection;
import org.apache.flink.api.common.eventtime.Watermark;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.sink.RichSinkFunction;
import org.apache.flink.streaming.api.functions.sink.SinkFunction;
import utils.DruidPoolUtil;


public class VerticaSink extends RichSinkFunction<Object> {
    DruidPooledConnection conn = null;

    @Override
    public void open(Configuration parameters) throws Exception {
        DruidDataSource dataSource = DruidPoolUtil.getDruidDataSource("vertica");
        conn = dataSource.getConnection();
    }

    @Override
    public void invoke(Object value, Context context) throws Exception {
        super.invoke(value, context);
    }

    @Override
    public void close() throws Exception {
        if (conn!=null){
            conn.close();
        }
    }
}
