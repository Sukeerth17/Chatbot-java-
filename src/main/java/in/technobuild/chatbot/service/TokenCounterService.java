package in.technobuild.chatbot.service;

import com.knuddels.jtokkit.Encodings;
import com.knuddels.jtokkit.api.Encoding;
import com.knuddels.jtokkit.api.EncodingRegistry;
import com.knuddels.jtokkit.api.EncodingType;
import in.technobuild.chatbot.entity.Message;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class TokenCounterService {

    private final EncodingRegistry registry = Encodings.newDefaultEncodingRegistry();
    private final Encoding encoding = registry.getEncoding(EncodingType.CL100K_BASE);

    public int countTokens(String text) {
        if (text == null || text.isBlank()) {
            return 0;
        }
        return encoding.countTokens(text);
    }

    public int estimateTokens(String text) {
        if (text == null || text.isBlank()) {
            return 0;
        }
        int wordCount = text.trim().split("\\s+").length;
        return (int) (wordCount * 1.3);
    }

    public List<Message> trimToTokenLimit(List<Message> messages, int maxTokens) {
        if (messages == null || messages.isEmpty()) {
            return List.of();
        }

        List<Message> ordered = new ArrayList<>(messages);
        ordered.sort(Comparator.comparing(Message::getCreatedAt, Comparator.nullsLast(Comparator.naturalOrder())));

        while (!ordered.isEmpty() && countConversationTokens(ordered) > maxTokens) {
            ordered.remove(0);
        }
        return ordered;
    }

    public int countConversationTokens(List<Message> messages) {
        if (messages == null || messages.isEmpty()) {
            return 0;
        }

        return messages.stream()
                .map(Message::getContent)
                .mapToInt(this::countTokens)
                .sum();
    }
}
