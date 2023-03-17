package OrientedObject01;

/**
 * @author lzx
 * @date 2023/2/20 16:26
 * @description: TODO 形参是引用数据类型
 */
public class ValueTransferTest2 {
    public static void main(String[] args) {
        Data d1 = new Data();
        d1.m = 10;
        d1.n = 20;
        System.out.println("m = " + d1.m + ",n = "+ d1.n); //m = 10,n = 20

        ValueTransferTest2 test2 = new ValueTransferTest2();
        test2.swap(d1); //换序 传入的是地址,交换地址 值会变
        System.out.println("m = " + d1.m + ",n = "+ d1.n); //m = 20,n = 10
    }

    public void swap(Data data){
        int tmp = data.m;
        data.m = data.n;
        data.n = tmp;
    }
}
class Data {
    int m;
    int n;
}