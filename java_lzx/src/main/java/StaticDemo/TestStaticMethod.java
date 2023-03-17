package StaticDemo;

/**
 * @author lzx
 * @date 2023/3/10 10:18
 * @description: TODO
 */
public class TestStaticMethod {
    public static void main(String[] args) {
        Father.method();
        Father.fun();
        Son.method(); //继承静态方法

        Father f = new Son(); //静态方法的调用都只看编译时类型。
        f.method();
    }
}
