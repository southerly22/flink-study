/**
 * Java的接口
 *
 * @author lzx
 * @date 2023/03/12 15:53
 **/
public class TestInterface {
    public static void main(String[] args) {
        //1.创建接口实现类的对象
        Computer computer = new Computer();
        Usb usb = new Printer(); //接口的多态性
        computer.transfer(usb);

        // 2.创建接口实现类的 匿名对象
        computer.transfer(new Camera());

        // 3.创建接口 匿名实现类
        Usb usb1 = new Usb() { //接口没有构造器，必须实现接口的所有抽象方法
            @Override
            public void start() {
                System.out.println("移动硬盘开始工作！");
            }

            @Override
            public void end() {
                System.out.println("移动硬盘停止工作！");
            }
        };
        computer.transfer(usb1);

        // 4.创建接口匿名实现类 的匿名对象
        computer.transfer(new Usb(){

            @Override
            public void start() {
                System.out.println("摄像头开始工作！");
            }

            @Override
            public void end() {
                System.out.println("摄像头关闭工作！");
            }
        });
    }
}

class Computer{
    public void transfer(Usb usb){
        System.out.println("设备开始工作。。。");
        usb.start();

        System.out.println("数据传输完成。。。");
        usb.end();
    }
}

class Camera implements Usb{

    @Override
    public void start() {
        System.out.println("相机开始工作！");
    }

    @Override
    public void end() {
        System.out.println("相机关闭工作！");
    }
}

class Printer implements Usb {

    @Override
    public void start() {
        System.out.println("打印机开始工作...");
    }

    @Override
    public void end() {
        System.out.println("打印机工作完成...");
    }
}

//接口中的 常量都是finall的，方法也都是抽象方法，实现接口必须实现该接口的所有抽象方法
interface Usb{
    public abstract void start();
    void end();
}
