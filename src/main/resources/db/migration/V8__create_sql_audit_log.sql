CREATE TABLE IF NOT EXISTS sql_audit_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    session_id VARCHAR(36),
    original_question TEXT NOT NULL,
    generated_sql TEXT NOT NULL,
    validated BOOLEAN DEFAULT FALSE,
    executed BOOLEAN DEFAULT FALSE,
    row_count INT DEFAULT 0,
    execution_ms INT DEFAULT 0,
    rejected_reason VARCHAR(500),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_user_id (user_id),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
