package StringDemos;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * @author lzx
 * @date 2023/3/13 15:46
 * @description: TODO 获取“ ab”在 “abkkcadkabkebfkabkskab” 中出现的次数
 */
public class AG3 {
    public static void main(String[] args) {
        String subStr = "ab";
        String mainStr = "babkkcadkabkebfkabkskab";
        System.out.println(appearCnt(mainStr, subStr));
        System.out.println(appearCnt2(mainStr, subStr));
    }

    public static int appearCnt(String mainStr,String subStr){
        int index = 0;
       int count = 0;
       while ((index = mainStr.indexOf(subStr,index))!=-1){
           index +=subStr.length();
           count++;
       }
        return count;
    }

    public static int appearCnt2(String mainStr,String subStr){
        char[] mainArr = mainStr.toCharArray();
        char[] subArr = subStr.toCharArray();
        int count = 0;
        for (int i = 0; i < mainArr.length; i++) {
            int temp = i;
            for (int j = 0; j < subArr.length; j++) {
                   if (mainArr[temp] != subArr[j]){
                       break;
                   }else {
                       temp++;
                       if (j == subArr.length - 1){
                           count++;
                       }
                   }
            }
        }
        return count;
    }
}
