package flink_core.dataGen;

import org.apache.commons.math3.random.RandomDataGenerator;
import org.apache.flink.api.common.typeinfo.Types;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.configuration.RestOptions;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.source.datagen.DataGeneratorSource;
import org.apache.flink.streaming.api.functions.source.datagen.RandomGenerator;
import org.apache.flink.streaming.api.functions.source.datagen.SequenceGenerator;
import scala.tools.nsc.doc.model.Val;

/**
 * @author lzx
 * @date 2023/6/13 14:41
 * @description: TODO dataStreamApi的数据生成器
 */
public class DataStreamDataGeneratorDemo {
    public static void main(String[] args) throws Exception {
        Configuration configuration = new Configuration();
        configuration.set(RestOptions.ENABLE_FLAMEGRAPH, true); //开启火焰图
        StreamExecutionEnvironment env = StreamExecutionEnvironment.createLocalEnvironmentWithWebUI(configuration);
        env.setParallelism(1);
        env.disableOperatorChaining(); //禁用算子链

        // todo 1.orderInfo
        SingleOutputStreamOperator<OrderInfo> orderInfoDS = env.addSource(new DataGeneratorSource<>(new RandomGenerator<OrderInfo>() {
            @Override
            public OrderInfo next() {
                return new OrderInfo(
                        random.nextInt(1, 100000),
                        random.nextLong(1, 1000000),
                        random.nextUniform(1, 1000),
                        System.currentTimeMillis());
            }
        })).returns(Types.POJO(OrderInfo.class));
        
        // todo 2.userInfo
        SingleOutputStreamOperator<UserInfo> userInfoDS = env.addSource(new DataGeneratorSource<>(new SequenceGenerator<UserInfo>(1, 1000000) {
            RandomDataGenerator random = new RandomDataGenerator();

            @Override
            public UserInfo next() {
                return new UserInfo(
                        valuesToEmit.peek().intValue(),
                        valuesToEmit.poll().longValue(),
                        random.nextInt(1, 100),
                        random.nextInt(0, 1)
                );
            }
        })).returns(Types.POJO(UserInfo.class));

        orderInfoDS.print();
        userInfoDS.print();
        env.execute();
    }
}
