package flink_sql.udf;

import org.apache.flink.table.planner.expressions.In;

import javax.swing.plaf.PanelUI;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

/**
 * @author lzx
 * @date 2023/6/16 16:11
 * @description: TODO
 */
public class TP {
    public static void main(String[] args) {
        HashMap<Integer, Integer> hashMap = new HashMap<>();
        hashMap.put(200,2);
        hashMap.put(300,3);
        hashMap.put(500,1);

        System.out.println("getProcessTime(hashMap,50) = " + getProcessTime(hashMap, 90));

    }
    public static int getProcessTime(HashMap<Integer, Integer> hashMap,Integer tp1){
        TreeMap<Integer, Integer> treeMap = new TreeMap<>(hashMap);
        Integer sum = treeMap.values().stream().reduce(0, Integer::sum);
        int tp = tp1; // 50
        int responseTime = 0;
        int p = 0;
        int pos = (int) Math.ceil(sum * (tp / 100D));
        for (Map.Entry<Integer, Integer> entry : treeMap.entrySet()) {
            p += entry.getValue();
            if (p >= pos) {
                responseTime = entry.getKey();
                break;
            }
        }
        return responseTime;
    }
}
