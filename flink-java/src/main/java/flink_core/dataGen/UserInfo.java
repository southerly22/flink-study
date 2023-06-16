package flink_core.dataGen;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author lzx
 * @date 2023/6/13 15:29
 * @description: TODO
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserInfo {

    private Integer id;
    private Long user_id;
    private Integer age;
    private Integer sex;
}
