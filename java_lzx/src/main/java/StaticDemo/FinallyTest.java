package StaticDemo;

/**
 * @author lzx
 * @date 2023/3/10 16:19
 * @description: TODO
 */
public class FinallyTest {
    public static int test1(int a){
        try {
            a += 20;
            return a;
        }finally {
            a +=30;
            return a;
        }
    }
    public static int test2(int b){
        try {
            b+=20;
            return b;
        }finally {
            b+=30;
            System.out.println(b);//2. 输出60
        }
    }
    public static void main(String[] args) {
        int num =10;
        System.out.println(test1(10)); //1. 输出60
        System.out.println(test2(10)); //3. 输出30
    }
}
