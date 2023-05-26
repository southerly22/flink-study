package MultiThread;

import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.time.StopWatch;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collector;
import java.util.stream.Collectors;

/**
 * 电商比价项目demo
 *
 * @author lzx
 * @date 2023/05/25 17:20
 **/
public class CompletableFuture_MallDemo {
    static List<NetMall> list = Arrays.asList(
            new NetMall("jd"),
            new NetMall("tb"),
            new NetMall("pdd"),
            new NetMall("dd")
    );

    /**
     * 同步查询 比价
     *
     * @param netMallList 网站名字list
     * @param productName 产品名字
     * @return java.util.List<java.lang.String>
     * @author lzx
     * @date 2023-05-25 17:28
     */
    public static List<String> getPrice(List<NetMall> netMallList, String productName) {

        return netMallList
                .stream()
                .map(netMall -> {
                    //<<Mysql>> in jd price is 89.98
                    return String.format("<<Mysql>> in %s price is %.2f", netMall.getNetMallName(), netMall.calcPrice(productName));
                })
                .collect(Collectors.toList());
    }

    /**
     * 异步查询价格
     *
     * @param netMallList
     * @param productName
     * @return java.util.List<java.lang.String>
     * @author lzx
     * @date 2023-05-25 17:48
     */
    public static List<String> asyncGetPrice(List<NetMall> netMallList, String productName) {
        return netMallList
                .stream()
                .map(netMall ->
                        CompletableFuture.supplyAsync(() -> String.format("<<Mysql>> in %s price is %.2f", netMall.getNetMallName(), netMall.calcPrice(productName)))
                ).collect(Collectors.toList())
                .stream()
                .map(s -> s.join())
                .collect(Collectors.toList());
    }

    public static void main(String[] args) {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        List<String> mySql = getPrice(list, "MySql");
        for (String m : mySql) {
            System.out.println(m);
        }
        stopWatch.stop();
        System.out.println("耗时: " + stopWatch.getTime());

        stopWatch.reset();
        stopWatch.start();
        List<String> mySql1 = asyncGetPrice(list, "MySql");
        for (String s : mySql1) {
            System.out.println(s);
        }
        stopWatch.stop();
        System.out.println("耗时: " + stopWatch.getTime());
    }
}

class NetMall {
    @Getter
    private String netMallName;

    public NetMall(String netMallName) {
        this.netMallName = netMallName;
    }

    public double calcPrice(String productName) {
        // 模拟网络 延迟
        try {
            TimeUnit.SECONDS.sleep(1);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return ThreadLocalRandom.current().nextDouble() * 2 + productName.charAt(0);
    }
}