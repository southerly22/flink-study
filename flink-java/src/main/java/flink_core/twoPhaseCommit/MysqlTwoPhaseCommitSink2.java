package flink_core.twoPhaseCommit;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import org.apache.flink.api.common.ExecutionConfig;
import org.apache.flink.api.common.typeutils.base.VoidSerializer;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.api.java.typeutils.runtime.kryo.KryoSerializer;
import org.apache.flink.streaming.api.functions.sink.TwoPhaseCommitSinkFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.beans.Transient;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * @author lzx
 * @date 2023/5/17 10:25
 * @description: TODO 自定义kafka to mysql,继承TwoPhaseCommitSinkFunction,实现两阶段提交
 *  泛型：
 *  ObjectNode : 输入类型
 *  Connection ： 事务 用于存储处理事务所需的所有信息
 *  Void       ： 上下文对象
 */
public class MysqlTwoPhaseCommitSink2 extends TwoPhaseCommitSinkFunction<Tuple2<String,Integer>, MysqlConnectState,Void> {
    private final static Logger log = LoggerFactory.getLogger(MysqlTwoPhaseCommitSink2.class);

    public MysqlTwoPhaseCommitSink2() {
        super(new KryoSerializer<>(MysqlConnectState.class,new ExecutionConfig()),VoidSerializer.INSTANCE);
    }

    // 开启一个事务
    @Override
    protected MysqlConnectState beginTransaction() throws Exception {
        log.warn("start beginTransaction----------");
        String url = "jdbc:mysql://localhost:3306/test?useUnicode=true&characterEncoding=UTF-8&zeroDateTimeBehavior=convertToNull&useSSL=false&autoReconnect=true";
        Connection connection = DriverManager.getConnection(url, "root", "123456");
        // 关闭 自动提交事务
        connection.setAutoCommit(false);
        return new MysqlConnectState(connection);
    }
    // 接收数据 负责写出
    @Override
    protected void invoke(MysqlConnectState transaction, Tuple2<String, Integer> value, Context context) throws Exception {
        log.warn("start invoke----------");
        Connection conn = transaction.connection;
        String sql = "insert into `t_eos` values (?,?) on duplicate key update id = ?";
        PreparedStatement statement = conn.prepareStatement(sql);
        statement.setInt(1,value.f1);
        statement.setString(2,value.f0);
        statement.setInt(3,value.f1);
        // 预提交
        statement.executeUpdate();
        statement.close();
        if (value.f1 == 15){
            throw new Exception("出错了！！！");
        }
    }

    // 预提交阶段
    @Override
    protected void preCommit(MysqlConnectState transaction) throws Exception {
        log.warn("start preCommit----------");
        // invoke 已做 （statement.executeUpdate();）
    }

    @SneakyThrows
    @Override
    protected void commit(MysqlConnectState transaction) {
        log.warn("start Commit----------");
        Connection conn = transaction.connection;
        try {
            conn.commit(); //正式提交阶段
        } catch (SQLException e) {
            log.info("提交失败");
            e.printStackTrace();
        }
        conn.close();
    }

    @SneakyThrows
    @Override
    protected void abort(MysqlConnectState transaction) {
        log.warn("start abort----------");
        Connection conn = transaction.connection;
        try {
            conn.rollback(); //正式提交阶段
        } catch (SQLException e) {
            log.info("提交失败");
            e.printStackTrace();
        }
        conn.close();
    }
}
@Data
@NoArgsConstructor
@AllArgsConstructor
class MysqlConnectState{
    public transient Connection connection;
}