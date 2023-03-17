package myTest;

import com.alibaba.fastjson.JSONObject;

import java.util.HashMap;

/**
 * @author lzx
 * @date 2023/3/1 17:11
 * @description: TODO
 */
public class Test01 {
    public static void main(String[] args) {

        String[] s = {"5015100001,c5,首页",
                "5015100002,c6,A页",
                "5015100001,c4,B页",
                "5015100002,c6,C页",
                "5015100001,c5,D页"};

        HashMap<String, JSONObject> resMap = new HashMap<>();

        for (String s1 : s) {
            String[] split = s1.split(",");
            String rowKey = split[0];
            String qualifier = split[1];
            String value = split[2];

            if (resMap.containsKey(rowKey)) {
                resMap.get(rowKey).put(qualifier, value);
            } else {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put(qualifier, value);
                resMap.put(rowKey, jsonObject);
            }
        }

        System.out.println(resMap.toString());
    }
}
