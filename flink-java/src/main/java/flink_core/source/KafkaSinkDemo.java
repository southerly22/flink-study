package flink_core.source;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.connector.base.DeliveryGuarantee;
import org.apache.flink.connector.kafka.sink.KafkaRecordSerializationSchema;
import org.apache.flink.connector.kafka.sink.KafkaSink;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

import java.util.ArrayList;

/**
 * @author lzx
 * @date 2023/09/12 22:38
 **/
public class KafkaSinkDemo {
    public static void main(String[] args) throws Exception {
        Configuration configuration = new Configuration();
        configuration.setInteger("rest.port", 8086);
        StreamExecutionEnvironment env = StreamExecutionEnvironment.createLocalEnvironmentWithWebUI(configuration);
        env.setParallelism(6);
        KafkaSink<String> kafkaSink = KafkaSink.<String>builder()
                .setBootstrapServers("192.168.6.83:9094,192.168.6.83:9092,192.168.6.83:9093")
                .setRecordSerializer(
                        KafkaRecordSerializationSchema.builder()
                                .setTopic("lzx_0912")
                                .setValueSerializationSchema(new SimpleStringSchema())
                                .build()
                )
                .setDeliverGuarantee(DeliveryGuarantee.EXACTLY_ONCE)
                .build();


        ArrayList<String> arrayList = new ArrayList<>();
        String json = "{\"name\":\"Bob Brown\",\"address\":\"456 Oak Lane\",\"city\":\"Pretendville\"}";

        JSONObject jsonObject = JSON.parseObject(json);
        for (int i = 0; i < 100; i++) {
            jsonObject.put("age",i);
            System.out.println(jsonObject.toJSONString());
            arrayList.add(jsonObject.toJSONString());
        }


        DataStreamSource<String> source = env.fromCollection(arrayList);

        source.sinkTo(kafkaSink).name("sinkkafka");

        env.execute();
    }
}
