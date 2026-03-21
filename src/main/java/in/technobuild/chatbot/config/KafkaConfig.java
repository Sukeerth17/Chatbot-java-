package in.technobuild.chatbot.config;

import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaAdmin;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;

@Slf4j
@Configuration
@EnableKafka
public class KafkaConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Value("${kafka.topics.chat-requests}")
    private String chatRequestsTopicName;

    @Value("${kafka.topics.sql-requests}")
    private String sqlRequestsTopicName;

    @Value("${kafka.topics.chat-responses}")
    private String chatResponsesTopicName;

    @Value("${kafka.topics.doc-ingestion}")
    private String docIngestionTopicName;

    @Value("${kafka.topics.model-health}")
    private String modelHealthTopicName;

    @Bean
    public KafkaAdmin kafkaAdmin() {
        Map<String, Object> configs = new HashMap<>();
        configs.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        return new KafkaAdmin(configs);
    }

    @Bean
    public ProducerFactory<String, Object> producerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        return new DefaultKafkaProducerFactory<>(props);
    }

    @Bean
    public KafkaTemplate<String, Object> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }

    @Bean
    public ConsumerFactory<String, Object> consumerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "chatbot-consumer-group");
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        props.put(JsonDeserializer.TRUSTED_PACKAGES, "in.technobuild.chatbot.kafka.model");
        return new DefaultKafkaConsumerFactory<>(
                props,
                new StringDeserializer(),
                new JsonDeserializer<>().trustedPackages("in.technobuild.chatbot.kafka.model")
        );
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, Object> kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, Object> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory());
        return factory;
    }

    @Bean
    public NewTopic chatRequestsTopic() {
        log.info("Created topic {}", chatRequestsTopicName);
        return TopicBuilder.name(chatRequestsTopicName).partitions(3).replicas(1).build();
    }

    @Bean
    public NewTopic sqlRequestsTopic() {
        log.info("Created topic {}", sqlRequestsTopicName);
        return TopicBuilder.name(sqlRequestsTopicName).partitions(3).replicas(1).build();
    }

    @Bean
    public NewTopic chatResponsesTopic() {
        log.info("Created topic {}", chatResponsesTopicName);
        return TopicBuilder.name(chatResponsesTopicName).partitions(3).replicas(1).build();
    }

    @Bean
    public NewTopic docIngestionTopic() {
        log.info("Created topic {}", docIngestionTopicName);
        return TopicBuilder.name(docIngestionTopicName).partitions(1).replicas(1).build();
    }

    @Bean
    public NewTopic modelHealthTopic() {
        log.info("Created topic {}", modelHealthTopicName);
        return TopicBuilder.name(modelHealthTopicName).partitions(1).replicas(1).build();
    }
}
