package IODemos;

/**
 * @author lzx
 * @date 2023/3/22 18:17
 * @description: TODO
 */
public class test02 {
    public static void main(String[] args) {
        long fileSize = 108745;
        long threadNum = 10;
        long chunkSize = 200;

        long batchSize = fileSize / (threadNum * chunkSize) + 1;
        long remainSize = fileSize % chunkSize;
        System.out.println(remainSize);
        long start = 0L, end = 0L, mid = 0L;

        for (long i = 0; i < batchSize; i++) {

            mid = i * threadNum * chunkSize;
            System.out.println(mid);
            for (long l = 0; l < threadNum; l++) {
                start = Math.min(l * chunkSize + mid,fileSize);
                end = Math.min((l + 1) * chunkSize - 1 + mid,fileSize);
                if (start == fileSize) break; //终止循环
                System.out.println(start + "==" + end);
            }
        }
    }
}
