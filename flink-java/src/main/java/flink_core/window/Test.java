package flink_core.window;

import java.util.Comparator;
import java.util.HashMap;
import java.util.TreeSet;

/**
 * @author lzx
 * @date 2023/5/5 13:21
 * @description: TODO
 */
public class Test {
    public static void main(String[] args) {
        TreeSet<EventBean> set = new TreeSet<>(new Comparator<EventBean>() {
            @Override
            public int compare(EventBean o1, EventBean o2) {
                // return o1.getActTimelong() - o2.getActTimelong(); // 小根堆
                return o2.getActTimelong() - o1.getActTimelong(); // 大根堆
            }
        });

        set.add(new EventBean(1,"e01",10000,"p01",10));
        set.add(new EventBean(1,"e02",11000,"p01",20));
        set.add(new EventBean(1,"e03",20000,"p01",40));
        for (EventBean eventBean : set) {
            System.out.println(eventBean);
        }
        if (set.size() > 2) set.remove(set.last());

        System.out.println("--------------------------------");
        System.out.println(set.first());
        System.out.println(set.last());

        for (EventBean eventBean : set) {
            System.out.println(eventBean);
        }

        HashMap<String, Integer> hashMap = new HashMap<>();

        hashMap.put("xiaoli",1);
        hashMap.put("xiaoliu",2);
        hashMap.put("xiaoli",3);
        System.out.println(hashMap.toString());
    }
}
