package huorong.async;

import com.alibaba.fastjson.JSONObject;

import java.util.List;

public interface CustomAsyncInter<T> {
    // 得到key
    String getKey(T input);
    // 补充 维度信息
    // void join(T input, JSONObject jsonObject);
    void join(T input, List<JSONObject> dimInfoList);
}
