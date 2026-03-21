package in.technobuild.chatbot.repository;

import in.technobuild.chatbot.entity.TokenUsage;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface TokenUsageRepository extends JpaRepository<TokenUsage, Long> {

    Optional<TokenUsage> findByUserIdAndUsageDate(Long userId, LocalDate usageDate);

    List<TokenUsage> findByUserId(Long userId);

    @Query("SELECT COALESCE(SUM(t.inputTokens + t.outputTokens), 0) FROM TokenUsage t " +
            "WHERE t.userId = :userId AND t.usageDate = :date")
    Long getTotalTokensForUserToday(@Param("userId") Long userId, @Param("date") LocalDate date);
}
