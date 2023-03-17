package StringDemos;

/**
 * @author lzx
 * @date 2023/3/13 14:28
 * @description: TODO
 */
public class Str1 {
    public static void main(String[] args) {
        String s1 = "hello";
        String s2 = "world";
        String s3 = "hello" + "world"; //常量 + 常量结果在常量池中，且常量池中不会存在相同内容的常量，编译期间就可以得到答案
        String s4 = s1 + "world"; //变量 + 常量结果在堆中
        String s5 = s1 + s2; //变量 + 变量 结果在堆中
        String s6 = (s1 + s2).intern(); //intern 把拼接的结果放到常量池里面
        String s7 = s1.concat("world"); //concat方法拼接，哪怕是两个常量对象拼接，结果也是在堆。


        System.out.println(s3 == s4);
        System.out.println(s3 == s5);
        System.out.println(s4 == s5);
        System.out.println(s3 == s6);
        System.out.println(s4 == s7);
        // **结论：**
        //
        // （1）常量+常量：结果是常量池。且常量池中不会存在相同内容的常量。
        //
        // （2）常量与变量 或 变量与变量：结果在堆中
        //
        // （3）拼接后调用intern方法：返回值在常量池中
    }
}
