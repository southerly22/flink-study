package huorong.entity;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.beanutils.BeanUtils;

import java.lang.reflect.InvocationTargetException;

/**
 * @author lzx
 * @date 2023/6/6 13:12
 * @description: TODO
 */
public class Test {
    public static void main(String[] args) throws InvocationTargetException, IllegalAccessException, NoSuchMethodException {

        SampleUserdefinedScan sampleUserdefinedScan = new SampleUserdefinedScan();
        sampleUserdefinedScan.setRk("f3f39227924ef9c814b12e08a5a2eefef94af9e6");
        sampleUserdefinedScan.setDie("Binary: Nothing found");
        JSONObject jsonObject = (JSONObject) JSON.toJSON(sampleUserdefinedScan);
        System.out.println(jsonObject);


    }
}
