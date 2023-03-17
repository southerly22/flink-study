package StaticDemo;

/**
 * @author lzx
 * @date 2023/3/10 10:34
 * @description: TODO
 */
public class TestSingleton {
    public static void main(String[] args) {
        Singleton instance = Singleton.getInstance();
        // Singleton s1 = new Singleton(); 私有的构造器 无法new出来
    }
}
