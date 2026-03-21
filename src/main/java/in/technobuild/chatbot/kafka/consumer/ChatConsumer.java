package in.technobuild.chatbot.kafka.consumer;

import in.technobuild.chatbot.kafka.model.ChatRequestEvent;
import in.technobuild.chatbot.service.ChatOrchestrationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatConsumer {

    private final ChatOrchestrationService chatOrchestrationService;

    @KafkaListener(topics = "${kafka.topics.chat-requests}", groupId = "${spring.kafka.consumer.group-id}")
    public void consume(ChatRequestEvent event, @Header(KafkaHeaders.RECEIVED_TOPIC) String topic) {
        try {
            log.info("Received event from topic {}: {}", topic, event);
            chatOrchestrationService.processChat(event);
        } catch (Exception ex) {
            log.error("Failed to process chat event for messageId={}", event.getMessageId(), ex);
        }
    }
}
