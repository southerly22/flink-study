package polymorphism;

/**
 * @author lzx
 * @date 2023/3/9 13:28
 * @description: TODO
 */
public class PetShop {
    public Pet sale(String type){
        switch (type){
            case "Dog":
                return new Dog();
            case "Cat":
                return new Cat();
        }
        return null;
    }
}
