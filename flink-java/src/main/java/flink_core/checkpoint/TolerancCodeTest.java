package flink_core.checkpoint;

import com.mysql.cj.jdbc.MysqlXADataSource;
import org.apache.commons.lang3.RandomUtils;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.functions.RichMapFunction;
import org.apache.flink.api.common.functions.RuntimeContext;
import org.apache.flink.api.common.restartstrategy.RestartStrategies;
import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.api.common.state.ValueState;
import org.apache.flink.api.common.state.ValueStateDescriptor;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.connector.jdbc.JdbcExactlyOnceOptions;
import org.apache.flink.connector.jdbc.JdbcExecutionOptions;
import org.apache.flink.connector.jdbc.JdbcSink;
import org.apache.flink.connector.jdbc.JdbcStatementBuilder;
import org.apache.flink.connector.kafka.source.KafkaSource;
import org.apache.flink.connector.kafka.source.enumerator.initializer.OffsetsInitializer;
import org.apache.flink.runtime.state.hashmap.HashMapStateBackend;
import org.apache.flink.streaming.api.CheckpointingMode;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.sink.SinkFunction;
import org.apache.flink.util.function.SerializableSupplier;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.OffsetResetStrategy;

import javax.sql.XADataSource;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * @author lzx
 * Flink 端到端精确一致性  容错能力的测试
 * @date 2023/05/15 13:07
 * <p>
 * 1.从kafka读取数据（里面有operator-state状态）
 * <p>
 * 2.处理过程中用到了 带状态的map 算子（里面用到了keyed-state状态，逻辑：输入一个字符串 变大写拼接上一个字符串输出）
 * <p>
 * 3.用 exactl-once的mysql-sink 算子输出数据（并且附带主键幂等的特性）
 **/
public class TolerancCodeTest {
    public static void main(String[] args) throws Exception {
        Configuration conf = new Configuration();
        conf.setInteger("rest.port",8085);
        //从保存点 启动程序
        //conf.setString("execution.savepoint.path", "file:////Users/liuzhixin/codeplace/flink-study/flink-java/ck");
        StreamExecutionEnvironment env = StreamExecutionEnvironment.createLocalEnvironmentWithWebUI(conf);
        env.setParallelism(1);
        /**
         *  checkpoint 容错机制
         */
        env.enableCheckpointing(1000, CheckpointingMode.EXACTLY_ONCE);
        env.getCheckpointConfig().setCheckpointStorage("file:///D:\\WorkPlace\\flink-study\\flink-java\\ck");
        /**
         *  task 容错机制
         */
        env.setRestartStrategy(RestartStrategies.fixedDelayRestart(3, 1000));
        /**
         *  state 状态后端
         */
        env.setStateBackend(new HashMapStateBackend());

        /**
         *  构造一个支持eos语义的 kafkaSource
         */
        KafkaSource<String> sourceOperator = KafkaSource.<String>builder()
                .setBootstrapServers("192.168.1.56:9092,192.168.1.61:9092,192.168.1.58:9092,192.168.3.71:9092,192.168.3.178:9092")
                .setTopics("eos0515")
                .setGroupId("test0515")
                .setValueOnlyDeserializer(new SimpleStringSchema())
                .setProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "false") // 不允许 kafkaconsumer自动提交消费位移到 __consumer_offsets

                // kafkaSource 做状态checkpoint时，默认会向__consumer_offsets 提交一下状态中记录的offset
                // 但是，flink的容错机制并不依赖__consumer_offsets的记录，所以关闭该默认机制
                .setProperty("commit.offsets.on.checkpoint", "false") // 指定是否在进行 checkpoint 时将消费位点提交至 Kafka broker

                //Kafka source 能够通过位点初始化器（OffsetsInitializer）来指定从不同的偏移量开始消费
                // kafkaSource启动时，获取offset 的策略是，如果是 committedOffsets 则是从之前所记录的offset 接着消费
                // 如果没有可用的之前记录的offsets，则用OffsetResetStrategy.LATEST 读取最新的offsets 开始消费
                .setStartingOffsets(OffsetsInitializer.committedOffsets(OffsetResetStrategy.LATEST))
                .build();
        /**
         *  构造一个支持精确一致性的 jdbcSink
         */
        SinkFunction<String> exactlyOnceJdbcSink = JdbcSink.exactlyOnceSink(
                "insert into t_eos values(?,?) on duplicate key update id = ?",
                new JdbcStatementBuilder<String>() {
                    @Override
                    public void accept(PreparedStatement preparedStatement, String s) throws SQLException {
                        String[] arr = s.split(",");
                        preparedStatement.setInt(1, Integer.parseInt(arr[0]));
                        preparedStatement.setString(2, arr[1]);
                        preparedStatement.setInt(3, Integer.parseInt(arr[0]));
                    }
                },
                JdbcExecutionOptions.builder()
                        .withBatchSize(1)
                        .withMaxRetries(3)
                        .build(),
                JdbcExactlyOnceOptions.builder()
                        .withTransactionPerConnection(true) //mysql 不支持在同一个连接上存在并行的多个未完成的事务，因此此参数必须为true
                        .build(),
                new SerializableSupplier<XADataSource>() {
                    @Override
                    public XADataSource get() {
                        // XADataSource就是jdbc连接，不过它是支持分布式事务的连接
                        // 而且它的构造方法，不同的数据库构造方法不同
                        MysqlXADataSource mysqlXADataSource = new MysqlXADataSource();
                        mysqlXADataSource.setUrl("jdbc:mysql://localhost:3306/test");
                        mysqlXADataSource.setUser("root");
                        mysqlXADataSource.setPassword("123456");
                        return mysqlXADataSource;
                    }
                }
        );

        /**
         *  处理逻辑
         */
        DataStreamSource<String> kafkaSourceStream = env.fromSource(sourceOperator, WatermarkStrategy.noWatermarks(), "kafkaSource");

        SingleOutputStreamOperator<String> streamWithState = kafkaSourceStream.keyBy(s -> "g1")
                .map(new RichMapFunction<String, String>() {
                    ValueState<String> valueState = null;

                    @Override
                    public void open(Configuration parameters) throws Exception {
                        RuntimeContext context = getRuntimeContext();
                        ValueStateDescriptor<String> stringValueStateDescriptor = new ValueStateDescriptor<>("valueState", String.class);
                        valueState = context.getState(stringValueStateDescriptor);
                    }

                    @Override
                    public String map(String element) throws Exception {
                        String preStr = valueState.value();
                        if (preStr== null) preStr="1";
                        valueState.update(element);

                        //自定义异常
                        if ("x".equals(element) && RandomUtils.nextInt(0,5) % 2==0){
                            throw new Exception("发生了一次异常。。。。");
                        }

                        return preStr + ":" + element.toUpperCase();
                        // return element;
                    }
                });

        // 流开始写入
        streamWithState.addSink(exactlyOnceJdbcSink);
        //streamWithState.sinkTo()
        env.execute();
    }
}
