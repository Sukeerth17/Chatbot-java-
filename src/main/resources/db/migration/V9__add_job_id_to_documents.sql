CREATE EXTENSION IF NOT EXISTS pgcrypto;

ALTER TABLE documents
    ADD COLUMN IF NOT EXISTS job_id VARCHAR(36);

UPDATE documents
SET job_id = gen_random_uuid()::text
WHERE job_id IS NULL;

ALTER TABLE documents
    ALTER COLUMN job_id SET NOT NULL;

CREATE UNIQUE INDEX IF NOT EXISTS uq_documents_job_id ON documents(job_id);
CREATE INDEX IF NOT EXISTS idx_documents_job_id ON documents(job_id);
