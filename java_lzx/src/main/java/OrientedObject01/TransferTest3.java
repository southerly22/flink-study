package OrientedObject01;

/**
 * @author lzx
 * @date 2023/2/20 16:53
 * @description: TODO
 */
public class TransferTest3 {
    public static void main(String[] args) {
        TransferTest3 test3 = new TransferTest3();
        test3.first();
    }
    public void first(){
        int i = 5;
        Value v = new Value();
        v.i = 25;
        second(v,i);
        System.out.println(v.i); // 20
    }
    public void second(Value v ,int i){
        i = 0;
        v.i = 20;
        Value val = new Value();
        v = val;
        System.out.println(v.i + " " + i); //15,0
    }
}
class Value{
    int i =15;
}
