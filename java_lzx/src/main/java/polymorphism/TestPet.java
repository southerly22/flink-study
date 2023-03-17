package polymorphism;

/**
 * @author lzx
 * @date 2023/3/9 11:27
 * @description: TODO
 */
public class TestPet {
    public static void main(String[] args) {
        //多态引用
        // Pet pet = new Dog();
        // pet.setNickname("小白");
        // pet.eat();

        /***
         * @Description: 多态的表现形式。编译时看左边（父类） 只能调用父类声明的方法，不能调用子类的扩展方法   pet.catchMouse()
         *                            运行时看右边（子类） 如果子类重写了方法 一定是执行子类重写的方法  pet.eat()
         **/

        Pet pet = new Cat();
        pet.setNickname("小花");
        pet.eat();
    }
}
