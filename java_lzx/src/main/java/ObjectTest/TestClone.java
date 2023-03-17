package ObjectTest;

/**
 * @author lzx
 * @date 2023/3/9 17:53
 * @description: TODO
 */
public class TestClone {
    public static void main(String[] args) {
        Animal a1 = new Animal("花花");
        try {
            Animal a2 = (Animal)a1.clone();
            System.out.println("原始对象 "+a1);
            a2.setName("毛毛");
            System.out.println("clone后的对象 " + a2);
        }catch (CloneNotSupportedException e){
            e.printStackTrace();
        }
    }
}
class Animal implements Cloneable{
    private String name;

    public Animal() {
        super();
    }

    public Animal(String name) {
        super();
        this.name = name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return "Animal{" +
                "name='" + name + '\'' +
                '}';
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
}
