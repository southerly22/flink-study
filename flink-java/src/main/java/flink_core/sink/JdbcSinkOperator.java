package flink_core.sink;

import com.mysql.cj.jdbc.MysqlXADataSource;
import flink_core.source.EventLog;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.connector.jdbc.*;
import org.apache.flink.streaming.api.CheckpointingMode;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.sink.SinkFunction;
import org.apache.flink.util.function.SerializableSupplier;

import javax.sql.XADataSource;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;

/**
 * Jdbc Sink
 *
 * @author lzx
 * @date 2023/05/19 10:37
 * @description jdbc sink算子，包含实现eos的算子
 **/
public class JdbcSinkOperator {
    public static void main(String[] args) throws Exception {
        Configuration conf = new Configuration();
        conf.setInteger("rest.port", 8085);

        StreamExecutionEnvironment env = StreamExecutionEnvironment.createLocalEnvironmentWithWebUI(conf);
        env.enableCheckpointing(5000, CheckpointingMode.EXACTLY_ONCE);
        env.getCheckpointConfig().setCheckpointStorage("file:////Users/liuzhixin/codeplace/flink-study/flink-java/ck");

        //todo 构建source
        DataStreamSource<EventLog> streamSource = env.addSource(new LzxSourceFunction());

        // todo 构建 具有EOS特性的 sink
        SinkFunction<EventLog> exactlyOnceSink = JdbcSink.exactlyOnceSink(
                "insert into EventLog values (?,?,?,?,?);",
                new JdbcStatementBuilder<EventLog>() {
                    @Override
                    public void accept(PreparedStatement ps, EventLog e) throws SQLException {
                        ps.setLong(1, e.getGuid());
                        ps.setString(2, e.getSessionId());
                        ps.setString(3, e.getEventId());
                        ps.setTimestamp(4, new Timestamp(e.getTimeStamp()));
                        ps.setString(5, e.getEventInfo().toString());
                        System.out.println(ps);
                    }
                },
                JdbcExecutionOptions.builder()
                        .withBatchSize(10)
                        .withMaxRetries(0)
                        .build()
                , JdbcExactlyOnceOptions.builder()
                        .withTransactionPerConnection(true)
                        .build()
                , new SerializableSupplier<XADataSource>() {
                    @Override
                    public XADataSource get() {
                        MysqlXADataSource xaDataSource = new MysqlXADataSource();
                        xaDataSource.setUrl("jdbc:mysql://localhost:3306/test?useUnicode=true&characterEncoding=UTF-8");
                        xaDataSource.setUser("root");
                        xaDataSource.setPassword("123456");
                        return xaDataSource;
                    }
                }
        );

        // todo 构建普通 sink
        SinkFunction<EventLog> commonSink = JdbcSink.sink(
                "insert into EventLog values (?,?,?,?,?);",
                new JdbcStatementBuilder<EventLog>() {
                    @Override
                    public void accept(PreparedStatement ps, EventLog e) throws SQLException {
                        ps.setLong(1, e.getGuid());
                        ps.setString(2, e.getSessionId());
                        ps.setString(3, e.getEventId());
                        ps.setTimestamp(4, new Timestamp(e.getTimeStamp()));
                        ps.setString(5, e.getEventInfo().toString());
                        System.out.println(ps);
                    }
                },
                JdbcExecutionOptions.builder()
                        .withBatchSize(10)
                        .withMaxRetries(0)
                        .build(),
                new JdbcConnectionOptions.JdbcConnectionOptionsBuilder()
                        .withUrl("jdbc:mysql://localhost:3306/test?useUnicode=true&characterEncoding=UTF-8")
                        .withDriverName("com.mysql.cj.jdbc.Driver")
                        .withUsername("root")
                        .withPassword("123456")
                        .build()


        );

        //streamSource.addSink(exactlyOnceSink).name("eosSink");  //eos sink
        streamSource.addSink(commonSink).name("commonSink"); // common sink
        env.execute("Jdbc-Sink");
    }
}
