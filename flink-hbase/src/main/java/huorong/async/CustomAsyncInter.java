package huorong.async;

import com.alibaba.fastjson.JSONObject;

public interface CustomAsyncInter<T> {
    // 得到key
    String getKey(T input);
    // 补充 维度信息
     void join(T input, JSONObject jsonObject);
}
