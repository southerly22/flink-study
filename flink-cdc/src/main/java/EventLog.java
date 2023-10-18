import lombok.*;

import java.util.Map;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
public class EventLog {
    private long guid;
    private String SessionId;
    private String eventId;
    private long timeStamp;
    private Map<String,String> eventInfo;
}
