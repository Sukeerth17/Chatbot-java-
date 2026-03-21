package in.technobuild.chatbot.service;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class HallucinationGuardService {

    public boolean isGrounded(String answer, List<String> retrievedChunks) {
        if (answer == null || answer.isBlank() || retrievedChunks == null || retrievedChunks.isEmpty()) {
            return false;
        }

        Set<String> answerWords = tokenize(answer);
        Set<String> chunkWords = tokenize(String.join(" ", retrievedChunks));

        if (answerWords.isEmpty() || chunkWords.isEmpty()) {
            return false;
        }

        long overlap = answerWords.stream().filter(chunkWords::contains).count();
        double overlapRatio = (double) overlap / (double) answerWords.size();
        log.info("Hallucination check overlapRatio={}", overlapRatio);
        return overlapRatio >= 0.35;
    }

    private Set<String> tokenize(String text) {
        return Arrays.stream(text.toLowerCase(Locale.ROOT).split("[^a-z0-9]+"))
                .filter(word -> word.length() > 2)
                .collect(Collectors.toCollection(HashSet::new));
    }
}
