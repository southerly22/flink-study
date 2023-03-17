package AbstractDemo;

/**
 * @author lzx
 * @date 2023/3/10 18:25
 * @description: TODO 继承抽象类 必须重写父类的所有抽象方法
 */
public class Truck extends Vehicle{
    @Override
    public double calcFuelEfficiency() {
        System.out.println("卡车的燃油效率...");
        return 20;
    }

    @Override
    public double calcTripDistance() {
        System.out.println("卡车的行驶距离...");
        return 200;
    }
}
