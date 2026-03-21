package in.technobuild.chatbot.service;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class PiiScrubberService {

    private static final Pattern AADHAAR_PATTERN = Pattern.compile("\\b\\d{4}\\s\\d{4}\\s\\d{4}\\b");
    private static final Pattern PHONE_PATTERN = Pattern.compile("\\b[6-9]\\d{9}\\b");
    private static final Pattern EMAIL_PATTERN = Pattern.compile("\\b[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}\\b");
    private static final Pattern PAN_PATTERN = Pattern.compile("\\b[A-Z]{5}[0-9]{4}[A-Z]\\b");
    private static final Pattern CARD_PATTERN = Pattern.compile("\\b\\d{4}[\\s-]\\d{4}[\\s-]\\d{4}[\\s-]\\d{4}\\b");

    public String scrub(String text) {
        if (text == null || text.isBlank()) {
            return text;
        }

        int totalReplacements = 0;
        ReplacementResult aadhaar = replaceWithCount(text, AADHAAR_PATTERN, "[AADHAAR_REDACTED]");
        totalReplacements += aadhaar.count();

        ReplacementResult phone = replaceWithCount(aadhaar.value(), PHONE_PATTERN, "[PHONE_REDACTED]");
        totalReplacements += phone.count();

        ReplacementResult email = replaceWithCount(phone.value(), EMAIL_PATTERN, "[EMAIL_REDACTED]");
        totalReplacements += email.count();

        ReplacementResult pan = replaceWithCount(email.value(), PAN_PATTERN, "[PAN_REDACTED]");
        totalReplacements += pan.count();

        ReplacementResult card = replaceWithCount(pan.value(), CARD_PATTERN, "[CARD_REDACTED]");
        totalReplacements += card.count();

        log.info("PII scrub completed. replacementCount={}", totalReplacements);
        return card.value();
    }

    public boolean isPiiDetected(String text) {
        if (text == null || text.isBlank()) {
            return false;
        }
        return AADHAAR_PATTERN.matcher(text).find()
                || PHONE_PATTERN.matcher(text).find()
                || EMAIL_PATTERN.matcher(text).find()
                || PAN_PATTERN.matcher(text).find()
                || CARD_PATTERN.matcher(text).find();
    }

    private ReplacementResult replaceWithCount(String input, Pattern pattern, String replacement) {
        Matcher matcher = pattern.matcher(input);
        String output = matcher.replaceAll(replacement);
        int count = 0;
        matcher.reset();
        while (matcher.find()) {
            count++;
        }
        return new ReplacementResult(output, count);
    }

    private record ReplacementResult(String value, int count) {
    }
}
