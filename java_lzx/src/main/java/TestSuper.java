/**
 * 测试Super关键字
 *
 * @author lzx
 * @date 2023/03/06 08:53
 **/
public class TestSuper {
    public static void main(String[] args) {

        //默认调用父类的空参构造器
        Student s1 = new Student(1,2,"小李");
        System.out.println("-------------------------------------");
        Student s2 = new Student();
        System.out.println(s2.name);
    }
}
