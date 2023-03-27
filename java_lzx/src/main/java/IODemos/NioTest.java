package IODemos;

import java.nio.IntBuffer;
import java.security.SecureRandom;
import java.util.Random;

/**
 * @author lzx
 * @date 2023/3/27 10:01
 * @description: TODO
 */
public class NioTest {
    // private static final int BUFFER_SIZE = 1024;

    public static void main(String[] args) {
        IntBuffer buffer = IntBuffer.allocate(10);
        System.out.println("capacity: "+ buffer.capacity());

        for (int i = 0; i < 5; i++) {
            int randomNum = new SecureRandom().nextInt(20);
            buffer.put(randomNum);
        }

        System.out.println("before flip limit: " + buffer.limit());
        System.out.println("before flip position: " + buffer.position());
        buffer.flip();
        System.out.println("after flip limit: "+buffer.limit());

        System.out.println("enter while loop");

        while (buffer.hasRemaining()) {
            System.out.println("position "+ buffer.position());
            System.out.println("limit: "+ buffer.limit());
            System.out.println("capacity:"+buffer.capacity());
            System.out.println("元素:" + buffer.get());
        }
    }
}
