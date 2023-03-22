package IODemos;

import org.apache.commons.io.monitor.FileAlterationMonitor;
import org.apache.commons.lang3.time.StopWatch;

import java.io.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author lzx
 * @date 2023/3/22 14:53
 * @description: TODO 多线程读文件  RandomAccessFile
 */
public class MultiThreadReadFile{
    public static void main(String[] args) {
        String filePath ="";
        int numThreads = 10;
        File file = new File(filePath);

        long fileSize = file.length();
        long chunkSize = fileSize / numThreads;
        long remainSize = fileSize % numThreads; //不足一批的数量

        //创建线程池
        ExecutorService threadPool = Executors.newFixedThreadPool(numThreads);
        for (int i = 0; i < numThreads; i++) {
            long start = i * chunkSize;
            long end = ( i+1 ) * chunkSize -1;

            if (i == numThreads-1){
                end += remainSize;
            }
            threadPool.execute(new FileReadTask(file,start,end));
        }
        threadPool.shutdown();
    }

    // 读文件
    static class FileReadTask implements Runnable{
        private File file;
        private long start;
        private long end;

        public FileReadTask(File file, long start, long end) {
            this.file = file;
            this.start = start;
            this.end = end;
        }

        @Override
        public void run() {
            try {
                RandomAccessFile raf = new RandomAccessFile(file, "r");
                raf.seek(start);
                byte[] buffer = new byte[1024];
                int len;
                while ((len = raf.read(buffer)) > 0 && raf.getFilePointer() <= end) {
                    //逻辑处理
                    System.out.println(new String(buffer, 0, len));
                }
            }catch (IOException e){
                e.printStackTrace();
            }
        }
    }
}
