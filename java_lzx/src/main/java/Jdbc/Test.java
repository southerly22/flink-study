package Jdbc;

import java.lang.reflect.Field;
import java.sql.Date;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Stream;

/**
 * @author lzx
 * @date 2023/09/16 19:27
 **/
public class Test {
    public static void main(String[] args) throws IllegalAccessException {
        //Object jiaHe = new JiaHe();
        //Class<?> jiaHeClass = jiaHe.getClass();
        //Field[] declaredFields = jiaHeClass.getDeclaredFields();
        //
        //for (Field declaredField : declaredFields) {
        //    System.out.println(declaredField.getGenericType().toString());
        //    Date date = (Date) declaredField.get(jiaHe);
        //    date.getTime();
        //}
        //
        ////System.out.println(new Date(new Random().nextInt()));

        ArrayList<JiaHe> list = new ArrayList<>();
        list.add(null);
        list.add(new JiaHe("2","x",new Date(100)));

        //StringBuffer buffer = new StringBuffer();
        //
        //buffer.append("insert into ").append("tableName").append(" values(");
        //for (int i=0;i<3;i++) {
        //    buffer.append("?").append(",");
        //}
        //buffer.replace(buffer.length()-1,buffer.length(),")");
        //
        //System.out.println(buffer.toString());

        //System.out.println(JiaHe.class.getDeclaredFields().length);
    }
}
