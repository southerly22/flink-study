package flink_sql.test;

import java.nio.charset.StandardCharsets;

/**
 * @author lzx
 * @date 2023/6/29 18:40
 * @description: TODO
 */
public class test {
    public static void main(String[] args) {
        String s = new String(new byte[1], StandardCharsets.UTF_8);
        System.out.println(s);
    }
}
