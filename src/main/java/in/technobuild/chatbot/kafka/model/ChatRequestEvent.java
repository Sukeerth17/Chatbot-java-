package in.technobuild.chatbot.kafka.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatRequestEvent {

    private String messageId;
    private Long userId;
    private String sessionId;
    private String userMessage;
    private Long conversationId;
    private String userRole;
    private String userName;
    private String currentDateTime;
    private long timestamp;
}
