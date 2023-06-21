package huorong.hr;

import com.zaxxer.hikari.HikariDataSource;
import huorong.phoenixPool.HikariPoolUtil;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.source.RichParallelSourceFunction;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;

/**
 * @author lzx
 * @date 2023/6/19 13:49
 * @description: TODO
 */
public class PhoenixSource extends RichParallelSourceFunction<Sample> {

    HikariDataSource dataSource;

    private final String selectSql;

    public PhoenixSource(String sql) {
        this.selectSql = sql;
    }

    @Override
    public void open(Configuration parameters) throws Exception {
        dataSource = HikariPoolUtil.createConn();
    }

    @Override
    public void run(SourceContext<Sample> ctx) throws Exception {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = dataSource.getConnection();
            ps = conn.prepareStatement(selectSql);
            rs = ps.executeQuery();
            ResultSetMetaData metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();
            while (rs.next()) {
                Sample sample = new Sample();
                for (int i = 1; i <= columnCount; i++) {
                    BeanUtils.setProperty(sample, metaData.getColumnName(i), rs.getString(metaData.getColumnName(i)));
                }
                ctx.collect(sample);
            }
        } finally {
            assert conn != null;
            conn.close();
            assert ps != null;
            ps.close();
            assert rs != null;
            rs.close();
        }
    }

    @Override
    public void cancel() {
    }

    @Override
    public void close() throws Exception {
        if (dataSource != null) {
            dataSource.close();
        }
    }
}
