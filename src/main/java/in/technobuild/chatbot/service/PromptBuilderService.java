package in.technobuild.chatbot.service;

import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class PromptBuilderService {

    @Value("${chatbot.system-prompt:You are TechnoBuild AI, an expert CRM assistant for the sales team. You must answer questions based STRICTLY on the [DOCUMENTS] provided. Do not invent or hallucinate answers. If the [DOCUMENTS] do not contain the answer, politely state that you do not have that information. Keep answers concise. User Name: {userName}, Role: {userRole}, Date: {currentDateTime}})")
    private String configuredSystemPrompt;

    public String buildChatPrompt(String systemPrompt,
                                  String userName,
                                  String userRole,
                                  String currentDateTime,
                                  List<String> retrievedChunks,
                                  String conversationHistory,
                                  String userMessage) {
        String basePrompt = (systemPrompt == null || systemPrompt.isBlank()) ? configuredSystemPrompt : systemPrompt;
        String resolvedPrompt = basePrompt
                .replace("{userName}", safe(userName))
                .replace("{userRole}", safe(userRole))
                .replace("{currentDateTime}", safe(currentDateTime));

        StringBuilder prompt = new StringBuilder(resolvedPrompt).append("\n\n");

        if (retrievedChunks != null && !retrievedChunks.isEmpty()) {
            prompt.append("[DOCUMENTS]\n");
            prompt.append(String.join("\n---\n", retrievedChunks));
            prompt.append("\n[END DOCUMENTS]\n\n");
        }

        if (conversationHistory != null && !conversationHistory.isBlank()) {
            prompt.append("Conversation history:\n").append(conversationHistory).append("\n");
        }

        prompt.append("User: ").append(userMessage).append("\n\nAssistant:");
        return prompt.toString();
    }

    public String buildSqlFormattingPrompt(String sqlResult, String originalQuestion) {
        return "You are TechnoBuild AI, an expert data analyst. Convert the following raw database rows into a clear, professional plain English summary for a sales representative. "
                + "Keep the answer under 100 words. Do not mention 'SQL', 'database', or 'rows'. Just provide the business answer.\n"
                + "Question: " + originalQuestion + "\n"
                + "SQL Rows: " + sqlResult + "\n"
                + "Answer:";
    }

    public int estimatePromptTokens(String prompt) {
        if (prompt == null || prompt.isBlank()) {
            return 0;
        }
        int wordCount = prompt.trim().split("\\s+").length;
        return (int) (wordCount * 1.3);
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }
}
