package OrientedObject02;

/**
 * @author lzx
 * @date 2023/3/1 15:58
 * @description: TODO
 */


class Chinese {
    //实例变量
    String name;
    private int age;

    // 类变量
    static String nation = "kjijik";

    public Chinese() {

    }

    public Chinese(String name, int age) {
        this.name = name;
        this.age = age;
    }

    @Override
    public String toString() {
        return "Chinese{" +
                "name='" + name + '\'' +
                ", age=" + age +
                ", nation='" + nation + '\'' +
                '}';
    }
}

public class TestStatic {
    public static void main(String[] args) {
        Chinese c1 = new Chinese("小刘", 25);
        c1.nation = "中国";

        Chinese c2 = new Chinese("老干妈",66);

        System.out.println(c1);
        System.out.println(c2);

        System.out.println(Chinese.nation);
    }
}
