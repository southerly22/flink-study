package flink_sql.test;

import org.apache.flink.streaming.util.serialization.JSONKeyValueDeserializationSchema;

/**
 * 自定义json反序列化器
 *
 * @author lzx
 * @date 2023/06/29 23:19
 **/
public class CustomJsonDeserializer extends JSONKeyValueDeserializationSchema {
    public CustomJsonDeserializer(boolean includeMetadata) {
        super(includeMetadata);
    }


}
