package InterfaceDemo;

import ObjectTest.TestEquals;

/**
 * @author lzx
 * @date 2023/3/12 17:44
 * @description: TODO
 */
public class TestUsb implements Usb2,Usb3{
    public static void main(String[] args) {
        Usb3.show(); //接口的静态方法
        TestUsb testUsb = new TestUsb();
        testUsb.start();
        testUsb.stop();
    }

    @Override
    public void in() {
        System.out.println("in");
    }

    @Override
    public void out() {
        System.out.println("out");
    }

    @Override
    public void start() {
        Usb3.super.start(); //通过“`接口名.super.方法名`"的方法选择保留哪个接口的默认方法。
    }

    @Override
    public void stop() {
        Usb2.super.stop();
    }
}
