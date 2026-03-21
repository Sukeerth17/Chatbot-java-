package in.technobuild.chatbot.kafka.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IngestionEvent {

    private String jobId;
    private Long documentId;
    private String fileName;
    private String fileType;
    private String category;
    private String audience;
    private Long uploadedBy;
    private long timestamp;
}
