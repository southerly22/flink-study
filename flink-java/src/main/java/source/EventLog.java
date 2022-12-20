package source;

import lombok.*;

import java.util.Map;
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class EventLog {
    private long guid;
    private String SessionId;
    private String eventId;
    private long timeStampt;
    private Map<String,String> eventInfo;
}
