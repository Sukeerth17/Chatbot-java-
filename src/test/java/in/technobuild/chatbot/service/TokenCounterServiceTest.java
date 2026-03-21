package in.technobuild.chatbot.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import in.technobuild.chatbot.entity.Message;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TokenCounterServiceTest {

    private final TokenCounterService tokenCounterService = new TokenCounterService();

    @Test
    void countTokens_shortText_returnsPositiveInt() {
        assertTrue(tokenCounterService.countTokens("hello world") > 0);
    }

    @Test
    void countTokens_emptyString_returnsZero() {
        assertEquals(0, tokenCounterService.countTokens(""));
    }

    @Test
    void estimateTokens_tenWords_returnsApprox13() {
        assertEquals(13, tokenCounterService.estimateTokens("one two three four five six seven eight nine ten"));
    }

    @Test
    void trimToTokenLimit_listOver4000tokens_returnsTrimmedList() {
        List<Message> messages = new ArrayList<>();
        String content = "word ".repeat(1200);
        for (int i = 0; i < 6; i++) {
            messages.add(Message.builder()
                    .content(content)
                    .createdAt(LocalDateTime.now().minusMinutes(6 - i))
                    .build());
        }

        List<Message> trimmed = tokenCounterService.trimToTokenLimit(messages, 4000);
        assertTrue(trimmed.size() < messages.size());
    }

    @Test
    void trimToTokenLimit_listUnderLimit_returnsUnchanged() {
        List<Message> messages = List.of(
                Message.builder().content("hello").createdAt(LocalDateTime.now()).build(),
                Message.builder().content("world").createdAt(LocalDateTime.now().plusSeconds(1)).build()
        );

        List<Message> trimmed = tokenCounterService.trimToTokenLimit(messages, 4000);
        assertEquals(2, trimmed.size());
    }
}
