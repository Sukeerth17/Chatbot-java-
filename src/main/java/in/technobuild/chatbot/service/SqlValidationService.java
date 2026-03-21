package in.technobuild.chatbot.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class SqlValidationService {

    private static final List<String> BLOCKED_KEYWORDS = List.of(
            "UPDATE", "DELETE", "DROP", "INSERT", "TRUNCATE", "ALTER", "CREATE", "EXEC", "EXECUTE"
    );

    private static final Pattern FROM_PATTERN = Pattern.compile("FROM\\s+(\\w+)", Pattern.CASE_INSENSITIVE);
    private static final Pattern JOIN_PATTERN = Pattern.compile("JOIN\\s+(\\w+)", Pattern.CASE_INSENSITIVE);

    private final List<String> allowedTables;

    public SqlValidationService(@Value("${chatbot.sql.allowed-tables:}") List<String> allowedTables) {
        this.allowedTables = allowedTables.stream().map(t -> t.toLowerCase(Locale.ROOT)).toList();
    }

    public ValidationResult validate(String sql) {
        if (sql == null || sql.isBlank()) {
            return new ValidationResult(false, "SQL is empty", null);
        }

        String trimmed = sql.trim();
        String upper = trimmed.toUpperCase(Locale.ROOT);

        if (!isSelectQuery(trimmed)) {
            return new ValidationResult(false, "Only SELECT queries are allowed", null);
        }

        for (String keyword : BLOCKED_KEYWORDS) {
            if (upper.contains(keyword)) {
                return new ValidationResult(false, "Query contains blocked keyword: " + keyword, null);
            }
        }

        List<String> tables = extractTables(trimmed);
        for (String table : tables) {
            if (!allowedTables.isEmpty() && !allowedTables.contains(table.toLowerCase(Locale.ROOT))) {
                return new ValidationResult(false, "Table " + table + " is not in the allowed list", null);
            }
        }

        String sanitizedSql = trimmed;
        if (!upper.contains(" LIMIT ")) {
            sanitizedSql = trimmed + " LIMIT 100";
        }

        String[] statements = sanitizedSql.split(";");
        int nonEmptyStatements = 0;
        for (String statement : statements) {
            if (!statement.trim().isEmpty()) {
                nonEmptyStatements++;
            }
        }

        if (nonEmptyStatements > 1) {
            return new ValidationResult(false, "Multiple statements not allowed", null);
        }

        return new ValidationResult(true, "VALID", sanitizedSql.trim());
    }

    public boolean isSelectQuery(String sql) {
        return sql != null && sql.trim().toUpperCase(Locale.ROOT).startsWith("SELECT");
    }

    private List<String> extractTables(String sql) {
        List<String> tables = new ArrayList<>();
        Matcher fromMatcher = FROM_PATTERN.matcher(sql);
        while (fromMatcher.find()) {
            tables.add(fromMatcher.group(1));
        }

        Matcher joinMatcher = JOIN_PATTERN.matcher(sql);
        while (joinMatcher.find()) {
            tables.add(joinMatcher.group(1));
        }

        return tables;
    }

    public record ValidationResult(boolean valid, String reason, String sanitizedSql) {
    }
}
