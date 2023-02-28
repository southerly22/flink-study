package OrientedObject01;

/**
 * @author lzx
 * @date 2023/2/20 16:26
 * @description: TODO 形参是基本数据类型
 */
public class ValueTransferTest1 {
    public static void main(String[] args) {
        int m = 10;
        int n = 20;
        System.out.println("m = " + m + ",n = "+n);

        // 际参数值的副本（复制品）传入方法内，而参数本身不受影响。
        ValueTransferTest1 test1 = new ValueTransferTest1();
        test1.swap(m,n);

        // int tmp = m;
        // m = n;
        // n = tmp;

        System.out.println("m = " + m + ",n = "+n);
    }
    public void swap(int m ,int n){
        int tmp = m;
        m = n;
        n = tmp;
    }
}
