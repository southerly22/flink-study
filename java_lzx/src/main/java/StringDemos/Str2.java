package StringDemos;

/**
 * @author lzx
 * @date 2023/3/13 14:40
 * @description: TODO
 */
public class Str2 {
    public static void main(String[] args) {
        //final 修饰的是常量
        final String s1 = "hello";
        final String s2 = "world";
        String s3 = "helloworld";

        String s4 = s1 + "world"; //常量 + 常量 结果放在常量池中
        String s5 = s1 + s2; //常量 + 常量 结果放在常量池中
        String s6 = "hello" + "world";

        System.out.println(s3 == s4);
        System.out.println(s3 == s5);
        System.out.println(s3 == s6);
    }
}
