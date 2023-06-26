package BlockingQueue;

import java.util.concurrent.TimeUnit;

/**
 * @author lzx
 * @date 2023/6/26 14:20
 * @description: TODO
 */
public class Test {
    public static void main(String[] args) {
        System.out.println(System.nanoTime());
        System.out.println(TimeUnit.MILLISECONDS.toNanos(System.currentTimeMillis()));
    }
}
