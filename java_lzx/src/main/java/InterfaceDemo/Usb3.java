package InterfaceDemo;

public interface Usb3 {
    //定义常量
    long MAX_SPEED= 500*1024*1024;

    //抽象方法
    void in();
    void out();

    //默认方法
    default void start(){
        System.out.println("usb3 开始");
    }
    default void stop(){
        System.out.println("usb3 关闭");
    }
    //静态方法
    static void show(){
        System.out.println("usb 3.0可以同步全速地进行读写操作！");
    }
}
