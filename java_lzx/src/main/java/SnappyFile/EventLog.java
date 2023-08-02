package SnappyFile;

import org.xerial.snappy.Snappy;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * @author lzx
 * @date 2023/5/4 11:08
 * @description: TODO
 */
public class EventLog {
    public static void main(String[] args) throws Exception {
        String fileName = "C:\\Users\\HR\\Downloads\\events_10_application_1680777792237_2441_1.snappy";
        RandomAccessFile randomFile = new RandomAccessFile(fileName, "r");
        int fileLength = (int) randomFile.length();
        randomFile.seek(0);
        byte[] bytes = new byte[fileLength];
        int byteread = randomFile.read(bytes);
        System.out.println(fileLength);
        System.out.println(byteread);
        byte[] uncompressed = Snappy.uncompress(bytes);
        String result = new String(uncompressed, "UTF-8");
        System.out.println(result);
    }
}
