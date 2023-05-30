/**
 * 方法的形参&&实参
 *
 * @author lzx
 * @date 2023/03/01 21:22
 **/
public class TestValue2 {
    public static void main(String[] args) {
        //基本类型
        int i =10;
        testMethod(i);
        System.out.println(i); //10

        // 引用类型
        People p = new People();
        p.age = 10;
        testMethod2(p);
        System.out.println(p.age); //9
    }

    public static void testMethod(int i){
        i--;
    }

    public static void testMethod2(People p){
        p.age--;
    }
}
class People{
    int age;
}
