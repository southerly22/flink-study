package IODemos;

/**
 * @author lzx
 * @date 2023/3/22 18:17
 * @description: TODO
 */
public class test02 {
    public static void main(String[] args) {
        long fileSize = 107745;
        long threadNum = 10;
        long chunkSize = 200;

        long batchSize = fileSize / (threadNum * chunkSize) + 1;
        long remainSize = fileSize % chunkSize;
        System.out.println(remainSize);
        long start = 0L, end = 0L, mid = 0L;

        for (long i = 0; i < batchSize; i++) {
            mid = i * threadNum * chunkSize;
            for (long l = 0; l < threadNum; l++) {
                start = l * chunkSize + mid;
                end = (l + 1) * chunkSize - 1 + mid;
                System.out.println(start+"=="+end);
                System.out.println("------------------");
            }
            if (i == batchSize -1) {
                for (long k = 0; k < threadNum; k++) {
                    start = k * chunkSize + mid;
                    end += (k + 1) * chunkSize - 1 + mid;
                    if (k == threadNum-1){
                        end += remainSize;
                    }
                    System.out.println(start+"=="+end);
                }
            }
        }

    }
}
