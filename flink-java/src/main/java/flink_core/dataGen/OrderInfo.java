package flink_core.dataGen;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author lzx
 * @date 2023/6/13 15:24
 * @description: TODO
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderInfo {

    Integer id;
    Long user_id;
    Double total_amount;
    Long create_time;

}
