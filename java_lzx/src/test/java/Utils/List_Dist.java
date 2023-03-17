package Utils;// package Utils;
//
// // import org.junit.jupiter.api.Test;Test
//
// import java.util.*;
// import java.util.stream.Collectors;
//
// /**
//  * @author lzx
//  * @date 2023/3/1 11:43
//  * @description: TODO 关于List去重操作
//  */
// public class List_Dist {
//     public  List<String> initList = Arrays.asList(
//             "张三",
//             "李四",
//             "张三",
//             "周一",
//             "刘四",
//             "李强",
//             "李白",
//             "张三",
//             "李强",
//             "王五"
//     );
//
//     @Test
//     //for循环重复坐标去重 复制一个 list2，再循环 List2，判断 list 中的元素的首尾出现的坐标位置是否一致，
//     // 如果一致，则说明没有重复的，否则重复，再删除重复的位置的元素。
//     public void distinct1(){
//         ArrayList<String> list1 = new ArrayList<>(initList);
//         ArrayList<String> list2 = new ArrayList<>(initList);
//         for (String element:list2){
//             if (list1.indexOf(element) != list1.lastIndexOf(element) ){
//                 list1.remove(element);
//             }
//         }
//         System.out.println(list1);
//     }
//
//     @Test
//     public void distinct2(){ //转为set 利用set集合的特性去重，但是顺序会打乱
//         HashSet<String> hashSet = new HashSet<>(initList);
//         ArrayList<String> list = new ArrayList<>(hashSet);
//         System.out.println(list.toString()); //[李强, 李四, 张三, 周一, 李白, 王五, 刘四]
//     }
//
//     @Test
//     // 元素顺序和原始 List 不一致，如果要保证顺序性，可以把 HashSet 换成 LinkedHashSet
//     public void distinct3(){
//         ArrayList<String> list = new ArrayList<>(new LinkedHashSet<>(initList));
//         System.out.println(list); //[张三, 李四, 周一, 刘四, 李强, 李白, 王五]
//     }
//
//     @Test
//     // Stream 的 distinct 方法去重，这个方法也十分简单，一行代码搞定！
//     public void distinct4(){
//         List list = new ArrayList<>(initList);
//         list = (List) list.stream().distinct().collect(Collectors.toList());
//         System.out.println(list); //[张三, 李四, 周一, 刘四, 李强, 李白, 王五]
//     }
// }
