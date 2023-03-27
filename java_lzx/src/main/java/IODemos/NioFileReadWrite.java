package IODemos;

import java.io.IOException;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * NIO＋Java多线程的方式读写文件
 *
 * @author lzx
 * @date 2023/03/26 23:00
 **/
public class NioFileReadWrite {
    private static final int BUFFER_SIZE = 1024; //缓冲区大小
    private static final int THREAD_POOL = 10; //线程池大小

    public static void main(String[] args) throws IOException, InterruptedException {
        Path inputFile = Paths.get("C:\\Users\\HR\\Desktop\\JavaIO\\readFile.txt");
        Path outputFile = Paths.get("C:\\Users\\HR\\Desktop\\JavaIO\\writeFile.txt");
        if (outputFile.toFile().exists()) {
            outputFile.toFile().delete();
        }
        // 使用java NIO读取文件内容
        FileChannel inputChannel = FileChannel.open(inputFile);
        ByteBuffer inputBuffer = ByteBuffer.allocate(BUFFER_SIZE);
        StringBuilder inputData = new StringBuilder();

        while (inputChannel.read(inputBuffer) != -1) {
            inputBuffer.flip();
            inputData.append(StandardCharsets.UTF_8.decode(inputBuffer));
            inputBuffer.clear();
        }
        inputChannel.close();

        //创建线程池
        ExecutorService executor = Executors.newFixedThreadPool(THREAD_POOL);

        //使用Java NIO写入文件
        // StandardOpenOption枚举来指定CREATE和WRITE选项，这将创建一个新文件（如果文件不存在）并以写模式打开文件通道。
        FileChannel outputChannel = FileChannel.open(outputFile, StandardOpenOption.CREATE, StandardOpenOption.WRITE);
        ByteBuffer outputBuffer = ByteBuffer.allocate(BUFFER_SIZE);
        int numThread = THREAD_POOL;
        int chunkSize = inputData.length() / numThread;
        for (int i = 0; i < numThread; i++) {
            int start = i * chunkSize;
            int end = (i == numThread - 1) ? inputData.length() : (i + 1) * chunkSize;
            String chunkData = inputData.substring(start, end);

            executor.execute(new WriteChunkToFile(outputChannel, outputBuffer, chunkData));
        }
        executor.shutdown();
        while (!executor.isTerminated()) {
            Thread.sleep(100);
        }
        outputChannel.close();
        System.out.println("文件读写完成！");
    }

    private static class WriteChunkToFile implements Runnable {
        private FileChannel fileChannel;
        private ByteBuffer byteBuffer;
        private String data;

        public WriteChunkToFile(FileChannel fileChannel, ByteBuffer byteBuffer, String data) {
            this.fileChannel = fileChannel;
            this.byteBuffer = byteBuffer;
            this.data = data;
        }

        @Override
        public void run() {
            synchronized (byteBuffer){
                byteBuffer.clear();
                byteBuffer.put(data.getBytes(StandardCharsets.UTF_8));
                System.out.println(Thread.currentThread().getName()+"当前线程的长度： "+data.getBytes(StandardCharsets.UTF_8).length);
                byteBuffer.flip();
                try {
                    fileChannel.write(byteBuffer);
                } catch (IOException e) {
                    System.out.println("写入文件出错！");
                }
            }
        }
    }
}
