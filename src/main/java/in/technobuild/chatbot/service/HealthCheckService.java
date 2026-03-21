package in.technobuild.chatbot.service;

import in.technobuild.chatbot.dto.response.HealthResponseDto;
import java.sql.Connection;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import javax.sql.DataSource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Slf4j
@Service
@RequiredArgsConstructor
public class HealthCheckService {

    private static final String UP = "UP";
    private static final String DOWN = "DOWN";

    @Qualifier("chatbotDataSource")
    private final DataSource chatbotDataSource;

    private final StringRedisTemplate stringRedisTemplate;

    @Qualifier("ollamaWebClient")
    private final WebClient ollamaWebClient;

    @Qualifier("pythonAiWebClient")
    private final WebClient pythonAiWebClient;

    @Value("${spring.kafka.bootstrap-servers}")
    private String kafkaBootstrapServers;

    public HealthResponseDto checkAll() {
        log.info("Running dependency health checks");

        String mysql = checkMysql();
        String redis = checkRedis();
        String kafka = checkKafka();
        String ollama = checkOllama();
        String python = checkPythonAi();

        return HealthResponseDto.builder()
                .mysql(mysql)
                .redis(redis)
                .kafka(kafka)
                .ollama(ollama)
                .python(python)
                .build();
    }

    private String checkMysql() {
        try (Connection connection = chatbotDataSource.getConnection()) {
            boolean valid = connection.isValid(2);
            return valid ? UP : DOWN;
        } catch (Exception ex) {
            log.error("MySQL health check failed", ex);
            return DOWN;
        }
    }

    private String checkRedis() {
        try {
            String pong = stringRedisTemplate.execute((RedisCallback<String>) this::pingRedis);
            return "PONG".equalsIgnoreCase(pong) ? UP : DOWN;
        } catch (Exception ex) {
            log.error("Redis health check failed", ex);
            return DOWN;
        }
    }

    private String pingRedis(RedisConnection connection) {
        return connection != null ? connection.ping() : null;
    }

    private String checkKafka() {
        Map<String, Object> config = Map.of(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaBootstrapServers);
        try (AdminClient adminClient = AdminClient.create(config)) {
            adminClient.describeCluster().clusterId().get(3, TimeUnit.SECONDS);
            return UP;
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            log.error("Kafka health check interrupted", ex);
            return DOWN;
        } catch (ExecutionException | TimeoutException ex) {
            log.error("Kafka health check failed", ex);
            return DOWN;
        }
    }

    private String checkOllama() {
        try {
            ollamaWebClient.get()
                    .uri("/api/tags")
                    .retrieve()
                    .toBodilessEntity()
                    .block(Duration.ofSeconds(5));
            return UP;
        } catch (Exception ex) {
            log.error("Ollama health check failed", ex);
            return DOWN;
        }
    }

    private String checkPythonAi() {
        try {
            pythonAiWebClient.get()
                    .uri("/health")
                    .retrieve()
                    .toBodilessEntity()
                    .block(Duration.ofSeconds(5));
            return UP;
        } catch (Exception ex) {
            log.error("Python AI service health check failed", ex);
            return DOWN;
        }
    }
}
