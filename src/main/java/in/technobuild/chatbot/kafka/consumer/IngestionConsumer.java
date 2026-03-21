package in.technobuild.chatbot.kafka.consumer;

import in.technobuild.chatbot.kafka.model.IngestionEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class IngestionConsumer {

    @KafkaListener(topics = "${kafka.topics.doc-ingestion}", groupId = "${spring.kafka.consumer.group-id}")
    public void consume(IngestionEvent event, @Header(KafkaHeaders.RECEIVED_TOPIC) String topic) {
        // TODO: Stage 7 — implement real logic here
        log.info("Received event from topic {}: {}", topic, event);
    }
}
