package ObjectTest;

import java.sql.PreparedStatement;

/**
 * @author lzx
 * @date 2023/3/9 18:10
 * @description: TODO
 */
public class TestFinalize {
    public static void main(String[] args) {
        Person person = new Person("Peter",12);
        System.out.println(person);
        person = null; //此时对象实体就是垃圾对象 等待被回收 但是时间不确定
        System.gc();    //垃圾回收
    }
}
class Person {
    private String name;
    private int age;

    public Person(String name, int age) {
        super();
        this.name = name;
        this.age = age;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    @Override
    protected void finalize() throws Throwable {
        System.out.println("对象被释放了===>" + this);
    }

    @Override
    public String toString() {
        return "Person{" +
                "name='" + name + '\'' +
                ", age=" + age +
                '}';
    }
}
