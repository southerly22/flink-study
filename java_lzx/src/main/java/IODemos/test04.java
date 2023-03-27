package IODemos;

/**
 * @author lzx
 * @date 2023/3/24 15:06
 * @description: TODO
 */
public class test04 {
    public static void main(String[] args) {
        int start =1;
        int end = 3;
        aa(start,end);
        aa(3,7);

        System.out.println(start);
    }
    public static void aa(int start,int end){
        while (start <= end){
            System.out.println(start);
            start++;
        }
    }
}
