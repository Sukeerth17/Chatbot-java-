package in.technobuild.chatbot.service;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SqlValidationServiceTest {

    private SqlValidationService sqlValidationService;

    @BeforeEach
    void setUp() {
        sqlValidationService = new SqlValidationService(List.of("customers", "orders", "products"));
    }

    @Test
    void validate_selectQuery_returnsValid() {
        SqlValidationService.ValidationResult result = sqlValidationService.validate("SELECT * FROM customers");
        assertTrue(result.valid());
    }

    @Test
    void validate_updateQuery_returnsInvalid() {
        SqlValidationService.ValidationResult result = sqlValidationService.validate("UPDATE customers SET name='x'");
        assertFalse(result.valid());
    }

    @Test
    void validate_deleteQuery_returnsInvalid() {
        SqlValidationService.ValidationResult result = sqlValidationService.validate("DELETE FROM customers");
        assertFalse(result.valid());
    }

    @Test
    void validate_queryWithoutLimit_appendsLimit100() {
        SqlValidationService.ValidationResult result = sqlValidationService.validate("SELECT id FROM customers");
        assertTrue(result.sanitizedSql().toUpperCase().contains("LIMIT 100"));
    }

    @Test
    void validate_queryWithDisallowedTable_returnsInvalid() {
        SqlValidationService.ValidationResult result = sqlValidationService.validate("SELECT * FROM salaries");
        assertFalse(result.valid());
    }

    @Test
    void validate_queryWithMultipleStatements_returnsInvalid() {
        SqlValidationService.ValidationResult result = sqlValidationService.validate("SELECT * FROM customers; SELECT * FROM orders;");
        assertFalse(result.valid());
    }

    @Test
    void validate_validSelectWithJoin_returnsValid() {
        SqlValidationService.ValidationResult result = sqlValidationService.validate(
                "SELECT c.id, o.id FROM customers c JOIN orders o ON c.id = o.customer_id"
        );
        assertTrue(result.valid());
        assertNotNull(result.sanitizedSql());
    }
}
