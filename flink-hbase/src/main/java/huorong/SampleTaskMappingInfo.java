package huorong;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * @author lzx
 * @date 2023/5/26 16:01
 * @description: TODO
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true) //开启链式调用
public class SampleTaskMappingInfo {
    private Long task_id;
    private String task_type;
    private String sha1;
    private Long task_CreateTime;
}
