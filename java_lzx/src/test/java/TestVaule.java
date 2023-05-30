/**
 * 方法值传递
 *
 * @author lzx
 * @date 2023/03/01 21:16
 **/
public class TestVaule {
    public static void main(String[] args) {
        //1.基本数据类型
        int i = 20;
        int j = i; //赋值的是数据值

        j--;
        System.out.println(i); //20

        //2.1 引用数据类型
        int[] arr1 = {1, 2, 3};
        int[] arr2 = arr1; //赋值的是地址
        arr2[0] = 10;
        System.out.println(arr1[0]); //10

        //2.2 引用数据类型
        Person p1 = new Person();
        Person p2 = p1;
        p1.age = 20;
        p2.age = 10;
        System.out.println(p1.age); //10
    }
}
class Person{
    int age;
}
