import com.ververica.cdc.debezium.DebeziumDeserializationSchema;
import com.alibaba.fastjson.JSONObject;
import io.debezium.data.Envelope;
import org.apache.flink.api.common.typeinfo.BasicTypeInfo;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.util.Collector;
import org.apache.kafka.connect.data.Field;
import org.apache.kafka.connect.data.Schema;
import org.apache.kafka.connect.data.Struct;
import org.apache.kafka.connect.source.SourceRecord;

import java.util.List;

/**
 * @author lzx
 * @date 2023/10/23 21:28
 **/
public class MyDebeziumDeserializationSchema implements DebeziumDeserializationSchema<String> {

    @Override
    public void deserialize(SourceRecord sourceRecord, Collector<String> out) throws Exception {
        JSONObject res = new JSONObject();

        // 获取数据库和表名称
        String topic = sourceRecord.topic();
        String[] fields = topic.split("\\.");
        String database = fields[1];
        String tableName = fields[2];

        Struct value = (Struct)sourceRecord.value();

        //变更schema信息
        if (value.toString().contains("historyRecord")){
            System.out.println("输出historyRecord:" + value.get("historyRecord"));
            throw new RuntimeException("schema信息发生变更，程序阻断！");
            //System.exit(1);
        }

        // 获取before数据
        Struct before = value.getStruct("before");
        JSONObject beforeJson = new JSONObject();
        if(before != null){
            Schema beforeSchema = before.schema();
            List<Field> beforeFields = beforeSchema.fields();
            for (Field field : beforeFields) {
                Object beforeValue = before.get(field);
                beforeJson.put(field.name(),beforeValue);
            }
        }


        // 获取after数据
        Struct after = value.getStruct("after");
        JSONObject afterJson = new JSONObject();
        if(after != null){
            Schema afterSchema = after.schema();
            List<Field> afterFields = afterSchema.fields();
            for (Field field : afterFields) {
                Object afterValue = after.get(field);
                afterJson.put(field.name(),afterValue);
            }
        }

        //获取操作类型 READ DELETE UPDATE CREATE
        Envelope.Operation operation = Envelope.operationFor(sourceRecord);
        String type = operation.toString().toLowerCase();
        System.out.println("type--->"+type);
        if("create".equals(type)){
            type = "insert";
        }




        // 将字段写到json对象中
        res.put("database",database);
        res.put("tableName",tableName);
        res.put("before",beforeJson);
        res.put("after",afterJson);
        res.put("type",type);

        //输出数据
        out.collect(res.toString());
    }

    @Override
    public TypeInformation<String> getProducedType() {
        return BasicTypeInfo.STRING_TYPE_INFO;
    }
}
