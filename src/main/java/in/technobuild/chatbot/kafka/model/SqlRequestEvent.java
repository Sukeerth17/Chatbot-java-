package in.technobuild.chatbot.kafka.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SqlRequestEvent {

    private String messageId;
    private Long userId;
    private String sessionId;
    private String originalQuestion;
    private Long conversationId;
    private long timestamp;
}
