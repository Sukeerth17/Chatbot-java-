CREATE TABLE IF NOT EXISTS vector_chunks (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    document_id BIGINT NOT NULL,
    content TEXT NOT NULL,
    embedding JSON NOT NULL,
    chunk_index INT NOT NULL,
    token_count INT NOT NULL,
    heading VARCHAR(500),
    source_file VARCHAR(500),
    category VARCHAR(100),
    audience ENUM('CUSTOMER', 'INTERNAL', 'ADMIN') DEFAULT 'CUSTOMER',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FULLTEXT INDEX ft_content (content),
    INDEX idx_document_id (document_id),
    INDEX idx_category (category),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
