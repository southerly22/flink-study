package huorong.entity;

import huorong.SampleTaskMappingInfo;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * @author lzx
 * @date 2023/6/5 17:07
 * @description: TODO
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true) //开启链式调用
public class SampleUerdefinedScan extends SampleTaskMappingInfo {
    private String rk;
    private String src_id;
    private String src_name;
    private String sha1;
    private Long add_timestamp;
    private Long task_id;
    private String task_type;
    private Long task_CreateTime;
}
