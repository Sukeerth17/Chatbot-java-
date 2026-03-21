package in.technobuild.chatbot.sse;

import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Slf4j
@Component
@RequiredArgsConstructor
public class SseEventPublisher {

    private final SseEmitterRegistry sseEmitterRegistry;

    public void sendToken(String messageId, String token) {
        sseEmitterRegistry.get(messageId).ifPresentOrElse(emitter -> {
            try {
                emitter.send(SseEmitter.event().data(token));
            } catch (IOException | IllegalStateException ex) {
                sseEmitterRegistry.remove(messageId);
                log.warn("Failed to send SSE token for messageId={}", messageId, ex);
            }
        }, () -> log.warn("No SSE emitter registered for messageId={}", messageId));
    }

    public void sendComplete(String messageId) {
        sseEmitterRegistry.get(messageId).ifPresent(emitter -> {
            try {
                emitter.send(SseEmitter.event().name("complete").data("[DONE]"));
                emitter.complete();
            } catch (IOException | IllegalStateException ex) {
                log.warn("Failed to complete SSE stream for messageId={}", messageId, ex);
            } finally {
                sseEmitterRegistry.remove(messageId);
            }
        });
    }

    public void sendError(String messageId, String errorMessage) {
        sseEmitterRegistry.get(messageId).ifPresent(emitter -> {
            try {
                emitter.send(SseEmitter.event().name("error").data(errorMessage));
                emitter.complete();
            } catch (IOException | IllegalStateException ex) {
                log.warn("Failed to send SSE error for messageId={}", messageId, ex);
            } finally {
                sseEmitterRegistry.remove(messageId);
            }
        });
    }
}
