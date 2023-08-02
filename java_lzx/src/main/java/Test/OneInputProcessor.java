package Test;

/**
 * @author lzx
 * @date 2023/6/15 11:03
 * @description: TODO
 */
public class OneInputProcessor {
    private StreamTaskInput input;

    public OneInputProcessor(StreamTaskInput streamTaskInput){
        this.input = streamTaskInput;
    }
    public void callEmitNext(){
        input.emitNet();
    }

    public static void main(String[] args) {
        NetWorkInput netWorkInput = new NetWorkInput();
        OneInputProcessor oneInputProcessor = new OneInputProcessor(netWorkInput);
        oneInputProcessor.callEmitNext();
    }
}
