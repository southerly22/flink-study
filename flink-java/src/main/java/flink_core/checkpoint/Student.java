package flink_core.checkpoint;

import lombok.*;

import java.sql.Timestamp;

/**
 * @author lzx
 * @date 2023/05/15 16:48
 **/

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class Student {
    Integer id;
    String name;
    Boolean sex;
    Integer age;
    Integer score;
    Timestamp createTime;
    Timestamp updateTime;
}
