package polymorphism;

/**
 * @author lzx
 * @date 2023/3/9 11:16
 * @description: TODO
 */
public class Person {
    private Pet pet;

    //领养
    public void adopt(Pet pet){ //形参是父类类型，实参则是子类对象
        this.pet = pet;
    }
    //喂食
    public void feed(){
        pet.eat(); //pet 实际引用的对象类型不同，执行的eat方法也会不同
    }
}
