package StaticDemo;

/**
 * @author lzx
 * @date 2023/3/10 13:24
 * @description: TODO
 */
public class Father2 {
    static {
        System.out.println("111111111111");
    }
    {
        System.out.println("2222222222");
    }

    public Father2() {
        System.out.println("333333333333333");
    }

    public static void main(String[] args) {
        Father2 father2 = new Father2();
    }
}
