package StaticDemo;

/**
 * @author lzx
 * @date 2023/3/10 10:17
 * @description: TODO
 */
public class Son extends Father {
    // @Override 子类无法重写父类的静态方法
    public static void method() {
        System.out.println("father.method");
    }
}
