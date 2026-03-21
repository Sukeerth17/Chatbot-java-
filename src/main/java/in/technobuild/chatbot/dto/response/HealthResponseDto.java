package in.technobuild.chatbot.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HealthResponseDto {

    private String mysql;
    private String redis;
    private String kafka;
    private String ollama;
    private String python;

    public boolean isAllUp() {
        return isUp(mysql) && isUp(redis) && isUp(kafka) && isUp(ollama) && isUp(python);
    }

    private boolean isUp(String status) {
        return "UP".equalsIgnoreCase(status);
    }
}
