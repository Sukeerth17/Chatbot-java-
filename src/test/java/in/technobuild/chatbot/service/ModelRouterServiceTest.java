package in.technobuild.chatbot.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ModelRouterServiceTest {

    private final ModelRouterService modelRouterService = new ModelRouterService();

    @Test
    void routeRequest_showKeyword_returnsSql() {
        assertEquals(ModelRouterService.RequestType.SQL, modelRouterService.routeRequest("show me users"));
    }

    @Test
    void routeRequest_listKeyword_returnsSql() {
        assertEquals(ModelRouterService.RequestType.SQL, modelRouterService.routeRequest("list all orders"));
    }

    @Test
    void routeRequest_howManyKeyword_returnsSql() {
        assertEquals(ModelRouterService.RequestType.SQL, modelRouterService.routeRequest("how many customers"));
    }

    @Test
    void routeRequest_generalQuestion_returnsChat() {
        assertEquals(ModelRouterService.RequestType.CHAT, modelRouterService.routeRequest("Explain refunds policy"));
    }

    @Test
    void routeRequest_greetingMessage_returnsChat() {
        assertEquals(ModelRouterService.RequestType.CHAT, modelRouterService.routeRequest("hello there"));
    }

    @Test
    void routeRequest_totalKeyword_returnsSql() {
        assertEquals(ModelRouterService.RequestType.SQL, modelRouterService.routeRequest("total revenue this month"));
    }

    @Test
    void routeRequest_countKeyword_returnsSql() {
        assertEquals(ModelRouterService.RequestType.SQL, modelRouterService.routeRequest("count products"));
    }

    @Test
    void routeRequest_emptyMessage_returnsChat() {
        assertEquals(ModelRouterService.RequestType.CHAT, modelRouterService.routeRequest(""));
    }
}
