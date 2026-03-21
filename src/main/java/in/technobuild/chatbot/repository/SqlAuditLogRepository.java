package in.technobuild.chatbot.repository;

import in.technobuild.chatbot.entity.SqlAuditLog;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SqlAuditLogRepository extends JpaRepository<SqlAuditLog, Long> {

    List<SqlAuditLog> findByUserId(Long userId);

    List<SqlAuditLog> findByUserIdAndCreatedAtBetween(Long userId, LocalDateTime from, LocalDateTime to);

    List<SqlAuditLog> findByValidatedFalse();
}
