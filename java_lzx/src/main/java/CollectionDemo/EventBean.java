package CollectionDemo;


import lombok.*;

/**
 * @author lzx
 * @date 2023/5/4 15:12
 * @description: TODO
 */
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Getter
@Setter
@Data
public class EventBean {
    private long guid;
    private String eventId;
    private long timeStamp;
    private String pageId;
    private int actTimelong;  // 行为时长
}
