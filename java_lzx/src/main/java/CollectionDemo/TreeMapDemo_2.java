package CollectionDemo;

import java.util.*;

/**
 * @author lzx
 * @date 2023/5/5 16:47
 * @description: TODO
 */
public class TreeMapDemo_2 {
    public static void main(String[] args) {

        TreeMap<EventBean, String> treeMap = new TreeMap<EventBean, String>(new Comparator<EventBean>() {
            @Override
            public int compare(EventBean o1, EventBean o2) {
                return o2.getActTimelong() - o1.getActTimelong(); //倒序排序
            }
        });

        treeMap.put(new EventBean(1,"e01",10000,"p01",10),"a");
        treeMap.put(new EventBean(1,"e02",11000,"p01",20),"a");
        treeMap.put(new EventBean(1,"e03",20000,"p01",40),"a");

        System.out.println(treeMap);
        for (EventBean bean : treeMap.keySet()) {
            System.out.println(bean);
        }
    }
}
