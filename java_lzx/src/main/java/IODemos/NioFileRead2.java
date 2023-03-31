package IODemos;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.channels.CompletionHandler;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author lzx
 * @date 2023/3/29 13:04
 * @description: TODO java nio + 多线程的方式 读文件
 */
public class NioFileRead2 {
    public static void main(String[] args) throws IOException, ExecutionException, InterruptedException {
        Path inputFile = Paths.get("C:\\Users\\HR\\Desktop\\JavaIO\\readFile.txt");
        Path outputFile = Paths.get("C:\\Users\\HR\\Desktop\\JavaIO\\writeFile2.txt");

        // 创建线程池
        ExecutorService executor = Executors.newFixedThreadPool(10);

        FileChannel inChannel = FileChannel.open(inputFile, StandardOpenOption.READ);
        long fileSize = inChannel.size();
        inChannel.close();

        long chunkSize = fileSize / 4;
        long position = 0L;

        //创建 MappedByteBuffer ,每个MappedByteBuffer对应文件的一部分
        MappedByteBuffer[] buffers = new MappedByteBuffer[4];

        // 创建Future数组，用于获取子线程返回的结果
        Future[] futures = new Future[4];

        // 分配任务给线程池
        for (int i = 0; i < 4; i++) {
            long size = (i == 3) ? fileSize - position : chunkSize * i;
            long startPos = position;
            position += size;

            futures[i] = executor.submit(()->{
                try {
                    // 将文件部分映射到MappedByteBuffer中
                    FileChannel fileChannel = FileChannel.open(inputFile, StandardOpenOption.READ);
                    MappedByteBuffer mappedByteBuffer = fileChannel.map(FileChannel.MapMode.READ_ONLY, startPos, size);
                    fileChannel.close();

                    // 读取mappedByteBuffer中的数据
                    byte[] bytes = new byte[(int) size];
                    mappedByteBuffer.get(bytes);

                    // 返回数据
                    return bytes;
                }catch (IOException e){
                    e.printStackTrace();
                }
                return null;
            });
        }

        //等待所有任务完成 并将结果合并到一个Byte数组中去
        byte[] result = new byte[(int) fileSize];
        for (int i = 0; i < 4; i++) {

            byte[] bytes = (byte[]) futures[i].get();
            System.arraycopy(bytes,0,result,(int) (i*chunkSize),bytes.length);
        }

        //关闭线程池
        executor.shutdown();

        //将结果写入新文件
        FileOutputStream outputStream = new FileOutputStream(outputFile.toString());
        outputStream.write(result);
        outputStream.close();

        System.out.println(new String(result, StandardCharsets.UTF_8));
    }
}
