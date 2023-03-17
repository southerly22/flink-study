package AbstractDemo;

/**
 * @author lzx
 * @date 2023/3/10 18:30
 * @description: TODO
 */
public class RiverBarge extends Vehicle {
    @Override
    public double calcFuelEfficiency() {
        System.out.println("驳船的燃油效率");
        return 0;
    }

    @Override
    public double calcTripDistance() {
        System.out.println("驳船的行驶距离");
        return 0;
    }
}
