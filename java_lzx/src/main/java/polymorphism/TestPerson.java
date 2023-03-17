package polymorphism;

/**
 * @author lzx
 * @date 2023/3/9 11:18
 * @description: TODO
 */
public class TestPerson {
    public static void main(String[] args) {
        Person person = new Person();
        Dog dog = new Dog();
        dog.setNickname("小白");
        person.adopt(dog); //实参是子类dog 形参是父类Pet
        person.feed(); //传入的是dog对象 调用的是dog的eat方法
    }
}
