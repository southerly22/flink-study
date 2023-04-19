/**
 * 人
 *
 * @author lzx
 * @date 2023/03/06 08:49
 **/
public class Person {
    String name="小左";
    private int age;


    public Person(){
        System.out.println("Person()。。。。");
    }

    public Person(String name,int age){
        this.name = name;
        this.age = age;
    }
}
