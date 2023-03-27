package IODemos;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * @author lzx
 * @date 2023/3/22 14:10
 * @description: TODO RandomAccessFile读文件 ，实现从任意一行开始读取
 */
public class RandomAccessFileReadLine {
   public static final String FILE_PATH = "C:\\Users\\HR\\Desktop\\JavaIO\\readFile.txt";
    // 记录每一行开头的指针位置
    public static final List<Integer> POINT_LIST = new ArrayList<>();
    // 静态代码块
    static {
        try {
            BufferedReader br = new BufferedReader(new FileReader(FILE_PATH));
            // 记录每一行字节大小
            int lineSize = 0;
            // 记录已经读取到的字节总大小
            int totalSize = 0;
            String readLine = null;
            while ((readLine = br.readLine()) != null) {
                lineSize = readLine.getBytes().length;
                POINT_LIST.add(totalSize);
                //每行有换行符 需要+2
                totalSize += lineSize +2;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static void main(String[] args) {
        String s = readLine(25);
        System.out.println(s);
        System.out.println(readLineInterval(3, 6));
    }

    // 获取指定行
    public static String readLine(int lineNum){
        if (lineNum <= 0 || lineNum > POINT_LIST.size()){
            throw new IllegalArgumentException(String.format("lineNum must between %s and %s",0,POINT_LIST.size()));
        }
        try {
            RandomAccessFile raf = new RandomAccessFile(new File(FILE_PATH),"r");
            raf.seek(POINT_LIST.get(lineNum - 1));
            byte[] bytes = raf.readLine().getBytes(StandardCharsets.ISO_8859_1);
            return new String(bytes,StandardCharsets.UTF_8);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
    // 获取开始行和结束行之间的数据 [start,end]
    public static String readLineInterval(int start,int end){
        RandomAccessFile raf = null;
        StringBuilder builder = new StringBuilder(end - start);
        int cnt = start;
        if (start <= 0 || end > POINT_LIST.size()){
            throw new IllegalArgumentException(String.format("lineNum must between %s and %s",0,POINT_LIST.size()));
        }
        try {
            raf = new RandomAccessFile(new File(FILE_PATH),"r");
            raf.seek(POINT_LIST.get(start - 1));
            while (cnt <= end){
                byte[] bytes = raf.readLine().getBytes(StandardCharsets.ISO_8859_1);
                builder.append(new String(bytes,StandardCharsets.UTF_8));
                builder.append("\n");
                cnt ++;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return builder.toString();
    }
}
