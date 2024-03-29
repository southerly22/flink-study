package ObjectTest;

import java.util.Date;
import java.util.jar.JarEntry;

/**
 * @author lzx
 * @date 2023/3/9 17:40
 * @description: TODO
 */
public class TestEquals {
    public static void main(String[] args) {
        int it = 65;
        float fl = 65.0f;
        System.out.println("65和65.0f是否相等？" + (it == fl)); //

        char ch1 = 'A'; char ch2 = 12;
        System.out.println("65和'A'是否相等？" + (it == ch1));//
        System.out.println("12和ch2是否相等？" + (12 == ch2));//

        String str1 = new String("hello");
        String str2 = new String("hello");
        System.out.println("str1和str2是否相等？"+ (str1 == str2));//

        System.out.println("str1是否equals str2？"+(str1.equals(str2)));//

        TestEquals testEquals = new TestEquals();
        System.out.println(testEquals);
        System.out.println(testEquals.toString());

        System.out.println(testEquals.getClass());//运行时类型（因为有多态）
    }
}
