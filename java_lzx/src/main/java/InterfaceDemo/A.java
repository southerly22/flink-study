package InterfaceDemo;

/**
 * @author lzx
 * @date 2023/05/25 16:31
 **/
public class A {
   private TestInterFace testInterFace;

    public A(TestInterFace testInterFace) { // 多态 向上/向下转型
        this.testInterFace = testInterFace;
        doSth();
    }
    public void doSth(){
        System.out.println("this is a message");
    }
}

class B implements TestInterFace{
    public static void main(String[] args) {
        new A(new B());
    }

    @Override
    public void systemStr(String str) {
        System.out.println(str);
    }
}