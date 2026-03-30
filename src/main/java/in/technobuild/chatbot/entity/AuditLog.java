package in.technobuild.chatbot.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "audit_log")
public class AuditLog {

    public enum EventType {
        QUESTION,
        ANSWER,
        FEEDBACK,
        ERROR,
        LOGIN,
        LOGOUT,
        DATA_DELETION,
        DOC_UPLOAD
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "session_id", length = 36)
    private String sessionId;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false)
    private EventType eventType;

    @Column(name = "question_hash", length = 64)
    private String questionHash;

    @Column(name = "doc_ids_retrieved", columnDefinition = "jsonb")
    private String docIdsRetrieved;

    @Column(name = "response_hash", length = 64)
    private String responseHash;

    @Column(name = "tokens_used")
    private Integer tokensUsed;

    @Column(name = "latency_ms")
    private Integer latencyMs;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
