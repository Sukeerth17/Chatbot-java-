package in.technobuild.chatbot.service;

import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class ModelRouterService {

    private static final List<String> SQL_KEYWORDS = List.of(
            "show", "list", "how many", "total", "count", "find all", "give me", "what is the",
            "fetch", "retrieve", "display all", "top 5", "top 10", "summarise the data",
            "average", "maximum", "minimum", "sum of"
    );

    public RequestType routeRequest(String userMessage) {
        String normalized = userMessage == null ? "" : userMessage.toLowerCase();

        RequestType type = SQL_KEYWORDS.stream().anyMatch(normalized::contains)
                ? RequestType.SQL
                : RequestType.CHAT;

        String preview = normalized.length() <= 50 ? normalized : normalized.substring(0, 50);
        log.info("Routed '{}' to {}", preview, type);
        return type;
    }

    public boolean isSqlIntent(String userMessage) {
        return routeRequest(userMessage) == RequestType.SQL;
    }

    public enum RequestType {
        CHAT,
        SQL
    }
}
