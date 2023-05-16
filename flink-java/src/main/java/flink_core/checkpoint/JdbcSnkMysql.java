package flink_core.checkpoint;

import com.mysql.cj.jdbc.MysqlDataSource;
import com.mysql.cj.jdbc.MysqlXADataSource;
import lombok.Data;
import org.apache.flink.api.common.RuntimeExecutionMode;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.common.restartstrategy.RestartStrategies;
import org.apache.flink.api.common.typeinfo.TypeHint;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.connector.jdbc.*;
import org.apache.flink.streaming.api.CheckpointingMode;
import org.apache.flink.streaming.api.datastream.DataStreamSink;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.sink.SinkFunction;
import org.apache.flink.util.function.SerializableSupplier;

import javax.print.attribute.standard.MediaSize;
import javax.sql.XADataSource;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.Random;

/**
 * jdbcSinkMysql
 *
 * @author lzx
 * @date 2023/05/15 15:45
 **/
public class JdbcSnkMysql {
    public static void main(String[] args) throws Exception {
        Configuration conf = new Configuration();
        conf.setInteger("rest.port", 8085);
        StreamExecutionEnvironment env = StreamExecutionEnvironment.createLocalEnvironmentWithWebUI(conf);
        env.enableCheckpointing(1000, CheckpointingMode.EXACTLY_ONCE);
        env.getCheckpointConfig().setCheckpointStorage("file:///D:\\WorkPlace\\flink-study\\flink-java\\ck");
        env.setRestartStrategy(RestartStrategies.fixedDelayRestart(3,2000));
        // env.setParallelism(1);

        ArrayList<Student> students = new ArrayList<>();
        Random random = new Random();
        for (int i = 0; i < 20; i++) {
            boolean flag = true;
            if (i % 2 == 0) {
                flag = false;
            }
            Student student = new Student(i, "name_" + i, flag, random.nextInt(20), random.nextInt(100), new Timestamp(new Date().getTime()), new Timestamp(new Date().getTime()));
            students.add(student);
            // System.out.println(student);
        }

        DataStreamSource<Student> streamSource = env.fromCollection(students, TypeInformation.of(new TypeHint<Student>() {
        }));

        SinkFunction<Student> exactlySink = JdbcSink.exactlyOnceSink(
                "insert into student1 (id,name,sex,age,score,createTime,updateTime) values(?,?,?,?,?,?,?)",
                new JdbcStatementBuilder<Student>() {
                    @Override
                    public void accept(PreparedStatement ps, Student s) throws SQLException {
                        ps.setInt(1, s.id);
                        ps.setString(2, s.name);
                        ps.setBoolean(3, s.sex);
                        ps.setInt(4, s.age);
                        ps.setInt(5, s.score);
                        ps.setTimestamp(6, s.createTime);
                        ps.setTimestamp(7, s.updateTime);
                        System.out.println("------------->>> " + ps.toString());
                    }
                },
                JdbcExecutionOptions.builder()
                        .withBatchSize(1)
                        .withBatchIntervalMs(200)
                        .withMaxRetries(0)
                        .build(),
                JdbcExactlyOnceOptions.builder()
                        .withTransactionPerConnection(true)
                        .build(),
                new SerializableSupplier<XADataSource>() {
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

        SinkFunction<Student> jdbcSink = JdbcSink.sink(
                "insert into student1 (id,name,sex,age,score,createTime,updateTime) values(?,?,?,?,?,?,?)",
                new JdbcStatementBuilder<Student>() {
                    @Override
                    public void accept(PreparedStatement ps, Student s) throws SQLException {
                        ps.setInt(1, s.id);
                        ps.setString(2, s.name);
                        ps.setBoolean(3, s.sex);
                        ps.setInt(4, s.age);
                        ps.setInt(5, s.score);
                        ps.setTimestamp(6, s.createTime);
                        ps.setTimestamp(7, s.updateTime);
                    }
                },
                JdbcExecutionOptions.builder()
                        .withBatchSize(1)
                        .withMaxRetries(3)
                        .build(),
                new JdbcConnectionOptions.JdbcConnectionOptionsBuilder()
                        .withUrl("jdbc:mysql://localhost:3306/test?useUnicode=true&characterEncoding=UTF-8")
                        .withUsername("root")
                        .withPassword("123456")
                        .withDriverName("com.mysql.cj.jdbc.Driver")
                        .build()
        );

        // streamSource.addSink(exactlySink);
        streamSource.addSink(jdbcSink);

        env.execute("sink Mysql");
    }
}
