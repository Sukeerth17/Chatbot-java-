package in.technobuild.chatbot.kafka.consumer;

import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class ModelHealthConsumer {

    @KafkaListener(topics = "${kafka.topics.model-health}", groupId = "${spring.kafka.consumer.group-id}")
    public void consume(String event, @Header(KafkaHeaders.RECEIVED_TOPIC) String topic) {
        // TODO: Stage 6 — implement real logic here
        log.info("Received event from topic {}: {}", topic, event);
    }
}
