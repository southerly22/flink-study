package BlockingQueue.DelayQueue;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

/**
 * @author lzx
 * @date 2023/6/25 15:23
 * @description: TODO 使用DelayQueue
 * 需求：
 *  用户下单，到达指定时间不处理订单过期
 */

@Data
@AllArgsConstructor
public class MyDelayMessage implements Delayed {

    //默认延时3s
    private static final long DELAY_MS= 1000L * 3;

    // 订单id
    private final String orderId;

    // 创建时间
    private final long crteateTS;

    // 过期时间
    private final long expire;

    public MyDelayMessage(String orderId) {
        this.orderId = orderId;
        this.crteateTS = System.currentTimeMillis();
        this.expire = this.crteateTS + DELAY_MS; // 计算出过期时间
    }

    public MyDelayMessage(String orderId, long expire) {
        this.orderId = orderId;
        this.crteateTS = System.currentTimeMillis();
        this.expire = this.crteateTS + expire * DELAY_MS; // 计算出过期时间
    }

    // todo 返回延迟时长
    @Override
    public long getDelay(TimeUnit unit) {
        // 根据当前时间计算
        return unit.convert(this.expire - System.currentTimeMillis(), TimeUnit.MILLISECONDS);
    }

    // todo 对插入的数据进行排序
    @Override
    public int compareTo(Delayed o) {
        return (int) (this.getDelay(TimeUnit.MILLISECONDS) - o.getDelay(TimeUnit.MILLISECONDS));
    }
}
