package entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.sql.Timestamp;

@Data
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class CdcDemo {
    private Integer id;
    private String userName;
    private Timestamp ts;
    private String materialInfo;
}
