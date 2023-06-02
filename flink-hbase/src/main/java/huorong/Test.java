package huorong;

import org.apache.commons.lang.StringEscapeUtils;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * @author lzx
 * @date 2023/5/26 17:54
 * @description: TODO
 */
public class Test {
    public static void main(String[] args){
        String str = "Binary: format: TCPDUMP's style capture (.ACP/PCAP)";
        String concat = "'".concat(str).concat("'");
        // System.out.println(str);
        // System.out.println(concat);

        // System.out.println(str.replaceAll("'", "''"));
        // System.out.println(StringEscapeUtils.escapeSql(str));

        System.out.println(str.replaceAll("'", "\\'"));
    }
}
