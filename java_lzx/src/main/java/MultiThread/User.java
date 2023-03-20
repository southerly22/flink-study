package MultiThread;

import java.sql.Timestamp;

/**
 * user
 *
 * @author lzx
 * @date 2023/03/20 22:49
 **/
public class User {
    private int id;
    private String name;
    private Timestamp createdTime;
    private Timestamp updatedTime;

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Timestamp getCreatedTime() {
        return createdTime;
    }

    public Timestamp getUpdatedTime() {
        return updatedTime;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setCreatedTime(Timestamp createdTime) {
        this.createdTime = createdTime;
    }

    public void setUpdatedTime(Timestamp updatedTime) {
        this.updatedTime = updatedTime;
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", createdTime=" + createdTime +
                ", updatedTime=" + updatedTime +
                '}';
    }
}
