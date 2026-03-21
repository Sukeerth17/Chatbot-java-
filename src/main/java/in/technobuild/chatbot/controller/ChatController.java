package in.technobuild.chatbot.controller;

import in.technobuild.chatbot.dto.request.ChatRequestDto;
import in.technobuild.chatbot.entity.Conversation;
import in.technobuild.chatbot.kafka.model.ChatRequestEvent;
import in.technobuild.chatbot.kafka.model.SqlRequestEvent;
import in.technobuild.chatbot.kafka.producer.ChatRequestProducer;
import in.technobuild.chatbot.kafka.producer.SqlRequestProducer;
import in.technobuild.chatbot.security.UserPrincipal;
import in.technobuild.chatbot.service.ConversationService;
import in.technobuild.chatbot.service.CostTrackerService;
import in.technobuild.chatbot.service.ModelRouterService;
import in.technobuild.chatbot.service.PiiScrubberService;
import in.technobuild.chatbot.service.RateLimiterService;
import in.technobuild.chatbot.sse.SseEmitterRegistry;
import jakarta.validation.Valid;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Slf4j
@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatRequestProducer chatRequestProducer;
    private final SqlRequestProducer sqlRequestProducer;
    private final SseEmitterRegistry sseEmitterRegistry;
    private final ConversationService conversationService;
    private final PiiScrubberService piiScrubberService;
    private final ModelRouterService modelRouterService;

    @Lazy
    private final RateLimiterService rateLimiterService;

    @Lazy
    private final CostTrackerService costTrackerService;

    @PostMapping(produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter chat(@Valid @RequestBody ChatRequestDto request,
                           @AuthenticationPrincipal UserPrincipal user) {
        String messageId = UUID.randomUUID().toString();
        SseEmitter emitter = new SseEmitter(120_000L);
        sseEmitterRegistry.register(messageId, emitter);

        try {
            if (user == null || user.getUserId() == null) {
                throw new IllegalStateException("User not authenticated");
            }

            Long userId = Long.parseLong(user.getUserId());

            if (!rateLimiterService.isAllowed(userId)) {
                throw new IllegalStateException("Rate limit exceeded");
            }

            if (!costTrackerService.hasBudget(userId)) {
                throw new IllegalStateException("Daily token budget exceeded");
            }

            Conversation conversation = conversationService.getOrCreateConversation(userId, request.getSessionId());
            String scrubbedMessage = piiScrubberService.scrub(request.getMessage());

            ChatRequestEvent event = ChatRequestEvent.builder()
                    .messageId(messageId)
                    .userId(userId)
                    .sessionId(conversation.getSessionId())
                    .userMessage(scrubbedMessage)
                    .conversationId(conversation.getId())
                    .userRole(user.getRole())
                    .userName(user.getUsername())
                    .currentDateTime(LocalDateTime.now().toString())
                    .timestamp(System.currentTimeMillis())
                    .build();

            if (modelRouterService.isSqlIntent(scrubbedMessage)) {
                SqlRequestEvent sqlEvent = SqlRequestEvent.builder()
                        .messageId(messageId)
                        .userId(userId)
                        .sessionId(conversation.getSessionId())
                        .originalQuestion(scrubbedMessage)
                        .conversationId(conversation.getId())
                        .timestamp(System.currentTimeMillis())
                        .build();
                sqlRequestProducer.publish(sqlEvent);
                log.info("SQL request published for messageId={}", messageId);
            } else {
                chatRequestProducer.publish(event);
                log.info("Chat request published for messageId={}", messageId);
            }
            return emitter;
        } catch (Exception ex) {
            log.error("Failed to process /api/chat request", ex);
            try {
                emitter.send(SseEmitter.event().name("error").data("Something went wrong. Please try again."));
            } catch (IOException ioEx) {
                log.error("Failed to send SSE error event", ioEx);
            }
            emitter.completeWithError(ex);
            sseEmitterRegistry.remove(messageId);
            return emitter;
        }
    }
}
