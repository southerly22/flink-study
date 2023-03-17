package StaticDemo;

/**
 * @author lzx
 * @date 2023/3/10 10:16
 * @description: TODO
 */
public class Father {
    public static void method() {
        System.out.println("father.method");
    }
    public static void fun() {
        System.out.println("father.fun");
    }

    public static void main(String[] args) {
        long l = Runtime.getRuntime().totalMemory()/1024/1024;
        long l1 = Runtime.getRuntime().maxMemory() /1024/1024/1024;
        System.out.println(l+"M,"+l1+"G");
    }
}
