package polymorphism;

/**
 * @author lzx
 * @date 2023/3/9 16:37
 * @description: TODO
 */
public class InterviewTest2 {
    public static void main(String[] args) {
        Father father = new Father();
        Son son = new Son();
        System.out.println(father.getInfo());
        System.out.println(son.getInfo());
        son.test();
        System.out.println("------------------------------");
        son.setInfo("大硅谷");
        System.out.println(father.getInfo()); //atguigu
        System.out.println(son.getInfo()); // 大硅谷
        son.test(); //大硅谷 atguigu

    }

}
class Father {
    private String info = "atguigu";

    public void setInfo(String info) {
        this.info = info;
    }

    public String getInfo() {
        return info;
    }
}

class Son extends Father {
    private String info = "尚硅谷";

    public void setInfo(String info) {
        this.info = info;
    }

    public String getInfo() {
        return info;
    }

    public void test() {
        System.out.println(this.getInfo());
        System.out.println(super.getInfo());
    }
}