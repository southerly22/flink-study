package huorong.hr;

import org.apache.flink.api.common.JobExecutionResult;
import org.apache.flink.api.common.accumulators.IntCounter;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.common.functions.RichFilterFunction;
import org.apache.flink.api.common.functions.RichMapFunction;
import org.apache.flink.api.common.state.MapState;
import org.apache.flink.api.common.state.MapStateDescriptor;
import org.apache.flink.api.common.state.ValueState;
import org.apache.flink.api.common.state.ValueStateDescriptor;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.connector.jdbc.*;
import org.apache.flink.streaming.api.CheckpointingMode;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.sink.SinkFunction;
import org.apache.flink.util.function.SerializableSupplier;
import org.h2.jdbcx.JdbcDataSource;
import org.omg.PortableInterceptor.INACTIVE;

import javax.sql.XADataSource;
import java.awt.image.TileObserver;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashSet;

/**
 * @author lzx
 * @date 2023/6/19 13:28
 * @description: TODO
 */
public class SampleInfo_Split {

    public static void main(String[] args) throws Exception {
        Configuration conf = new Configuration();
        conf.setInteger("rest.port",8085);
        StreamExecutionEnvironment env = StreamExecutionEnvironment.createLocalEnvironmentWithWebUI(conf);
        env.enableCheckpointing(1000, CheckpointingMode.EXACTLY_ONCE);
        env.getCheckpointConfig().setCheckpointStorage("file:///D:\\WorkPlace\\flink-study\\flink-hbase\\ck");

        String sql = "select \"md5\",\"seaweed_path\" from USDP.SAMPLE_INFO";
        DataStreamSource<Sample> source = env.addSource(new PhoenixSource(sql));

        SingleOutputStreamOperator<Sample> resDS = source.filter(new RichFilterFunction<Sample>() {
            @Override
            public boolean filter(Sample value) throws Exception {
                return value.getMd5() != null && value.getSeaweed_path() != null;
            }
        });

        SinkFunction<Sample> sink = JdbcSink.sink(
                "upsert into TEST.MD5_SEAWEEDPATH2(\"md5\",\"seaweed_path\") values(?,?)",
                new JdbcStatementBuilder<Sample>() {
                    @Override
                    public void accept(PreparedStatement ps, Sample sample) throws SQLException {
                        ps.setString(1, sample.getMd5());
                        ps.setString(2, sample.getSeaweed_path());
                    }
                },
                JdbcExecutionOptions.builder()
                        .withBatchSize(1)
                        .withBatchIntervalMs(1000)
                        .build(),
                new JdbcConnectionOptions.JdbcConnectionOptionsBuilder()
                        .withUrl("jdbc:phoenix:192.168.1.118:2181;AutoCommit=true")
                        .withDriverName("org.apache.phoenix.jdbc.PhoenixDriver")
                        .build()
        );

        resDS.addSink(sink);

        env.execute();
    }
}
