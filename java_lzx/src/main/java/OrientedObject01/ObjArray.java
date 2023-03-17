package OrientedObject01;

/**
 * @author lzx
 * @date 2023/3/2 10:10
 * @description: TODO 对象数组内存解析
 */
public class ObjArray {
    public static void main(String[] args) {
        Obj[] arr = new Obj[5];
        for (int i = 0; i < arr.length; i++) {
            arr[i] = new Obj();
        }
        arr[0].number = 135;
        arr[0].score = 90;
        arr[0].state = 2;

        for (int i = 0; i < arr.length; i++) {
            System.out.println(arr[i].toString());
        }
    }
}
class Obj{
    int number;
    int state;
    int score;

    @Override
    public String toString() {
        return "Obj{" +
                "number=" + number +
                ", state=" + state +
                ", score=" + score +
                '}';
    }
}
