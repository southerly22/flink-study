package flink_core.window;

import org.apache.flink.api.java.tuple.Tuple2;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * @author lzx
 * @date 2023/5/5 17:50
 * @description: TODO
 */
public class Test02 {
    public static void main(String[] args) {

        HashMap<String, Tuple2<Integer, Long>> hashMap = new HashMap<>();
        List<EventBean> list = new ArrayList<>();
        list.add(new EventBean(1,"e01",10000,"p01",10));
        list.add(new EventBean(1,"e02",11000,"p01",20));
        list.add(new EventBean(1,"e02",20000,"p01",40));

        for (EventBean bean : list) {

            Tuple2<Integer, Long> countAndTime = hashMap.getOrDefault(bean.getEventId(), Tuple2.of(0, 0L));
            hashMap.put(bean.getEventId(), Tuple2.of(countAndTime.f0 + 1, countAndTime.f1 + bean.getActTimelong()));
        }
        System.out.println(hashMap);
    }
}
