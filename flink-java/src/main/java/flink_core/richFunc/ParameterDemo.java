package flink_core.richFunc;

import org.apache.flink.api.common.functions.RichFilterFunction;
import org.apache.flink.api.java.ExecutionEnvironment;
import org.apache.flink.api.java.operators.DataSource;
import org.apache.flink.api.java.operators.FilterOperator;
import org.apache.flink.configuration.ConfigOptions;
import org.apache.flink.configuration.Configuration;

/**
 * @author lzx
 * @date 2023/06/11 16:15
 **/
public class ParameterDemo {
    public static void main(String[] args) throws Exception {
        ExecutionEnvironment env = ExecutionEnvironment.getExecutionEnvironment();

        DataSource<Integer> ds = env.fromElements(1, 2, 3, 4, 5);

        Configuration conf = new Configuration();
        conf.setInteger("limit", 3);

        FilterOperator<Integer> filter = ds.filter(new RichFilterFunction<Integer>() {
            private int limit;

            @Override
            public void open(Configuration parameters) throws Exception {
                //limit = parameters.getInteger("limit", 2); // 该方法过时
                parameters.getOptional(ConfigOptions.key("limit")
                                .intType()
                                .noDefaultValue())
                        .ifPresent(integer -> limit = integer);
                System.out.println("limit = " + limit);
            }

            @Override
            public boolean filter(Integer value) throws Exception {
                return value > limit;
            }
        }).withParameters(conf);

        filter.print();

    }
}
