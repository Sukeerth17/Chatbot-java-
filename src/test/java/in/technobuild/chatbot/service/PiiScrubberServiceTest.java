package in.technobuild.chatbot.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PiiScrubberServiceTest {

    private final PiiScrubberService piiScrubberService = new PiiScrubberService();

    @Test
    void scrub_aadhaarNumber_isRedacted() {
        String output = piiScrubberService.scrub("My Aadhaar is 1234 5678 9012");
        assertTrue(output.contains("[AADHAAR_REDACTED]"));
    }

    @Test
    void scrub_indianPhoneNumber_isRedacted() {
        String output = piiScrubberService.scrub("Call me at 9876543210");
        assertTrue(output.contains("[PHONE_REDACTED]"));
    }

    @Test
    void scrub_emailAddress_isRedacted() {
        String output = piiScrubberService.scrub("Mail me at test.user@example.com");
        assertTrue(output.contains("[EMAIL_REDACTED]"));
    }

    @Test
    void scrub_panNumber_isRedacted() {
        String output = piiScrubberService.scrub("PAN ABCDE1234F");
        assertTrue(output.contains("[PAN_REDACTED]"));
    }

    @Test
    void scrub_creditCardNumber_isRedacted() {
        String output = piiScrubberService.scrub("Card 1234 5678 9012 3456");
        assertTrue(output.contains("[CARD_REDACTED]"));
    }

    @Test
    void scrub_cleanText_returnsUnchanged() {
        String input = "Hello, how are you";
        assertEquals(input, piiScrubberService.scrub(input));
    }

    @Test
    void scrub_multipleTypesInOneMessage_allRedacted() {
        String output = piiScrubberService.scrub("Mail a@b.com and call 9876543210");
        assertTrue(output.contains("[EMAIL_REDACTED]"));
        assertTrue(output.contains("[PHONE_REDACTED]"));
    }

    @Test
    void isPiiDetected_withAadhaar_returnsTrue() {
        assertTrue(piiScrubberService.isPiiDetected("1234 5678 9012"));
    }

    @Test
    void isPiiDetected_cleanText_returnsFalse() {
        assertFalse(piiScrubberService.isPiiDetected("just a normal sentence"));
    }
}
