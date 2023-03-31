package IODemos;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;

/**
 * @author lzx
 * @date 2023/3/29 17:07
 * @description: TODO
 */
public class FileChannelReadLine {
    private static String filepath = "C:\\Users\\HR\\Desktop\\JavaIO\\readFile.txt";
    private static int bufferSize = 1024 * 1024;

    public static void main(String[] args) throws IOException {

        RandomAccessFile randomAccessFile = new RandomAccessFile(filepath, "r");
        FileChannel channel = randomAccessFile.getChannel();
        ByteBuffer buffer = ByteBuffer.allocate(bufferSize);

        int bytesRead = channel.read(buffer);
        ByteBuffer stringBuilder = ByteBuffer.allocate(20);

        while (bytesRead != -1) {
            System.out.println("读取字节数" + bytesRead);
            buffer.flip(); //切换模式
            while (buffer.hasRemaining()) {
                byte b = buffer.get();
                if (b == 13) { //换行和回车
                    stringBuilder.flip();
                    final String line = StandardCharsets.UTF_8.decode(stringBuilder).toString();
                    System.out.println("line--> " + line);
                    stringBuilder.clear();
                } else {
                    if (stringBuilder.hasRemaining()) {
                        stringBuilder.put(b);
                    } else {
                        // 空间不够 扩容
                        stringBuilder = reAllocate(stringBuilder);
                        stringBuilder.put(b);
                    }
                }
            }
            buffer.clear();
            bytesRead = channel.read(buffer);
        }
        randomAccessFile.close();
    }

    // 扩容
    static ByteBuffer reAllocate(ByteBuffer stringBuilder) {
        final int capacity = stringBuilder.capacity();
        byte[] newBuffer = new byte[capacity * 2];
        System.arraycopy(stringBuilder.array(), 0, newBuffer, 0, capacity);
        return (ByteBuffer) ByteBuffer.wrap(newBuffer).position(capacity);
    }
}
