-- ============================================================
-- V3: Session Notes, Notifications, Audit Logs
-- ============================================================

-- ============================================================
-- SESSION NOTES (Encrypted Clinical Notes)
-- ============================================================
CREATE TABLE session_notes (
    id                    UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    appointment_id        UUID         NOT NULL UNIQUE REFERENCES appointments(id),
    patient_id            UUID         NOT NULL REFERENCES patients(id),
    therapist_id          UUID         NOT NULL REFERENCES therapists(id),
    encrypted_content     TEXT         NOT NULL,
    follow_up_instructions TEXT,
    mood_rating           INTEGER      CHECK (mood_rating BETWEEN 1 AND 10),
    risk_level            VARCHAR(20)  CHECK (risk_level IN ('LOW','MODERATE','HIGH','CRITICAL')),
    follow_up_required    BOOLEAN      NOT NULL DEFAULT FALSE,
    follow_up_date        DATE,
    diagnosis_codes       VARCHAR(500),
    treatment_plan        TEXT,
    is_finalized          BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at            TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at            TIMESTAMP,
    created_by            VARCHAR(255),
    updated_by            VARCHAR(255),
    deleted_at            TIMESTAMP,
    version               BIGINT       NOT NULL DEFAULT 0
);

CREATE INDEX idx_notes_appointment  ON session_notes(appointment_id);
CREATE INDEX idx_notes_patient      ON session_notes(patient_id)   WHERE deleted_at IS NULL;
CREATE INDEX idx_notes_therapist    ON session_notes(therapist_id) WHERE deleted_at IS NULL;
CREATE INDEX idx_notes_followup     ON session_notes(follow_up_date, follow_up_required)
    WHERE deleted_at IS NULL AND follow_up_required = TRUE;

-- ============================================================
-- NOTIFICATIONS
-- ============================================================
CREATE TABLE notifications (
    id               UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id          UUID         NOT NULL REFERENCES users(id),
    type             VARCHAR(40)  NOT NULL,
    title            VARCHAR(200) NOT NULL,
    message          TEXT         NOT NULL,
    is_read          BOOLEAN      NOT NULL DEFAULT FALSE,
    reference_id     UUID,
    reference_type   VARCHAR(50),
    email_sent       BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at       TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at       TIMESTAMP,
    created_by       VARCHAR(255),
    updated_by       VARCHAR(255),
    deleted_at       TIMESTAMP,
    version          BIGINT       NOT NULL DEFAULT 0
);

CREATE INDEX idx_notif_user     ON notifications(user_id)         WHERE deleted_at IS NULL;
CREATE INDEX idx_notif_unread   ON notifications(user_id, is_read) WHERE deleted_at IS NULL AND is_read = FALSE;
CREATE INDEX idx_notif_created  ON notifications(created_at DESC);

-- ============================================================
-- AUDIT LOGS (Immutable — no soft delete, no version)
-- ============================================================
CREATE TABLE audit_logs (
    id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    actor_id     UUID,
    actor_email  VARCHAR(255),
    action       VARCHAR(100)  NOT NULL,
    entity_type  VARCHAR(100),
    entity_id    UUID,
    details      TEXT,
    ip_address   VARCHAR(50),
    user_agent   VARCHAR(500),
    outcome      VARCHAR(20)   CHECK (outcome IN ('SUCCESS','FAILURE')),
    created_at   TIMESTAMP     NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_audit_actor      ON audit_logs(actor_id);
CREATE INDEX idx_audit_entity     ON audit_logs(entity_type, entity_id);
CREATE INDEX idx_audit_created_at ON audit_logs(created_at DESC);
CREATE INDEX idx_audit_action     ON audit_logs(action);
