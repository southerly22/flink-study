import java.lang.reflect.Field;
import java.sql.*;
import java.sql.Date;
import java.util.*;

public class MyJdbcUtil {
//    public static void main(String[] args) throws Exception {
//        String user = "root";
//        String password = "123456";
//        String driver = "com.mysql.cj.jdbc.Driver";
//        String url = "jdbc:mysql://localhost:3306/lzxtest?useServerPrepStmts=true&rewriteBatchedStatements=true";
//
//
//        Class.forName(driver);
//        Connection connection = DriverManager.getConnection(url, user, password);
//
//
//        List<JiaHe> jiaHeList = createVo();
//        insertTable(jiaHeList, "lzx_test_0914", connection);
////        query(JiaHe.class, "",connection,"select * from t_customer where id = ?", 1);
//    }
//
//    // 测试数据
//    public static List<JiaHe> createVo() {
//        ArrayList<JiaHe> list = new ArrayList<>();
//        for (int i = 0; i < 100; i++) {
//            JiaHe jiaHe = new JiaHe(new Random().nextLong()+"","xing_tu" + i, "type" + i);
//            list.add(jiaHe);
//        }
//        return list;
//    }

    // 反射 insert into
    public static <T> void insertTable(List<T> objects, String tableName, Connection conn) {
        PreparedStatement ps = null;
        int batch = 50;

        int fieldsCnt = objects.get(1).getClass().getDeclaredFields().length;

        // 拼接sql语句
        StringBuffer buffer = new StringBuffer();
        buffer.append("insert into ").append(tableName).append(" values(");
        for (int i = 0; i < fieldsCnt; i++) {
            buffer.append("?").append(",");
        }
        buffer.replace(buffer.length()-1,buffer.length(),")");


        try {
            conn.setAutoCommit(false);
            ps = conn.prepareStatement(buffer.toString());

            for (int i = 0; i < objects.size(); i++) {

                Object obj = objects.get(i);
                Class<?> c = obj.getClass(); //反射成实际对象
                Field[] fields = c.getDeclaredFields(); //拿到类中的属性
                //for (Field field : fields) {//过滤不需要属性
                //    if (field.getName().lastIndexOf("id") != -1 || field.getName().lastIndexOf("version") != -1 || field.getName().lastIndexOf("pageIndex") != -1 || field.getName().lastIndexOf("pageSize") != -1) {
                //        fieldSize = fieldSize - 1;
                //    }
                //}
                for (int j = 0; j < fields.length; j++) {
                    Field field = fields[j];
                    field.setAccessible(true); //设置这些属性是可以访问的
                    String type = field.getType().toString(); //属性的类型
                    if (type.endsWith("String")) {
                        ps.setString(j + 1, String.valueOf(field.get(obj)));
                    } else if (type.endsWith("Date")) {
                        ps.setDate(j + 1, (Date) field.get(obj));
                    } else {

                    }
                }
                ps.addBatch();

                if (i % batch == 0) {
                    ps.executeBatch();
                    conn.commit();
                    ps.clearBatch();
                }
            }
            ps.executeBatch();
            conn.commit();
            ps.clearBatch();
            ps.close();
            conn.close();

        } catch (SQLException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }
    /*
    通用的查询多个Javabean对象的方法，例如：多个员工对象，多个部门对象等
    这里的clazz接收的是T类型的Class对象，
    如果查询员工信息，clazz代表Employee.class，
    如果查询部门信息，clazz代表Department.class，
     */
    protected static <T> ArrayList<T> query(Class<T> clazz, String sql, Connection connection, Object... args) throws Exception {
        // 创建PreparedStatement对象，对sql预编译
        //Connection connection = JDBCTools.getConnection();
        PreparedStatement ps = connection.prepareStatement(sql);
        //设置?的值
        if(args != null && args.length>0){
            for(int i=0; i<args.length; i++) {
                ps.setObject(i+1, args[i]);//?的编号从1开始，不是从0开始，数组的下标是从0开始
            }
        }

        ArrayList<T> list = new ArrayList<>();
        ResultSet res = ps.executeQuery();

        /*
        获取结果集的元数据对象。
        元数据对象中有该结果集一共有几列、列名称是什么等信息
         */
        ResultSetMetaData metaData = res.getMetaData();
        int columnCount = metaData.getColumnCount();//获取结果集列数

        //遍历结果集ResultSet，把查询结果中的一条一条记录，变成一个一个T 对象，放到list中。
        while(res.next()){
            //循环一次代表有一行，代表有一个T对象
            T t = clazz.newInstance();//要求这个类型必须有公共的无参构造

            //把这条记录的每一个单元格的值取出来，设置到t对象对应的属性中。
            for(int i=1; i<=columnCount; i++){
                //for循环一次，代表取某一行的1个单元格的值
                Object value = res.getObject(i);

                //这个值应该是t对象的某个属性值
                //获取该属性对应的Field对象
//                String columnName = metaData.getColumnName(i);//获取第i列的字段名
                String columnName = metaData.getColumnLabel(i);//获取第i列的字段名或字段的别名
                Field field = clazz.getDeclaredField(columnName);
                field.setAccessible(true);//这么做可以操作private的属性

                field.set(t, value);
            }

            list.add(t);
        }

        res.close();
        ps.close();
        return list;
    }
}
