package in.technobuild.chatbot.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class CostTrackerService {

    public boolean hasBudget(Long userId) {
        log.info("Cost budget check for userId={}", userId);
        return true;
    }
}
