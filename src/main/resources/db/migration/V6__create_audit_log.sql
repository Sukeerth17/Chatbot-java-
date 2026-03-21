CREATE TABLE IF NOT EXISTS audit_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    session_id VARCHAR(36),
    event_type ENUM('QUESTION', 'ANSWER', 'FEEDBACK', 'ERROR', 'LOGIN', 'LOGOUT', 'DATA_DELETION', 'DOC_UPLOAD') NOT NULL,
    question_hash VARCHAR(64),
    doc_ids_retrieved JSON,
    response_hash VARCHAR(64),
    tokens_used INT DEFAULT 0,
    latency_ms INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_user_id (user_id),
    INDEX idx_event_type (event_type),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Append-only. Never UPDATE or DELETE rows.';
