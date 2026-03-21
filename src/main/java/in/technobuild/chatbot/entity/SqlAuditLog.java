package in.technobuild.chatbot.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
@Table(name = "sql_audit_log")
public class SqlAuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "session_id", length = 36)
    private String sessionId;

    @Column(name = "original_question", nullable = false, columnDefinition = "TEXT")
    private String originalQuestion;

    @Column(name = "generated_sql", nullable = false, columnDefinition = "TEXT")
    private String generatedSql;

    @Column(name = "validated")
    private Boolean validated;

    @Column(name = "executed")
    private Boolean executed;

    @Column(name = "row_count")
    private Integer rowCount;

    @Column(name = "execution_ms")
    private Integer executionMs;

    @Column(name = "rejected_reason", length = 500)
    private String rejectedReason;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
