package Test;

/**
 * @author lzx
 * @date 2023/6/15 10:49
 * @description: TODO
 */
public class NetWorkInput extends AbsNetWorkInput{
    @Override
    public String prepareSnapshot() {
        System.out.println("NetWorkInput -> prepareSnapshot");
        return "prepareSnapshot";
    }

    public static void main(String[] args) {
        NetWorkInput netWorkInput = new NetWorkInput();
        // System.out.println("netWorkInput.getInputIndex() = " + netWorkInput.getInputIndex());
        System.out.println("netWorkInput.prepareSnapshot() = " + netWorkInput.prepareSnapshot());
    }

}
