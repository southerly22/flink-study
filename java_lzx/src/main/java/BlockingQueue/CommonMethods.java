package BlockingQueue;

import java.util.concurrent.ArrayBlockingQueue;

/**
 * @author lzx
 * @date 2023/6/25 11:50
 * @description: TODO
 */
public class CommonMethods {
    public static void main(String[] args) {
        ArrayBlockingQueue<String> blockingQueue = new ArrayBlockingQueue<>(3);

        // 第一组
        blockingQueue.add("张1");
        blockingQueue.add("张2");
        blockingQueue.add("张3");
        blockingQueue.add("张4");

    }
}
