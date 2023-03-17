package StaticDemo;

/**
 * @author lzx
 * @date 2023/3/10 10:42
 * @description: TODO 单例 -- 懒汉
 */
public class Singleton_Lazy {
    //1.私有构造器
    private Singleton_Lazy() {
    }
    //2.私有静态实例
    private static Singleton_Lazy singleton_lazy;

    //3.公共获取对象实例的静态方法
    public static Singleton_Lazy getInstance(){
        if (singleton_lazy == null){
            singleton_lazy =  new Singleton_Lazy();
        }
        return singleton_lazy;
    }
}
