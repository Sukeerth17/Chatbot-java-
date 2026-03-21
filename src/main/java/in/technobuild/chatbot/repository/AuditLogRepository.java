package in.technobuild.chatbot.repository;

import in.technobuild.chatbot.entity.AuditLog;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    List<AuditLog> findByUserId(Long userId);

    List<AuditLog> findByUserIdAndCreatedAtBetween(Long userId, LocalDateTime from, LocalDateTime to);
}
