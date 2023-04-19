package source;

import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.connector.kafka.source.KafkaSource;
import org.apache.flink.connector.kafka.source.enumerator.initializer.OffsetsInitializer;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

public class KafkaSourceDemo {
    public static void main(String[] args) throws Exception {

        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        KafkaSource<String> kafkaSource = KafkaSource.<String>builder()
                .setBootstrapServers("localhost:9094,localhost:9092,localhost:9093")
                .setTopics("lzx_test")
                .setGroupId("lzx_test0418")
                //kafka 偏移量的选择
                //OffsetsInitializer.latest()  最新的
                //OffsetsInitializer.earliest() //最早的
                //OffsetsInitializer.offsets(Map< TopicPartition,Long >) 自定位置
                .setStartingOffsets(OffsetsInitializer.earliest())
                //value 的反序列化器
                .setValueOnlyDeserializer(new SimpleStringSchema())

                //开启kafka的消费者自动提交偏移量设置
                // 他会把最新的offsets 提交到kafka_consumer  topic里面
                // 就算开启此机制，kafkaSource依然不依赖自定提交机制
                // （宕机时 重启时优先从flink的状态算子里面去获取offset《更可靠》）
                .setProperty("auto.offset.commit", "false")

                // 把source设置为有界流，此时用该source读取数据时，读到指定的位置，程序就会退出执行
                // 场景：补全数据，重跑某段数据
                //.setBounded(OffsetsInitializer.offsets(Map< TopicPartition,Long >))

                // 把source设置为无界流，此时用该source读取数据时，读到指定的位置，程序不会退出执行
                // 场景：从kafka读取一段长度的数据 和另外一条流进行双流join （类似 纬表 和事实表 join）
                //.setUnbounded(OffsetsInitializer.offsets(Map< TopicPartition,Long >))
                .build();

        //env.addSource(); // 接收的是SourceFunction 接口的实现类
        //env.fromSource(); // 接收的是Source 接口的实现类

        DataStreamSource<String> kfkDs = env.fromSource(kafkaSource, WatermarkStrategy.noWatermarks(), "kfk");
        kfkDs.print();

        env.execute();
    }
}
