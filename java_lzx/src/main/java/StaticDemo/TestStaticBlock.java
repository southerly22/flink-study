package StaticDemo;

/**
 * @author lzx
 * @date 2023/3/10 13:08
 * @description: TODO
 */
public class TestStaticBlock{
    public static void main(String[] args) {
        Chinese1 zs = new Chinese1("zs");
        Chinese1 ls = new Chinese1("ls");
    }
}

class Chinese1 {
    private static String country;
    private String name;
    {
        System.out.println("非静态代码块：country " + country);
    }

    static {
        country = "China";
        System.out.println("静态代码块");
    }

    public Chinese1(String name) {
        this.name = name;
    }
}