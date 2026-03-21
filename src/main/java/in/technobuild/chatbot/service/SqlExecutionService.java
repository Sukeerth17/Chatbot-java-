package in.technobuild.chatbot.service;

import in.technobuild.chatbot.entity.SqlAuditLog;
import in.technobuild.chatbot.repository.SqlAuditLogRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.sql.DataSource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class SqlExecutionService {

    @Qualifier("businessDataSource")
    private final DataSource businessDataSource;

    private final SqlAuditLogRepository sqlAuditLogRepository;

    public SqlExecutionResult execute(String validatedSql, Long userId, String sessionId, String originalQuestion) {
        long start = System.currentTimeMillis();
        JdbcTemplate jdbcTemplate = new JdbcTemplate(businessDataSource);

        try {
            List<Map<String, Object>> explainRows = jdbcTemplate.queryForList("EXPLAIN " + validatedSql);
            int estimatedRows = explainRows.stream()
                    .map(row -> row.get("rows"))
                    .filter(Number.class::isInstance)
                    .map(Number.class::cast)
                    .mapToInt(Number::intValue)
                    .max()
                    .orElse(0);

            if (estimatedRows > 10000) {
                saveAudit(userId, sessionId, originalQuestion, validatedSql, true, false, 0,
                        System.currentTimeMillis() - start, "Query would scan too many rows");
                return new SqlExecutionResult(false, List.of(), 0,
                        System.currentTimeMillis() - start, "Query would scan too many rows");
            }

            List<Map<String, Object>> rows = jdbcTemplate.queryForList(validatedSql);
            long executionMs = System.currentTimeMillis() - start;
            saveAudit(userId, sessionId, originalQuestion, validatedSql, true, true, rows.size(), executionMs, null);

            return new SqlExecutionResult(true, rows, rows.size(), executionMs, null);
        } catch (Exception ex) {
            long executionMs = System.currentTimeMillis() - start;
            log.error("SQL execution failed", ex);
            saveAudit(userId, sessionId, originalQuestion, validatedSql, true, false, 0, executionMs,
                    "Query execution failed");
            return new SqlExecutionResult(false, List.of(), 0, executionMs, "Query execution failed");
        }
    }

    public String formatRowsAsText(List<Map<String, Object>> rows) {
        if (rows == null || rows.isEmpty()) {
            return "No records found.";
        }

        List<Map<String, Object>> toFormat = rows.size() > 10 ? new ArrayList<>(rows.subList(0, 10)) : rows;
        StringBuilder builder = new StringBuilder();

        for (int i = 0; i < toFormat.size(); i++) {
            Map<String, Object> row = toFormat.get(i);
            builder.append("Row ").append(i + 1).append(": ");
            builder.append(row.entrySet().stream()
                    .map(entry -> entry.getKey() + "=" + String.valueOf(entry.getValue()))
                    .reduce((a, b) -> a + ", " + b)
                    .orElse(""));
            builder.append("\n");
        }

        if (rows.size() > 10) {
            builder.append("... and ").append(rows.size() - 10).append(" more records");
        }

        return builder.toString();
    }

    private void saveAudit(Long userId,
                           String sessionId,
                           String originalQuestion,
                           String sql,
                           boolean validated,
                           boolean executed,
                           int rowCount,
                           long executionMs,
                           String rejectedReason) {
        SqlAuditLog logEntry = SqlAuditLog.builder()
                .userId(userId)
                .sessionId(sessionId)
                .originalQuestion(originalQuestion)
                .generatedSql(sql)
                .validated(validated)
                .executed(executed)
                .rowCount(rowCount)
                .executionMs((int) executionMs)
                .rejectedReason(rejectedReason)
                .build();

        sqlAuditLogRepository.save(logEntry);
    }

    public record SqlExecutionResult(boolean success,
                                     List<Map<String, Object>> rows,
                                     int rowCount,
                                     long executionMs,
                                     String errorMessage) {
    }
}
