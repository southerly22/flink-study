package Utils;

import org.junit.jupiter.api.Test;

/**
 * @author lzx
 * @date 2023/2/21 21:07
 * @description: TODO
 */
public class StringDemos {
    public static void main(String[] args) {

    }

    @Test
    public void delete(){ //删除 start  到 end 之间的字符
        StringBuilder s1 = new StringBuilder("helloWord");
        StringBuilder s2 = s1.delete(0,3);
        System.out.println(s2);
    }

    @Test
    public void deleteCharAt(){  //删除 指定offset的字符
        StringBuilder s1 = new StringBuilder("helloWord");
        StringBuilder s2 = s1.deleteCharAt(0);
        System.out.println(s2);
    }

    @Test
    public void indexOf(){ //在当前字符序列中查询str的第一次出现下标
        StringBuilder s1 = new StringBuilder("helloWord");
        int index = s1.indexOf("l");
        System.out.println(index);
    }

    @Test
    public void setLen(){
        StringBuilder s1 = new StringBuilder("helloWord");
        s1.setLength(4);
        System.out.println(s1);
    }

    @Test
    public void insert(){ // 在指定位置 插入指定字符
        StringBuilder s1 = new StringBuilder("helloWord");
        StringBuilder s2 = s1.insert(1, 2222);
        System.out.println(s2);
    }

    @Test
    public void setCharAt(){ // 将指定位置的字符串 换成指定的字符
        StringBuilder s1 = new StringBuilder("helloWord");
        s1.setCharAt(0, 'A');
        System.out.println(s1);
    }
}
