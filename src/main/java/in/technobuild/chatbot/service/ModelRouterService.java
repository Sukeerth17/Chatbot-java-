package in.technobuild.chatbot.service;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ModelRouterService {

    private final OllamaService ollamaService;

    private static final String CLASSIFICATION_PROMPT_TEMPLATE = """
            Classify the following user message into exactly one of these three categories:
            
            1. SQL: The user is asking for data retrieval, counts, lists, or metrics from the CRM database (e.g., leads, follow-ups, pipeline, activities, users).
            2. KNOWLEDGE: The user is asking for definitions of business terms, process explanations, or how the application works (e.g., "what is the Proposal stage", "how to", "define").
            3. CHAT: General greetings, casual conversation, or anything else.
            
            Respond with ONLY the category name (SQL, KNOWLEDGE, or CHAT). Do not include any other text, punctuation, or explanation.
            
            Message: "{USER_MESSAGE}"
            Category:""";

    public RequestType routeRequest(String userMessage) {
        if (userMessage == null || userMessage.isBlank()) {
            return RequestType.CHAT;
        }

        String prompt = CLASSIFICATION_PROMPT_TEMPLATE.replace("{USER_MESSAGE}", userMessage);
        RequestType type = RequestType.CHAT;
        
        try {
            // Apply a strict 6-second timeout for the classification using the fast 1b model
            String response = CompletableFuture.supplyAsync(() -> ollamaService.generateChatResponse(prompt, "llama3.2:1b"))
                    .get(6, TimeUnit.SECONDS);
            type = parseCategory(response);
        } catch (TimeoutException e) {
            log.warn("LLM intent classification timed out, falling back to CHAT");
        } catch (Exception e) {
            log.error("Failed to classify intent using LLM, falling back to CHAT", e);
        }
        
        String preview = userMessage.length() <= 50 ? userMessage : userMessage.substring(0, 50);
        log.info("Routed '{}' to {}", preview.replace('\n', ' '), type);
        
        return type;
    }

    public boolean isSqlIntent(String userMessage) {
        return routeRequest(userMessage) == RequestType.SQL;
    }

    private RequestType parseCategory(String response) {
        if (response == null || response.isBlank()) {
            return RequestType.CHAT;
        }
        
        String normalized = response.toUpperCase().trim();
        
        if (normalized.contains("KNOWLEDGE")) {
            return RequestType.KNOWLEDGE;
        } else if (normalized.contains("SQL")) {
            return RequestType.SQL;
        } else {
            return RequestType.CHAT;
        }
    }

    public enum RequestType {
        CHAT,
        KNOWLEDGE,
        SQL
    }
}
