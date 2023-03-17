package StaticDemo;

/**
 * @author lzx
 * @date 2023/3/10 9:53
 * @description: TODO
 */
public class StaticTest {
    public static void main(String[] args) {
        Chinese c1 = new Chinese();
        c1.name="小李";
        c1.age=20;
        c1.nation="CHN";
        System.out.println(c1);

        Chinese c2 = new Chinese();
        c2.name="小刘";
        c2.age=21;
        System.out.println(c2);
    }
}
class Chinese{
    String name;
    int age;
    static String nation;

    @Override
    public String toString() {
        return "StaticDemo.Chinese{" +
                "name='" + name + '\'' +
                ", age=" + age +
                ", nation='" + nation + '\'' +
                '}';
    }
}
