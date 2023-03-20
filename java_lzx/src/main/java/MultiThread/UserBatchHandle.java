package MultiThread;

import org.apache.commons.lang3.time.StopWatch;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.List;

/**
 * @author lzx
 * @date 2023/03/20 22:56
 **/
public class UserBatchHandle {

    public static int batchSave(List userList,String threadName){
        String insertSql ="INSERT INTO user(id,name,createdTime,updatedTime) VALUES(?,?,sysdate(),sysdate())";
        Connection conn = null;
        PreparedStatement statement = null;
        User user;
        int[] count = new int[0];

        try {
            conn = DataSourceUtil.getConn();
            statement = conn.prepareStatement(insertSql);
            StopWatch stopWatch = new StopWatch();
            stopWatch.start();
            if (null != userList && userList.size() > 0){
                for (int i = 0; i < userList.size(); i++) {
                    user = (User) userList.get(i);
                    statement.setInt(1,user.getId());
                    statement.setString(2,user.getName());
                    statement.addBatch();
                }

                count = statement.executeBatch();
                System.out.println(count.length);
                stopWatch.stop();
                System.out.println(" threadName为"+threadName+", sync data to db, it  has spent " +stopWatch.getTime()+"  ms");
            }
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            DataSourceUtil.close(conn,statement);
        }
        //获取到最新的行数
        return count.length;
    }
}
