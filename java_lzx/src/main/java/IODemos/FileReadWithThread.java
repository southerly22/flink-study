package IODemos;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
/**
 * @author lzx
 * @date 2023/3/29 16:14
 * @description: TODO
 */
public class FileReadWithThread {
    public static void main(String[] args) throws IOException {
        // 创建线程池
        ExecutorService executorService = Executors.newFixedThreadPool(10);

        // 打开文件并创建FileChannel
        Path filePath = Paths.get("test.txt");
        FileChannel fileChannel = FileChannel.open(filePath);

        // 创建ByteBuffer数组
        ByteBuffer[] byteBuffers = new ByteBuffer[10];
        for (int i = 0; i < byteBuffers.length; i++) {
            byteBuffers[i] = ByteBuffer.allocate(1024);
        }

        long position = 0;
        long fileSize = Files.size(filePath);
        while (position < fileSize) {
            // 读取文件数据
            int bytesRead = (int) fileChannel.read(byteBuffers);

            // 处理读取到的数据
            for (ByteBuffer byteBuffer : byteBuffers) {
                byteBuffer.flip(); // 切换为读模式
                executorService.submit(() -> process(byteBuffer)); // 提交到线程池中处理
                byteBuffer.clear(); // 切换为写模式
            }

            // 更新position
            position += bytesRead;
        }

        // 关闭FileChannel和线程池
        fileChannel.close();
        executorService.shutdown();
    }

    private static void process(ByteBuffer byteBuffer) {
        // 处理ByteBuffer中的数据
        // ...
    }

}
