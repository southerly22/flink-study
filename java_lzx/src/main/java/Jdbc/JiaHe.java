package Jdbc;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.sql.Date;

/**
 * @author lzx
 * @date 2023/09/16 18:27
 **/
@Data
@AllArgsConstructor
@NoArgsConstructor
public class JiaHe {
    String name;
    String hobby;
    Date eventTime;
    //Boolean b;
}
