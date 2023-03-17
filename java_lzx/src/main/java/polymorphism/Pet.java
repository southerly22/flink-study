package polymorphism;

/**
 * @author lzx
 * @date 2023/3/9 11:23
 * @description: TODO
 */
public class Pet {
    private String nickname;//昵称

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }
    public void eat(){
        System.out.println(nickname + " 吃东西");
    }
}
