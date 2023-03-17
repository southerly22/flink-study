package StaticDemo;

/**
 * @author lzx
 * @date 2023/3/10 13:16
 * @description: TODO
 */
public class TestUserBlocak{
    public static void main(String[] args) {
        User user1 = new User();
        System.out.println(user1.getInfo());

        User user2 = new User("小明","666");
        System.out.println(user2.getInfo());
    }
}
class User {
    private String userName;
    private String password;
    private long registerTime;
    {
        System.out.println("新用户注册");
        registerTime = System.currentTimeMillis();
    }

    public User() {
        this.userName = registerTime+"";
        this.password = "123456";
    }

    public User(String userName, String password) {
        this.userName = userName;
        this.password = password;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getUserName() {
        return userName;
    }

    public String getPassword() {
        return password;
    }

    public long getRegisterTime() {
        return registerTime;
    }

    public String getInfo() {
        return "User{" +
                "userName='" + userName + '\'' +
                ", password='" + password + '\'' +
                ", registerTime=" + registerTime +
                '}';
    }
}
