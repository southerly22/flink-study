package polymorphism;

/**
 * @author lzx
 * @date 2023/3/9 15:39
 * @description: TODO
 */
public class ConversionTest {
    public static void main(String[] args) {
        double d = 13.4;
        long l = (long)d;
        System.out.println(l);

        int i =5;
        Object obj = "Hello";
        String objStr = (String) obj;
        System.out.println(objStr);

         Object objI = new Integer(5);
        String s = String.valueOf(objI);
        System.out.println(s);
    }
}
