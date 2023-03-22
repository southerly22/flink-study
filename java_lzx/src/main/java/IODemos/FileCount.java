package IODemos;

import org.apache.commons.lang3.time.StopWatch;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;

/**
 * @author lzx
 * @date 2023/3/22 15:46
 * @description: TODO 大文件 计算行数
 */
public class FileCount {
    public static void main(String[] args) {
        String filePath = "C:\\Users\\HR\\Desktop\\sha1Id.csv";
        File file = new File(filePath);
        FileCount fileCount = new FileCount();
        fileCount.fileCnt1(file);
    }

    // 计算文件行数 【行数： 107746,耗时： 24】
    public void fileCnt1(File file){
        StopWatch watch = new StopWatch();
        watch.start();
        try {
            if (file.exists()){
                long fileLen = file.length();
                LineNumberReader lineNumberReader = new LineNumberReader(new FileReader(file));
                lineNumberReader.skip(fileLen);
                int lines = lineNumberReader.getLineNumber();
                watch.stop();
                System.out.println("行数： "+lines+",耗时： "+watch.getTime());
                lineNumberReader.close();
            }
        }catch (IOException e){
            e.printStackTrace();
        }
    }
    public void fileCnt2(File file){

    }
}
