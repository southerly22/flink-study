import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.druid.pool.DruidPooledConnection;
import entity.CdcDemo;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.flink.api.common.eventtime.Watermark;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.sink.RichSinkFunction;
import org.apache.flink.streaming.api.functions.sink.SinkFunction;
import scala.reflect.internal.Trees;
import utils.DruidPoolUtil;

import java.util.List;
import java.util.logging.Logger;


public class VerticaSink<T> extends RichSinkFunction<List<T>> {
    private static final Log logger = LogFactory.getFactory().getInstance(VerticaSink.class);
    DruidPooledConnection conn = null;
    private String dataBaseName;
    private String tableName;

    public VerticaSink(String dataBaseName, String tableName) {
        this.dataBaseName = dataBaseName;
        this.tableName = tableName;
    }

    @Override
    public void open(Configuration parameters) throws Exception {
        DruidDataSource dataSource = DruidPoolUtil.getDruidDataSource("mysql");
        conn = dataSource.getConnection();
    }

    @Override
    public void invoke(List<T> value, Context context) throws Exception {
        logger.warn("开始写入！");
        if (value.size() > 0) {
            MyJdbcUtil.insertTable(value,tableName,conn);
        }
    }

    @Override
    public void close() throws Exception {
        if (conn!=null){
            conn.close();
        }
    }
}
