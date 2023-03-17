package StaticDemo;

/**
 * @author lzx
 * @date 2023/3/10 15:16
 * @description: TODO
 */
public class Test02 {
    static int x,y,z;
    static {
       int x = 5;
        x--; //就近原则 操作的是局部变量x 而不是Test02的x
    }
    static {
        x --;
    }

    public static void method() {
        y=z++;
        ++z;
    }
    public static void main(String[] args) {

        System.out.println(x); // -1
        z--;
        method();
        System.out.println(y); // -1
        System.out.println(z); // 1
        System.out.println("result:"+( z + y + x )); // -1
    }
}
