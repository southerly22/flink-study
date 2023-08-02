package Test;

/**
 * @author lzx
 * @date 2023/6/15 10:48
 * @description: TODO
 */
public abstract class AbsNetWorkInput implements StreamTaskInput {
    int index = 10;
    @Override
    public int getInputIndex() {
        return index;
    }

    @Override
    public String emitNet() {
        System.out.println("AbsNetWorkInput -> emitNet");
        return "emitNet";
    }

    public String emitNext(){
        System.out.println("AbsNetWorkInput -> emitNext");
        return "emitNext";
    }
}
