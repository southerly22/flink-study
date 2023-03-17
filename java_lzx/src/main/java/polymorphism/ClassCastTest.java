package polymorphism;

/**
 * @author lzx
 * @date 2023/3/9 13:55
 * @description: TODO
 */
public class ClassCastTest {
    public static void main(String[] args) {
        //没有类型转换
        Dog dog = new Dog(); //编译&& 运行的类型都是Dog

        //向上转型
        Pet pet = new Dog(); //编译时类型是pet 运行时类型是dog
        pet.setNickname("小白");
        pet.eat(); //可以调用父类声明的eat方法，但是执行的是子类重写的eat方法
        // pet.watchHouse(); 父类不能调用子类的扩展方法

        //强转！
        Dog dog1 = (Dog)pet;
        dog1.eat(); //调用的是子类的
        dog1.watchHouse(); //可以调用子类的扩展方法
        System.out.println("dog1.getNickname() = " + dog1.getNickname());

        Cat cat = (Cat) pet; //编译通过，pet是Pet类型，cat是pet的子类，向下转型可以
                            // 会报错，ClassCastException 因为(Dog)pet已经将pet转为dog类型 但是cat和dog无继承关系！

    }
}
