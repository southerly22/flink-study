package AbstractDemo;

/**
 * @author lzx
 * @date 2023/3/10 18:23
 * @description: TODO 抽象类
 */
public abstract class Vehicle {
    public abstract double calcFuelEfficiency();	//计算燃料效率的抽象方法
    public abstract double calcTripDistance();	//计算行驶距离的抽象方法
}