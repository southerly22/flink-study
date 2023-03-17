package StaticDemo;

import OrientedObject01.ValueTransferTest1;

/**
 * @author lzx
 * @date 2023/3/10 10:29
 * @description: TODO 单例设计模式
 */
public class Singleton {
    // 1.私有的构造器 外部无法用new 关键字创建该类的对象
    private Singleton() {}

    //2.内部提供一个当前类的实例化 必须是静态的
    private static Singleton single = new Singleton();

    //3.必须提供一个公共的静态方法，返回当前的类的对象
    public static Singleton getInstance(){
        return single;
    }
}
