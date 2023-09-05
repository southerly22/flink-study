package flink_core.topN;

import org.apache.flink.table.planner.expressions.In;

import java.util.Comparator;
import java.util.PriorityQueue;

/**
 * @author lzx
 * @date 2023/08/27 15:41
 **/
public class HeadLzx {
    public static void main(String[] args) {
        // 小顶堆
        PriorityQueue<Integer> queue = new PriorityQueue<>(new Comparator<Integer>() {
            @Override
            public int compare(Integer o1, Integer o2) {
                return o2 - o1;
            }
        });

        int top=3;

        for (int i = 0; i < 7; i++) {
            if (queue.size() < 3){
                queue.add(i);
            }else {
                int peek = queue.peek();
                if (i>peek){
                    queue.poll();
                    queue.add(i);
                }
            }
        }
        queue.forEach(System.out::println);
    }
}
