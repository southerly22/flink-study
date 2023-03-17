package polymorphism;

/**
 * @author lzx
 * @date 2023/3/9 11:16
 * @description: TODO
 */
public class Cat extends Pet {
    @Override
    public void eat() { //子类重写父类的方法
        System.out.println("Cat"+getNickname()+"吃鱼");
    }

    //子类扩展方法
    public void catchMouse(){
        System.out.println("cat catch Mouse...");
    }
}
