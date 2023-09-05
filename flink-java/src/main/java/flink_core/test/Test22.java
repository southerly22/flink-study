package flink_core.test;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * @author lzx
 * @date 2023/08/28 17:10
 **/
public class Test22 {
    public static void main(String[] args) {
        Map<String, Object> input = new HashMap<>();
        input.put("a", 1);
        {
            Map<String, Object> b = new HashMap<>();
            input.put("b", b);
            b.put("x", 1);
            {
                Map<String, Object> c = new HashMap<>();
                b.put("c", c);
                c.put("y", 1);
            }
            {
                Map<String, Object> d = new HashMap<>();
                b.put("d", d);

                {
                    Map<String, Object> e = new HashMap<>();
                    d.put("e", e);
                    e.put("o", 1);
                }
                d.put("f", 1);
            }
        }
        Map<String, Integer> map = printMap(input,"");
        map.forEach((k,v)->{
            System.out.println(k+","+v);
        });
    }

    // 递归
    private static Map<String, Integer> printMap(Map<String, Object> input, String s) {

        HashMap<String, Integer> hashMap = new HashMap<>();
        Set<Map.Entry<String, Object>> entrySet = input.entrySet();

        for (Map.Entry<String, Object> entry : entrySet) {
            String key = entry.getKey();
            Object value = entry.getValue();

            if (value instanceof Map){
                String prefix = s.isEmpty() ? key : s + "." + key;
                Map<String, Integer> map = printMap((Map<String, Object>) value, prefix);

                hashMap.putAll(map);

            }else {
                hashMap.put(s+"."+key,(int)value);
            }
        }


        return  hashMap;
    }
}
