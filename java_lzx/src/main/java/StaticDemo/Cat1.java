package StaticDemo;

/**
 * @author lzx
 * @date 2023/3/10 15:38
 * @description: TODO 就近原则
 */
public class Cat1 {
    public static void main(String[] args) {
        Cat2 cat2 = new Cat2();
        System.out.println(cat2.toString());

        Cat2 cat3 = new Cat2("a", "b", 2.3, 3);
        System.out.println(cat3.toString());
        System.out.println(cat2.age);
    }
}

class Cat2 {
    String name;
    String species;
    double weight;
    int age;

    public Cat2() {
    }

    //就近原则
    public Cat2(String name, String species, double weight, int age) {
        name = name;
        species = species;
        weight = weight;
        age = age;
    }
    static {
        int age = 10;
        age --;
    }

    @Override
    public String toString() {
        return "Cat2{" +
                "name='" + name + '\'' +
                ", species='" + species + '\'' +
                ", weight=" + weight +
                ", age=" + age +
                '}';
    }
}
