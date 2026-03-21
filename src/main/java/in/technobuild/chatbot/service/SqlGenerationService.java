package in.technobuild.chatbot.service;

import in.technobuild.chatbot.client.PythonAiClient;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class SqlGenerationService {

    private static final Duration SQL_TIMEOUT = Duration.ofSeconds(60);

    private final PythonAiClient pythonAiClient;

    public String generateSql(String userQuestion, String sessionUuid, boolean isFirstRequest) {
        PythonSqlResponse response = callPythonGetResponse(userQuestion, sessionUuid, isFirstRequest);
        return response != null ? response.uuid() : null;
    }

    public PythonSqlResponse callPythonGetResponse(String prompt, String uuid, boolean firstRequest) {
        try {
            PythonAiClient.PythonSqlResponse response = pythonAiClient.callGetResponse(prompt, uuid, firstRequest, SQL_TIMEOUT);
            return new PythonSqlResponse(response.response(), response.uuid());
        } catch (Exception ex) {
            log.error("Python /getResponse call failed", ex);
            return null;
        }
    }

    public record PythonSqlResponse(List<Map<String, Object>> response, String uuid) {
    }
}
