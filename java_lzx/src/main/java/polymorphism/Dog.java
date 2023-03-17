package polymorphism;

/**
 * @author lzx
 * @date 2023/3/9 11:14
 * @description: TODO
 */
public class Dog extends Pet {
    @Override
    public void eat(){
        System.out.println("dog"+super.getNickname()+"啃骨头");
    }
    public void watchHouse(){
        System.out.println("狗子看家...");
    }
}
