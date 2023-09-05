package flink_core.test;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static com.ibm.icu.impl.ValidIdentifiers.Datatype.x;

/**
 * @author lzx
 * @date 2023/08/28 14:52
 **/
public class Test11 {
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
        //System.out.println(x);
    }

    public static Map<String, Integer> printMap(Map<String,Object> map,String prefix) {
        HashMap<String, Integer> res = new HashMap<>();

        for (Map.Entry<String, Object> entry : map.entrySet()) {

            String key = entry.getKey();
            Object value = entry.getValue();

            if (value instanceof Map){
                String keyStr = prefix.isEmpty() ? key : prefix + "." + key;
                Map<String, Integer> printMap = printMap((Map<String, Object>) value, keyStr);
                res.putAll(printMap);
            }else {
                res.put(prefix +"." +key,(Integer) value);
                //System.out.println(key+"+"+value);
            }
        }

        return res;
    }
}
