package in.technobuild.chatbot.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class RateLimiterService {

    public boolean isAllowed(Long userId) {
        log.info("RateLimiter check for userId={}", userId);
        return true;
    }
}
