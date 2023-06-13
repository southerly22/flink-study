package flink_core.richFunc;

import org.apache.flink.api.common.ExecutionConfig;
import org.apache.flink.api.common.functions.RichFilterFunction;
import org.apache.flink.api.java.ExecutionEnvironment;
import org.apache.flink.api.java.operators.DataSource;
import org.apache.flink.api.java.operators.FilterOperator;
import org.apache.flink.configuration.ConfigOptions;
import org.apache.flink.configuration.Configuration;

/**
 * @author lzx
 * @date 2023/06/11 18:17
 **/
public class ParameterDemo2 {
    public static void main(String[] args) throws Exception {
        ExecutionEnvironment env = ExecutionEnvironment.getExecutionEnvironment();

        DataSource<Integer> ds = env.fromElements(1, 2, 3, 4, 5);

        Configuration conf = new Configuration();
        conf.setInteger("limit",3);
        env.getConfig().setGlobalJobParameters(conf);

        FilterOperator<Integer> filter = ds.filter(new RichFilterFunction<Integer>() {
            private int limit;

            @Override
            public void open(Configuration parameters) throws Exception {
                ExecutionConfig.GlobalJobParameters globalJobParameters = getRuntimeContext().getExecutionConfig().getGlobalJobParameters();
                Configuration globalConf = (Configuration) globalJobParameters;
                globalConf.getOptional(ConfigOptions.key("limit").intType().noDefaultValue()).ifPresent(integer -> limit = integer);
            }

            @Override
            public boolean filter(Integer value) throws Exception {
                return value > limit;
            }
        });

        filter.print();
    }
}
