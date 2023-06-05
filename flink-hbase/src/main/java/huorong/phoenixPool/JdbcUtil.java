package huorong.phoenixPool;

import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.druid.pool.DruidPooledConnection;
import com.alibaba.fastjson.JSONObject;
import com.google.common.base.CaseFormat;
import org.apache.commons.beanutils.BeanUtils;

import java.lang.reflect.InvocationTargetException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 当前工具类可以适用于任何JDBC方式访问的数据库中的任何查询语句
 * 单行单列：select count(*) from t;
 * 单行多列：select * from t where id='1001'; id为主键
 * 多行单列：select name from t;
 * 多行多列：select * from t;
 * Map<String,List<Object>>
 * List<JSON>
 * List<Map>
 */
public class JdbcUtil {
    // <T> 泛型方法
    public static <T> List<T> queryList(Connection connection, String sql, Class<T> clz, boolean underScoreToCamel) throws InstantiationException, IllegalAccessException, SQLException, InvocationTargetException {

        // 创建集合 用于存储结果数据
        ArrayList<T> arrayList = new ArrayList<>();

        PreparedStatement ps = connection.prepareStatement(sql);
        ResultSet rs = ps.executeQuery();

        // 查询元数据
        ResultSetMetaData metaData = rs.getMetaData();
        int columnCount = metaData.getColumnCount();

        // 遍历结果，将每行数据转换为 T 对象并加入集合  行遍历
        while (rs.next()) {
            // 创建 T 对象
            T t = clz.newInstance();

            // 列遍历 并给T 对象赋值
            for (int i = 0; i < columnCount; i++) {
                String columnName = metaData.getColumnName(i + 1);
                Object value = rs.getObject(columnName);

                //判断 是否要进行下划线和驼峰命名规则转换
                if (underScoreToCamel) {
                    columnName = CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, columnName.toLowerCase());
                }
                //赋值
                BeanUtils.setProperty(t, columnName, value);
            }

            //将对象 放入集合
            arrayList.add(t);
        }
        ps.close();
        rs.close();

        return arrayList;
    }

    public static void main(String[] args) throws SQLException {
        DruidDataSource dataSource = DruidPoolUtil.createConn();
        DruidPooledConnection conn = dataSource.getConnection();
        String sql = "SELECT * FROM OFFICIAL.SAMPLE_PAD_SCAN_LATEST WHERE \"rk\"  like '000000015acd38b8f06d639438ac442493d08180%'";
        try {
            List<JSONObject> jsonObjects = queryList(conn, sql, JSONObject.class, false);
            for (JSONObject jsonObject : jsonObjects) {
                System.out.println(jsonObject);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            conn.close();
        }
    }
}
