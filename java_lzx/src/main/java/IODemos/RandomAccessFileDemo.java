package IODemos;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * @author lzx
 * @date 2023/3/22 14:10
 * @description: TODO RandomAccessFile读文件 ，回车占 2个字节。 无法按行读
 */
public class RandomAccessFileDemo {
    public static void main(String[] args) {
        String filePath = "C:\\Users\\HR\\Desktop\\JavaIO\\RandomAccessFile.txt";
        File file = new File(filePath);
        RandomAccessFileDemo rafDemo = new RandomAccessFileDemo();
        rafDemo.readAndWrite(file);
    }

    public void readFile(File file,long start,long end){
        RandomAccessFile raf = null;
        try {
            raf = new RandomAccessFile(file, "r");
            raf.seek(0);
            byte[] buffer = new byte[42];
            int len;
            while ((len = raf.read(buffer)) > 0) {
                System.out.println(new String(buffer, 0, len));
                System.out.println("--------");
            }
        }catch (IOException e){
            e.printStackTrace();
        }finally {
            if (raf!=null) {
                try {
                    raf.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    //写文件 另外指定位置读取文件
    public void readAndWrite(File file){
        try {
            if (!file.exists()) {
                file.createNewFile();
            }
        }catch (IOException e){
            e.printStackTrace();
        }
        try {
            RandomAccessFile raf = new RandomAccessFile(file, "rw");
            String str1 = "晴天，阴天，多云，小雨，大风，中雨，小雪，雷阵雨";    // 要写入的字符串
            String str2 = new String(str1.getBytes("GBK"),"ISO-8859-1");    // 编码转换
            raf.writeBytes(str2);

            System.out.println("当前指针位置： "+raf.getFilePointer());
            raf.seek(6); //移动文件指针
            System.out.println("从文件头跳过6个字节。。。");
            byte[] buffer = new byte[2];
            int len;
            while ((len = raf.read(buffer, 0, 2)) != -1) {
                String s = new String(buffer, 0, len, "gbk"); //读文件 编码转换
                System.out.print(s);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
