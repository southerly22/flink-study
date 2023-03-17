package StaticDemo;

/**
 * @author lzx
 * @date 2023/3/10 14:12
 * @description: TODO
 */
public class FieldTest {
    public static void main(String[] args) {
        Order order = new Order();
        System.out.println(order.orderId);
    }
}
class Order{
    {
        orderId =2;
    }
    int orderId = 1;

    public Order() {
        super();
        // this.orderId = 3;
    }
}
