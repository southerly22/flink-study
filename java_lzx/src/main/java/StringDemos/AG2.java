package StringDemos;


/**
 * @author lzx
 * @date 2023/3/13 15:02
 * @description: TODO 将字符串中指定部分进行反转，如： “ab`cdef`g”反转为”ab`fedc`g”
 *                  substring ( ]
 */
public class AG2 {
    public static void main(String[] args) {
        String str = "abcdefg";
        System.out.println(reverse(str, 2, 5));
        System.out.println(str.charAt(6));
    }
    public static String reverse(String str,int start,int end){
        if (str != null || start > end){
            if (start==end) return str;
            String s = str.substring(0, start);
            StringBuilder s1 = new StringBuilder(str.length());
            s1.append(s);
            for (int i = end; i >=start ; i--) {
                s1.append(str.charAt(i));
            }
            s1.append(str.substring(end + 1));
            return s1.toString();
        }
        return "";
    }
}
