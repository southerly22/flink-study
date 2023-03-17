package StringDemos;

/**
 * @author lzx
 * @date 2023/3/13 14:55
 * @description: TODO 去除字符串前后空格
 */
public class AG1 {
    public static void main(String[] args) {
        String str = " lzx    ";
        System.out.println(trim(str));
    }
    public static String trim(String str){
        if (str != null){
            int start = 0;
            int end = str.length()-1;
            while (start < end && str.charAt(start)==' '){
                start++;
            }
            while (start < end && str.charAt(end)==' '){
                end--;
            }
            if (str.charAt(start)==' ') return "";

            return str.substring(start,end+1);
        }
        return "";
    }
}
