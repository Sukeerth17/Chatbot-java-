CREATE TABLE IF NOT EXISTS documents (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    file_name VARCHAR(500) NOT NULL,
    file_hash VARCHAR(64) NOT NULL,
    file_type VARCHAR(50),
    category VARCHAR(100),
    audience ENUM('CUSTOMER', 'INTERNAL', 'ADMIN') DEFAULT 'CUSTOMER',
    status ENUM('PENDING', 'PROCESSING', 'COMPLETED', 'FAILED', 'STALE') DEFAULT 'PENDING',
    chunk_count INT DEFAULT 0,
    last_modified TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_embedded TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    uploaded_by BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_file_hash (file_hash),
    INDEX idx_status (status),
    INDEX idx_uploaded_by (uploaded_by),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

ALTER TABLE vector_chunks
    ADD CONSTRAINT fk_vector_chunks_document
    FOREIGN KEY (document_id) REFERENCES documents(id)
    ON DELETE CASCADE;
