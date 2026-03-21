package in.technobuild.chatbot.kafka.producer;

import in.technobuild.chatbot.kafka.model.IngestionEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class IngestionProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${kafka.topics.doc-ingestion}")
    private String topic;

    public void publish(IngestionEvent event) {
        String key = event.getJobId();
        kafkaTemplate.send(topic, key, event).whenComplete((result, ex) -> {
            if (ex == null) {
                log.info("Published {} to topic {}", event.getClass().getSimpleName(), topic);
                return;
            }
            log.error("Failed to publish to topic {}", topic, ex);
        });
    }
}
