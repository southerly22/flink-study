package StaticDemo;

/**
 * @author lzx
 * @date 2023/3/10 10:21
 * @description: TODO
 */
public class Static02 {
    public static void main(String[] args) {
        Demo test = null;
        test.hello(); //静态方法随着类的加载而加载
    }
}

class Demo {
    public static void hello() {
        System.out.println("hello");
    }
}
