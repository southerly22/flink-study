package dorisDemo;

import org.apache.doris.flink.cfg.DorisExecutionOptions;
import org.apache.doris.flink.cfg.DorisOptions;
import org.apache.doris.flink.cfg.DorisReadOptions;
import org.apache.doris.flink.sink.DorisSink;
import org.apache.doris.flink.sink.writer.RowDataSerializer;
import org.apache.flink.api.common.RuntimeExecutionMode;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.streaming.api.CheckpointingMode;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.table.api.DataTypes;
import org.apache.flink.table.data.GenericRowData;
import org.apache.flink.table.data.RowData;
import org.apache.flink.table.data.StringData;
import org.apache.flink.table.types.DataType;

import java.time.LocalDate;
import java.util.Properties;

/**
 * @author lzx
 * @date 2023/6/21 18:24
 * @description: TODO
 */
public class SinkDoris_RowData {
    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.enableCheckpointing(3000, CheckpointingMode.EXACTLY_ONCE);
        env.getCheckpointConfig().setCheckpointStorage("");
        env.setRuntimeMode(RuntimeExecutionMode.BATCH);

        //doris sink option
        DorisSink.Builder<RowData> builder = DorisSink.builder();
        DorisOptions dorisOptions = DorisOptions.builder()
                .setFenodes("")
                .setTableIdentifier("")
                .setUsername("root")
                .setPassword("000000").build();

        // json格式流式加载
        Properties prop = new Properties();
        prop.setProperty("format", "json");
        prop.setProperty("read_json_by_line", "true");
        DorisExecutionOptions executionOptions = DorisExecutionOptions.builder()
                .setLabelPrefix("label-doris")
                .setStreamLoadProp(prop).build();


        // flink rowdata
        String[] fields = {"city", "longitude", "latitude", "destroy_date"};
        DataType[] types = {DataTypes.VARCHAR(256), DataTypes.DOUBLE(), DataTypes.DOUBLE(), DataTypes.DATE()};

        builder.setDorisOptions(dorisOptions)
                .setDorisExecutionOptions(executionOptions)
                .setDorisReadOptions(DorisReadOptions.builder().build())
                .setSerializer(
                        RowDataSerializer.builder()
                                .setFieldNames(fields)
                                .setFieldType(types)
                                .setType("json").build()
                );

        // 模拟数据
        SingleOutputStreamOperator<RowData> source = env.fromElements("")
                .map(new MapFunction<String, RowData>() {
                    @Override
                    public RowData map(String value) throws Exception {
                        GenericRowData genericRowData = new GenericRowData(4);
                        genericRowData.setField(0, StringData.fromString("beijing"));
                        genericRowData.setField(1, 116.4040);
                        genericRowData.setField(2, 39.999);
                        genericRowData.setField(3, LocalDate.now().toEpochDay());
                        return genericRowData;
                    }
                });

        // 写入
        source.sinkTo(builder.build());

        env.execute();
    }
}
