package myTest;

import org.junit.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * @author lzx
 * @date 2023/09/20 23:23
 **/
public class Test02 {



    @Test
    public void test03(){
        HashMap<Integer, Integer> hashMap = new HashMap<>();

        for (int i = 1; i < 10; i++) {
            int k = new Random().nextInt(i);
            int v = new Random().nextInt(i);
            hashMap.put(k,v);
        }
        hashMap.forEach((k,v)->{
            System.out.println(k+","+v);
        });
    }


}
