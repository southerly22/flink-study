import day01.GeneSql;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.table.api.bridge.java.StreamTableEnvironment;

/**
 * test
 *
 * @author lzx
 * @date 2023/04/19 22:05
 **/
public class FlinkSql_Test {
    public static void main(String[] args) throws Exception {

        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        StreamTableEnvironment tEnv = StreamTableEnvironment.create(env);

        System.out.println(GeneSql.flinkSql());

        //tEnv.executeSql(GeneSql.flinkSql());

        //env.execute("test");
    }
}
