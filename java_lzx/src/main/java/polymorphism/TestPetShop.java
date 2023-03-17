package polymorphism;

/**
 * @author lzx
 * @date 2023/3/9 13:30
 * @description: TODO
 */
public class TestPetShop {
    public static void main(String[] args) {
        PetShop petShop = new PetShop();
        Pet dog = petShop.sale("Dog");
        dog.setNickname("小白");
        dog.eat();

        Pet cat = petShop.sale("Cat");
        cat.setNickname("小花");
        cat.eat();
    }
}
