package polymorphism;

import java.util.HashMap;
import java.util.List;

/**
 * @author lzx
 * @date 2023/3/9 14:19
 * @description: TODO
 */
public class TestInstanceof {
    public static void main(String[] args) {
        Pet[] pets = new Pet[2];
        pets[0] = new Dog();
        pets[0].setNickname("小白");
        pets[1] = new Cat();
        pets[1].setNickname("小花");

        for (int i = 0; i < pets.length; i++) {
            pets[i].eat();
            if (pets[i] instanceof Dog){
                Dog dog = (Dog) pets[i];
                System.out.println("dog.getNickname() = " + dog.getNickname());
                dog.watchHouse();
            }
            if (pets[i] instanceof Cat){
                Cat cat = (Cat) pets[i];
                System.out.println("cat.getNickname() = " + cat.getNickname());
                cat.catchMouse();
            }
        }
    }
}
