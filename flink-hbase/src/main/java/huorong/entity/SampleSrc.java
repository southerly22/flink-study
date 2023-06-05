package huorong.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * @author lzx
 * @date 2023/06/05 14:43
 **/

@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true) //开启链式调用
public class SampleSrc {
    private String rk;
    private String src_id;
    private String src_name;
    private String sha1;
    private Long add_timestamp;
}
