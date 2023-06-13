package flink_core.richFunc;

import org.apache.flink.api.common.functions.RichFilterFunction;
import org.apache.flink.api.java.DataSet;
import org.apache.flink.api.java.ExecutionEnvironment;
import org.apache.flink.api.java.operators.FilterOperator;
import org.apache.flink.api.java.utils.ParameterTool;
import org.apache.flink.configuration.Configuration;

/**
 * 富函数 传参
 *
 * @author lzx
 * @date 2023/06/11 15:21
 **/
public class Rich_Filter {
    public static void main(String[] args) throws Exception {
        //获取运行环境
        ExecutionEnvironment env = ExecutionEnvironment.getExecutionEnvironment();

        //读取数据
        DataSet<Integer> data = env.fromElements(1, 2, 3, 4, 5);

        Configuration conf = new Configuration();
        conf.setInteger("limit", 3);

        FilterOperator<Integer> filter = data.filter(new RichFilterFunction<Integer>() {
            private int limit;

            @Override
            public void open(Configuration parameters) throws Exception {
                limit = parameters.getInteger("limit", 0);
            }

            @Override
            public boolean filter(Integer value) throws Exception {
                return value > limit;
            }
        }).withParameters(conf);
        filter.print();

    }
}
