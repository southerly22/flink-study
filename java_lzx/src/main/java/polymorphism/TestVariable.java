package polymorphism;

import java.util.Arrays;

/**
 * @author lzx
 * @date 2023/3/9 13:45
 * @description: TODO
 */
public class TestVariable {
    public static void main(String[] args) {
        Base base = new Sub();
        base.add(1, 2, 3);

        Sub sub = (Sub) base;
        sub.add(1, 2,3);
    }
}

class Base {
    public void add(int a, int... arr) {
        System.out.println("base");
    }
}

class Sub extends Base {

    // public void add(int a, int[] arr) {
    //     System.out.println("sub");
    // }

    public void add(int a, int b, int c) {
        System.out.println("sub_2");
    }
}