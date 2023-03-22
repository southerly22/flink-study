package IODemos;

/**
 * tst
 *
 * @author lzx
 * @date 2023/03/21 21:43
 **/
public class Test01 {
    public static void main(String[] args) {

        System.out.println(Integer.MAX_VALUE);

        /**
         *
         * 0111 1111   1111 1111  1111 1111  1111 1111
         * 0010 0000  0000 0000 0000 0000 0000 0000
         * 0001 1111   1111 1111  1111 1111  1111 1111  1 << 29
         *
         *   0000 0000  0000 0000   0000 0000 0000 0110
         *  &
         *  0001 1111   1111 1111  1111 1111  1111 1111  1 << 29 -1
         *
         *  0000 0000  0000 0000   0000 0000 0000 0110
         *
         *
         *  0001 1111 11111111 11111111 11111111
         */

        System.out.println(~((1<<29) -1));
        System.out.println(6 & ~((1 << 29)-1));
        System.out.println(~5);
        System.out.println(0b11111111111111111111111111111111);
    }
}
