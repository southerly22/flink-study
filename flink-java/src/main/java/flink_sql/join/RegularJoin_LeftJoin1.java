package flink_sql.join;

import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.table.api.bridge.java.StreamTableEnvironment;

import java.time.ZoneId;

/**
 * 内连接
 *
 * @author lzx
 * @date 2023/04/18 17:03
 **/
public class RegularJoin_LeftJoin1 {
    public static void main(String[] args) throws Exception {

        Configuration conf = new Configuration();
        conf.setInteger("rest.port",8085);

        // 可以基于现有的 StreamExecutionEnvironment 创建 StreamTableEnvironment 来与 DataStream API 进行相互转换
        StreamExecutionEnvironment env = StreamExecutionEnvironment.createLocalEnvironmentWithWebUI(conf);
        StreamTableEnvironment tEnv = StreamTableEnvironment.create(env);

        // 指定国内时区
        tEnv.getConfig().setLocalTimeZone(ZoneId.of("Asia/Shanghai"));

        // 订单表
        String userOrderTableSql="CREATE TABLE user_order( \n" +
                "    order_id BIGINT,\n" +
                "    ts BIGINT,\n" +
                "    d_ts AS TO_TIMESTAMP_LTZ(ts,3)\n" +
                "    ) WITH( \n" +
                "        'connector' = 'kafka',\n" +
                "        'topic'='user_order',\n" +
                "        'properties.bootstrap.servers'='localhost:9094,localhost:9092,localhost:9093',\n" +
                "        'properties.group.id' = 'gid-sql-order',\n" +
                "        'scan.startup.mode' = 'latest-offset',\n" +
                "        'format' = 'json',\n" +
                "        'json.fail-on-missing-field' = 'false',\n" +
                "        'json.ignore-parse-errors' = 'true'\n" +
                "    )";
        tEnv.executeSql(userOrderTableSql);

        // 支付表
        String paymentFlowTableSql = "CREATE TABLE payment_flow( \n" +
                "    order_id BIGINT,\n" +
                "    pay_money BIGINT\n" +
                "    ) WITH( \n" +
                "        'connector' = 'kafka',\n" +
                "        'topic'='payment_flow',\n" +
                "        'properties.bootstrap.servers'='localhost:9094,localhost:9092,localhost:9093',\n" +
                "        'properties.group.id' = 'gid-sql-pay',\n" +
                "        'scan.startup.mode' = 'latest-offset',\n" +
                "        'format' = 'json',\n" +
                "        'json.fail-on-missing-field' = 'false',\n" +
                "        'json.ignore-parse-errors' = 'true'\n" +
                "    )";
        tEnv.executeSql(paymentFlowTableSql);

        // 汇总宽表
        String resTableSql = "CREATE TABLE order_payment( \n" +
                "    order_id BIGINT,\n" +
                "    pay_money BIGINT,\n" +
                "    d_ts TIMESTAMP_LTZ(3),\n" +
                "    PRIMARY KEY(order_id) NOT ENFORCED\n" +
                "    ) WITH( \n" +
                "        'connector' = 'upsert-kafka',\n" + //left join有回撤流，只可以使用upsert-kafka，同时还要指定key的format
                "        'topic'='order_payment',\n" +
                "        'properties.bootstrap.servers'='localhost:9094,localhost:9092,localhost:9093',\n" +
                "        'key.format' = 'json',\n" +
                "        'value.format' = 'json'\n" +
                "    )";

        tEnv.executeSql(resTableSql);

        // 关联订单表和支付表
        String joinSql="insert into order_payment\n" +
                "select\n" +
                "  uo.order_id,\n" +
                "  pf.pay_money,\n" +
                "  uo.d_ts\n" +
                "from user_order as uo\n" +
                "left join payment_flow pf\n" +
                "on uo.order_id = pf.order_id";

        tEnv.executeSql(joinSql);

    }
}
