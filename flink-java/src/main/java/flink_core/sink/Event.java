package flink_core.sink;

import lombok.*;

@Data
@Setter
@Getter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class Event {
    private String name;
    private String catalogue;
    private long score;
}
