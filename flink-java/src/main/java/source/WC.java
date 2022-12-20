package source;

import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.api.common.functions.ReduceFunction;
import org.apache.flink.api.java.ExecutionEnvironment;
import org.apache.flink.api.java.operators.DataSource;
import org.apache.flink.api.java.operators.FlatMapOperator;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.util.Collector;

public class WC {
    public static void main(String[] args) throws Exception {
        // 1.创建执行环境
        ExecutionEnvironment env = ExecutionEnvironment.getExecutionEnvironment();

        // 2.读取文件
        DataSource<String> lineDs = env.readTextFile("input/word.txt");

        FlatMapOperator<String, Tuple2<String, Long>> wordAndOne = lineDs.flatMap(new FlatMapFunction<String, Tuple2<String, Long>>() {
            @Override
            public void flatMap(String line, Collector<Tuple2<String, Long>> collector) throws Exception {
                String[] words = line.split(" ");
                for (String word : words) {
                    collector.collect(Tuple2.of(word, 1L));
                }
            }
        });

        //wordAndOne.groupBy(0)
        //        .sum(1)
        //        .print();

        wordAndOne
                .groupBy(f -> f.f0)
                .reduce(new ReduceFunction<Tuple2<String, Long>>() {
                    @Override
                    public Tuple2<String, Long> reduce(Tuple2<String, Long> t1, Tuple2<String, Long> t2) throws Exception {
                        if (t1 == null || t2.f1 >= t1.f1) {
                            return t2;
                        } else return t1;
                    }
                })
                .print();

    }
}
