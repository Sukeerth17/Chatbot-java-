package in.technobuild.chatbot.sse;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Slf4j
@Component
public class SseEmitterRegistry {

    private final ConcurrentHashMap<String, SseEmitter> emitters = new ConcurrentHashMap<>();

    public void register(String messageId, SseEmitter emitter) {
        emitter.onCompletion(() -> remove(messageId));
        emitter.onTimeout(() -> {
            log.warn("SseEmitter timeout for messageId={}", messageId);
            remove(messageId);
        });
        emitter.onError(ex -> {
            log.error("SseEmitter error for messageId={}", messageId, ex);
            remove(messageId);
        });

        emitters.put(messageId, emitter);
    }

    public Optional<SseEmitter> get(String messageId) {
        return Optional.ofNullable(emitters.get(messageId));
    }

    public void remove(String messageId) {
        emitters.remove(messageId);
        log.debug("SseEmitter removed for messageId {}", messageId);
    }

    public int size() {
        return emitters.size();
    }

    public boolean isRegistered(String messageId) {
        return emitters.containsKey(messageId);
    }
}
