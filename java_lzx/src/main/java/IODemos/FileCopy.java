package IODemos;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

/**
 * @author lzx
 * @date 2023/3/24 16:09
 * @description: TODO 对比IO 和NIO 读写文件的性能
 */
public class FileCopy {
    public static void main(String[] args) {
        String inPath = "D:\\lzx_share\\CentOS-7-x86_64-Minimal-2009.iso";
        String outPath = "D:\\AIOTest\\CentOS-72.iso";
        File infile = new File(inPath);
        if (!infile.exists()) {
            return;
        }
        File outfile = new File(outPath);
        // fileChannel(infile,outfile);  //fileChannelRead读取完成, 缓冲区大小：20480 ,耗时：1034
        randomAccessRead(infile,outfile); // 1M 耗时：4507
    }

    /***
     * @Author: lzx
     * @Description: 使用 randomAccessRead读文件 + FileOutputStream写文件
     * @Date: 2023/3/24
     **/
    public static void randomAccessRead(File infile, File outfile) {
        //测试：读取1.23GB文件耗时：8768ms
        long d1 = System.currentTimeMillis();
        RandomAccessFile raf = null;
        OutputStream output = null;
        int bufferSize = 20480;
        try {
            raf = new RandomAccessFile(infile, "rw");
            output = new FileOutputStream(outfile);
            int len = 0;             //每次读取内容长度
            byte[] data = new byte[bufferSize];//内容缓冲区
            while ((len = raf.read(data)) != -1) {
                output.write(data, 0, len);
            }
            long d2 = System.currentTimeMillis();
            System.out.println("randomAccessRead读取完成， 缓冲区大小：" + bufferSize + ",耗时：" + (d2 - d1));
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (raf != null) {
                    raf.close();
                }
                if (output != null) {
                    output.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /***
     * @Author: lzx
     * @Description: 使用 NIO 的 FileChannel读写文件
     * @Date: 2023/3/24
     * Java的FileChannel读文件方式相比于普通的IO流方式更高效，主要原因有以下几点：
     *
     * FileChannel可以直接将文件映射到内存中，避免了不必要的数据拷贝，提高了读取文件的效率。
     *
     * FileChannel支持异步读取，可以在读取数据的同时进行其他操作，提高了程序的并发性能。
     *
     * FileChannel支持从文件的任意位置读取数据，而不像普通的IO流只能从文件的开头读取数据。
     *
     * FileChannel支持内存映射文件，可以将文件的一部分或全部映射到内存中，这样可以直接在内存中进行读写操作，避免了频繁的磁盘IO操作，提高了程序的性能。
     **/
    public static void fileChannel(File infile, File outfile) {
        long d1 = System.currentTimeMillis();
        FileInputStream in = null;
        FileOutputStream out = null;
        FileChannel fic = null;
        FileChannel foc = null;
        int bufferSize = 20480;
        try {
            in = new FileInputStream(infile);
            out = new FileOutputStream(outfile);
            fic = in.getChannel();
            foc = out.getChannel();
            ByteBuffer buf = ByteBuffer.allocate(bufferSize);
            while (fic.read(buf) != -1) {
                buf.flip();
                foc.write(buf);
                buf.clear();
            }
            long d2 = System.currentTimeMillis();
            System.out.println("fileChannelRead读取完成, 缓冲区大小：" + bufferSize + " ,耗时：" + (d2 - d1));
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
                if (out != null) {
                    out.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
