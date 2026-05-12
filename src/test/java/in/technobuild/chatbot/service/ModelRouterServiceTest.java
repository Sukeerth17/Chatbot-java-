package in.technobuild.chatbot.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;

class ModelRouterServiceTest {

    private FakeOllamaService fakeOllamaService;
    private ModelRouterService modelRouterService;

    static class FakeOllamaService extends OllamaService {
        private String response;
        private boolean throwException;

        public FakeOllamaService() {
            super(null, null);
        }

        public void setResponse(String response) {
            this.response = response;
            this.throwException = false;
        }

        public void setThrowException(boolean throwException) {
            this.throwException = throwException;
        }

        @Override
        public String generateChatResponse(String prompt, String modelOverride) {
            if (throwException) {
                throw new RuntimeException("Ollama down");
            }
            return response;
        }
    }

    @BeforeEach
    void setUp() {
        fakeOllamaService = new FakeOllamaService();
        modelRouterService = new ModelRouterService(fakeOllamaService);
    }

    @Test
    void routeRequest_sqlResponse_returnsSql() {
        fakeOllamaService.setResponse("SQL");
        assertEquals(ModelRouterService.RequestType.SQL, modelRouterService.routeRequest("show me users"));
    }

    @Test
    void routeRequest_knowledgeResponse_returnsKnowledge() {
        fakeOllamaService.setResponse("KNOWLEDGE");
        assertEquals(ModelRouterService.RequestType.KNOWLEDGE, modelRouterService.routeRequest("What is the proposal stage?"));
    }

    @Test
    void routeRequest_chatResponse_returnsChat() {
        fakeOllamaService.setResponse("CHAT");
        assertEquals(ModelRouterService.RequestType.CHAT, modelRouterService.routeRequest("hello there"));
    }

    @Test
    void routeRequest_emptyMessage_returnsChat() {
        assertEquals(ModelRouterService.RequestType.CHAT, modelRouterService.routeRequest(""));
    }

    @Test
    void routeRequest_exceptionFromOllama_returnsChat() {
        fakeOllamaService.setThrowException(true);
        assertEquals(ModelRouterService.RequestType.CHAT, modelRouterService.routeRequest("some question"));
    }
}
