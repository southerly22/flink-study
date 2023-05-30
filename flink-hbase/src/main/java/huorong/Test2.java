package huorong;

import java.sql.Connection;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * @author lzx
 * @date 2023/05/29 15:38
 **/
public class Test2 {
    public static void main(String[] args) {

        Queue<Integer> pool = new ConcurrentLinkedQueue<>();
        pool.add(1);
        pool.add(2);
        pool.add(3);
        pool.add(4);

        //System.out.println("pool.poll() = " + pool.poll());

        while (pool.size() > 0) {
            System.out.println("pool.size() = " + pool.size());
            System.out.println("pool.poll() = " + pool.poll());
        }

    }
}
