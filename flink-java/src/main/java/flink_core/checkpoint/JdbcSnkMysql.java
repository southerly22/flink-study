package flink_core.checkpoint;

import com.mysql.cj.jdbc.MysqlXADataSource;
import lombok.Data;
import org.apache.flink.api.common.RuntimeExecutionMode;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.connector.jdbc.JdbcExactlyOnceOptions;
import org.apache.flink.connector.jdbc.JdbcExecutionOptions;
import org.apache.flink.connector.jdbc.JdbcSink;
import org.apache.flink.connector.jdbc.JdbcStatementBuilder;
import org.apache.flink.streaming.api.CheckpointingMode;
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
        ArrayList<Student> students = new ArrayList<>();
        Random random = new Random();
        for (int i = 0; i < 20; i++) {
            boolean flag = true;
            if (i%2==0) {
                flag = false;
            }
            Student student = new Student(i, "name_" + i, flag, random.nextInt(20), random.nextInt(100), new Timestamp(new Date().getTime()), new Timestamp(new Date().getTime()));
            students.add(student);
            System.out.println(student);
        }

        Configuration conf = new Configuration();
        conf.setInteger("rest.port",8085);
        StreamExecutionEnvironment env = StreamExecutionEnvironment.createLocalEnvironmentWithWebUI(conf);
        env.enableCheckpointing(1000, CheckpointingMode.EXACTLY_ONCE);
        env.getCheckpointConfig().setCheckpointStorage("file:////Users/liuzhixin/codeplace/flink-study/flink-java/ck");
        env.setParallelism(1);

        DataStreamSource<Student> dataStreamSource = env.fromCollection(students);
        // 创建Sink
        SinkFunction<Student> jdbcSink = JdbcSink.exactlyOnceSink(
                "insert into student (id,name,sex,age,score,createTime,updateTime) values(?,?,?,?,?,?,?)",
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
                        System.out.println("------------->>> "+ps.toString());
                    }
                },
                JdbcExecutionOptions.builder()
                        .withBatchSize(1)
                        .withMaxRetries(0)
                        .build(),
                JdbcExactlyOnceOptions.builder()
                        .withTransactionPerConnection(true)
                        .build(),
                new SerializableSupplier<XADataSource>() {
                    @Override
                    public XADataSource get() {
                        MysqlXADataSource xaDataSource = new MysqlXADataSource();
                        xaDataSource.setUrl("jdbc:mysql://localhost:3306/test");
                        xaDataSource.setUser("root");
                        xaDataSource.setPassword("123456");
                        return xaDataSource;
                    }
                }
        );

        dataStreamSource.addSink(jdbcSink).setParallelism(1).name("sink2Mysql");
        env.execute("sink Mysql");
    }
}
