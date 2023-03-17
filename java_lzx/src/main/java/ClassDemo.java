import java.lang.reflect.Field;

/**
 * @author lzx
 * @date 2023/3/14 18:04
 * @description: TODO
 */
public class ClassDemo {
    public static void main(String[] args) {
        try {
            ClassDemo c = new ClassDemo();
            Class cls = c.getClass();
            // field long l
            Field lVal = cls.getDeclaredField("l");
            System.out.println("Field = " + lVal.toString());
        } catch (Exception e) {
            System.out.println(e.toString());
        }
    }

    public ClassDemo() {
        // no argument constructor
    }

    public ClassDemo(long l) {
        this.l = l;
    }



    long l = 77688;



}
