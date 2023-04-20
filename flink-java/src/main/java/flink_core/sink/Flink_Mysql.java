package flink_core.sink;

import org.apache.flink.connector.jdbc.JdbcConnectionOptions;
import org.apache.flink.connector.jdbc.JdbcExecutionOptions;
import org.apache.flink.connector.jdbc.JdbcSink;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

/***
 * @Author: lzx
 * @Description: flink向 mysql中写入数据
 * @Date: 2022/12/20
 **/
public class Flink_Mysql {
    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        env.fromElements(
                new Event("Mary", "./home", 1000L),
                new Event("Bob", "./cart", 2000L)
        ).addSink(
                JdbcSink.sink(
                        "insert into lzx(name,catalogue,score) values(?,?,?)",
                        (statement, t) -> {
                            statement.setString(1, t.getName());
                            statement.setString(2, t.getCatalogue());
                            statement.setLong(3, t.getScore());
                        },
                        JdbcExecutionOptions.builder()
                                .withBatchSize(100)
                                .withBatchIntervalMs(200)
                                .withMaxRetries(3)
                                .build(),
                        new JdbcConnectionOptions.JdbcConnectionOptionsBuilder()
                                .withUrl("jdbc:mysql://localhost:3306/test")
                                .withDriverName("com.mysql.cj.jdbc.Driver")
                                .withUsername("root")
                                .withPassword("123456")
                                .build()
                )
        );
        env.execute();
    }
}
