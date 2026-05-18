-- ============================================================
-- V4: Documents, Refresh Tokens, Performance Indexes
-- ============================================================

-- ============================================================
-- DOCUMENTS (Patient File Uploads)
-- ============================================================
CREATE TABLE documents (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    patient_id          UUID         NOT NULL REFERENCES patients(id),
    uploaded_by_user_id UUID         NOT NULL,
    file_name           VARCHAR(255) NOT NULL,
    file_path           VARCHAR(1000) NOT NULL,
    file_type           VARCHAR(10),
    file_size_bytes     BIGINT,
    document_category   VARCHAR(50),
    description         VARCHAR(500),
    appointment_id      UUID         REFERENCES appointments(id),
    created_at          TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMP,
    created_by          VARCHAR(255),
    updated_by          VARCHAR(255),
    deleted_at          TIMESTAMP,
    version             BIGINT       NOT NULL DEFAULT 0
);

CREATE INDEX idx_doc_patient      ON documents(patient_id)          WHERE deleted_at IS NULL;
CREATE INDEX idx_doc_uploaded_by  ON documents(uploaded_by_user_id) WHERE deleted_at IS NULL;
CREATE INDEX idx_doc_appointment  ON documents(appointment_id)      WHERE deleted_at IS NULL;

-- ============================================================
-- REFRESH TOKENS
-- ============================================================
CREATE TABLE refresh_tokens (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id     UUID         NOT NULL REFERENCES users(id),
    token_hash  VARCHAR(128) NOT NULL UNIQUE,
    expires_at  TIMESTAMP    NOT NULL,
    is_revoked  BOOLEAN      NOT NULL DEFAULT FALSE,
    ip_address  VARCHAR(50),
    user_agent  VARCHAR(500),
    created_at  TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_rt_token_hash ON refresh_tokens(token_hash) WHERE is_revoked = FALSE;
CREATE INDEX idx_rt_user       ON refresh_tokens(user_id);
CREATE INDEX idx_rt_expires    ON refresh_tokens(expires_at) WHERE is_revoked = FALSE;

-- ============================================================
-- Additional composite indexes for analytics queries
-- ============================================================
CREATE INDEX idx_appt_analytics ON appointments(appointment_date, status, therapist_id)
    WHERE deleted_at IS NULL;

CREATE INDEX idx_patients_created ON patients(created_at DESC) WHERE deleted_at IS NULL;

-- ============================================================
-- Full-text search index for patient name/email
-- ============================================================
CREATE INDEX idx_users_fulltext ON users
    USING gin(to_tsvector('english', coalesce(first_name,'') || ' ' || coalesce(last_name,'') || ' ' || coalesce(email,'')));
