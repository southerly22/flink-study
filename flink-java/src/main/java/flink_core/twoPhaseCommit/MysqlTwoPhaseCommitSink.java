package flink_core.twoPhaseCommit;

import com.alibaba.fastjson.JSONObject;
import org.apache.flink.api.common.ExecutionConfig;
import org.apache.flink.api.common.typeutils.base.VoidSerializer;
import org.apache.flink.api.java.typeutils.runtime.kryo.KryoSerializer;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.flink.streaming.api.functions.sink.TwoPhaseCommitSinkFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author lzx
 * @date 2023/5/17 10:25
 * @description: TODO 自定义kafka to mysql,继承TwoPhaseCommitSinkFunction,实现两阶段提交
 *  泛型：
 *  ObjectNode : 输入类型
 *  Connection ： 事务 用于存储处理事务所需的所有信息
 *  Void       ： 上下文对象
 */
public class MysqlTwoPhaseCommitSink extends TwoPhaseCommitSinkFunction<ObjectNode, Connection,Void> {
    private final static Logger log = LoggerFactory.getLogger(MysqlTwoPhaseCommitSink.class);

    public MysqlTwoPhaseCommitSink() {

        super(new KryoSerializer<>(Connection.class,new ExecutionConfig()), // kryo自定义对象的序列化类型（事务对象的序列化类型）
                VoidSerializer.INSTANCE // 上下文对象的序列化类型
        );
    }

    // 预提交阶段，此阶段已在 invoke阶段做了
    @Override
    protected void preCommit(Connection transaction) throws Exception {
        log.info("start preCommit");
    }
    // 执行数据库入库 操作  task初始化时调用
    @Override
    protected void invoke(Connection connection, ObjectNode objectNode, Context context) throws Exception {
        log.info("start invoke");
        String date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        log.info("===>date:{} --{}",date,objectNode);
        String value = objectNode.get("value").toString();
        log.info("objectNode-value:" + value);
        JSONObject valueJson = JSONObject.parseObject(value);
        String value_str = (String) valueJson.get("value");
        String sql = "insert into `mysqlExactlyOnce_test` (`value`,`insert_time`) values (?,?)";
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setString(1,value_str);
        Timestamp value_time = new Timestamp(System.currentTimeMillis());
        ps.setTimestamp(2,value_time);
        log.info("要插入的数据:{}--{}",value_str,value_time);
        //执行insert语句
        ps.execute();
        //手动制造异常
        if(Integer.parseInt(value_str) == 15) {
            System.out.println(1 / 0);
        }
    }

    // 获取连接 手动开启事务
    @Override
    protected Connection beginTransaction() throws Exception {
        log.info("start beginTransaction");
        String url = "jdbc:mysql://localhost:3306/test?useUnicode=true&characterEncoding=UTF-8&zeroDateTimeBehavior=convertToNull&useSSL=false&autoReconnect=true";
        Connection connection = DBConnectionUtils.getConn(url, "root", "123456");
        return connection;
    }

    // 提交事务
    @Override
    protected void commit(Connection connection) {
        log.info(" start commit");
        DBConnectionUtils.commit(connection);
    }

    // 失败： 回滚 事务
    @Override
    protected void abort(Connection connection) {
        log.info("start abort");
        DBConnectionUtils.rollback(connection);
    }
}
