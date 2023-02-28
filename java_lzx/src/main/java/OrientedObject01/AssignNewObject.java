package OrientedObject01;

/**
 * @author lzx
 * @date 2023/2/20 16:41
 * @description: TODO
 */
public class AssignNewObject {
    public void swap(Data data){
        data = new Data();
        int tmp = data.m;
        data.m = data.n;
        data.n = tmp;
    }
    public static void main(String[] args) {
        Data data2 = new Data();
        data2.m = 10;
        data2.n = 20;
        System.out.println("data2.m = " + data2.m + " data2.n = " + data2.n);
        AssignNewObject assignNewObject = new AssignNewObject();

        assignNewObject.swap(data2);
        System.out.println("data2.m = " + data2.m + " data2.n = " + data2.n);
    }
}
