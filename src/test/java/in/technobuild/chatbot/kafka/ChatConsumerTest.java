package in.technobuild.chatbot.kafka;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;

import in.technobuild.chatbot.kafka.model.ChatRequestEvent;
import in.technobuild.chatbot.service.ChatOrchestrationService;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.annotation.DirtiesContext;

@SpringBootTest(properties = {
        "spring.kafka.bootstrap-servers=${spring.embedded.kafka.brokers}",
        "spring.kafka.consumer.auto-offset-reset=earliest"
})
@EmbeddedKafka(partitions = 1, topics = {"chat.requests"})
@DirtiesContext
class ChatConsumerTest {

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Autowired
    private EmbeddedKafkaBroker embeddedKafkaBroker;

    @MockBean
    private ChatOrchestrationService chatOrchestrationService;

    private CountDownLatch latch;

    @BeforeEach
    void setUp() {
        latch = new CountDownLatch(1);
    }

    @Test
    void consume_validChatRequestEvent_callsOrchestrationService() throws Exception {
        doAnswer(invocation -> {
            latch.countDown();
            return null;
        }).when(chatOrchestrationService).processChat(any(ChatRequestEvent.class));

        ChatRequestEvent event = ChatRequestEvent.builder()
                .messageId(UUID.randomUUID().toString())
                .userId(1L)
                .sessionId(UUID.randomUUID().toString())
                .userMessage("hello")
                .conversationId(10L)
                .userRole("USER")
                .userName("tester")
                .currentDateTime("2026-03-21T00:00:00")
                .timestamp(System.currentTimeMillis())
                .build();

        kafkaTemplate.send("chat.requests", String.valueOf(event.getUserId()), event);

        assertTrue(latch.await(10, TimeUnit.SECONDS));
        verify(chatOrchestrationService, timeout(10000)).processChat(any(ChatRequestEvent.class));
    }

    @Test
    void consume_withMalformedEvent_doesNotCrashConsumer() throws Exception {
        Map<String, Object> props = KafkaTestUtils.producerProps(embeddedKafkaBroker);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);

        KafkaTemplate<String, String> stringKafkaTemplate =
                new KafkaTemplate<>(new DefaultKafkaProducerFactory<>(props));

        stringKafkaTemplate.send("chat.requests", "bad-key", "not-a-valid-chat-request-event-json");
        stringKafkaTemplate.flush();

        assertFalse(latch.await(10, TimeUnit.SECONDS));
        verify(chatOrchestrationService, never()).processChat(any(ChatRequestEvent.class));
    }
}
