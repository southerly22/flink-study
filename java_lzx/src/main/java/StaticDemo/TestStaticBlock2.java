package StaticDemo;

/**
 * @author lzx
 * @date 2023/3/10 13:40
 * @description: TODO
 */
public class TestStaticBlock2 {
    public static void main(String[] args) {
        new Leaf();
    }
}
class Root{
    static {
        System.out.println("root的静态");
    }
    {
        System.out.println("root的普通块");
    }

    public Root() {
        System.out.println("root的构造器");
    }
}
class Mid extends Root{
    static {
        System.out.println("mid的静态");
    }
    {
        System.out.println("mid的普通");
    }

    public Mid() {
        System.out.println("Mid的无参构造器");
    }
    public Mid(String msg){
        this(); //mid 的无参构造器
        System.out.println("Mid的带参 构造器"+ msg);
    }
}
class Leaf extends Mid{
    static {
        System.out.println("Leaf的静态");
    }
    {
        System.out.println("Leaf的普通");
    }

    public Leaf() {
        super("mid"); //调用父类的 带一个参数的构造器
        System.out.println("leaf的无参构造器");
    }
}
