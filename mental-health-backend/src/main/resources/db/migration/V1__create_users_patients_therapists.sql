-- ============================================================
-- V1: Core Tables — users, patients, therapists
-- ============================================================

-- Extension for UUID generation
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- ============================================================
-- USERS
-- ============================================================
CREATE TABLE users (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email           VARCHAR(255)    NOT NULL UNIQUE,
    password_hash   VARCHAR(60)     NOT NULL,
    first_name      VARCHAR(100)    NOT NULL,
    last_name       VARCHAR(100)    NOT NULL,
    phone           VARCHAR(20),
    role            VARCHAR(20)     NOT NULL CHECK (role IN ('ADMIN','PSYCHIATRIST','PSYCHOLOGIST','RECEPTIONIST','PATIENT')),
    is_active       BOOLEAN         NOT NULL DEFAULT TRUE,
    is_email_verified BOOLEAN       NOT NULL DEFAULT FALSE,
    last_login_at   TIMESTAMP,
    profile_picture_url VARCHAR(500),
    created_at      TIMESTAMP       NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP,
    created_by      VARCHAR(255),
    updated_by      VARCHAR(255),
    deleted_at      TIMESTAMP,
    version         BIGINT          NOT NULL DEFAULT 0,
    CONSTRAINT chk_users_email_format CHECK (email ~* '^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$')
);

CREATE INDEX idx_users_email      ON users(email)      WHERE deleted_at IS NULL;
CREATE INDEX idx_users_role       ON users(role)        WHERE deleted_at IS NULL;
CREATE INDEX idx_users_is_active  ON users(is_active)  WHERE deleted_at IS NULL;
CREATE INDEX idx_users_deleted_at ON users(deleted_at);

-- ============================================================
-- PATIENTS
-- ============================================================
CREATE TABLE patients (
    id                          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id                     UUID            NOT NULL UNIQUE REFERENCES users(id),
    mrn                         VARCHAR(20)     NOT NULL UNIQUE,
    date_of_birth               DATE            NOT NULL,
    gender                      VARCHAR(20)     CHECK (gender IN ('MALE','FEMALE','NON_BINARY','PREFER_NOT_TO_SAY')),
    address                     VARCHAR(500),
    city                        VARCHAR(100),
    state                       VARCHAR(100),
    pincode                     VARCHAR(10),
    blood_group                 VARCHAR(10),
    occupation                  VARCHAR(100),
    marital_status              VARCHAR(20),
    referral_source             VARCHAR(200),
    chief_complaint             TEXT,
    medical_history             TEXT,
    current_medications         TEXT,
    allergies                   TEXT,
    insurance_provider          VARCHAR(200),
    insurance_number            VARCHAR(100),
    emergency_contact_name      VARCHAR(200),
    emergency_contact_phone     VARCHAR(20),
    emergency_contact_relation  VARCHAR(50),
    is_active_case              BOOLEAN         NOT NULL DEFAULT TRUE,
    created_at                  TIMESTAMP       NOT NULL DEFAULT NOW(),
    updated_at                  TIMESTAMP,
    created_by                  VARCHAR(255),
    updated_by                  VARCHAR(255),
    deleted_at                  TIMESTAMP,
    version                     BIGINT          NOT NULL DEFAULT 0
);

CREATE INDEX idx_patients_mrn        ON patients(mrn)        WHERE deleted_at IS NULL;
CREATE INDEX idx_patients_user_id    ON patients(user_id);
CREATE INDEX idx_patients_active     ON patients(is_active_case) WHERE deleted_at IS NULL;
CREATE INDEX idx_patients_deleted_at ON patients(deleted_at);
CREATE INDEX idx_patients_dob        ON patients(date_of_birth);

-- ============================================================
-- THERAPISTS
-- ============================================================
CREATE TABLE therapists (
    id                      UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id                 UUID            NOT NULL UNIQUE REFERENCES users(id),
    specialization          VARCHAR(50)     NOT NULL,
    license_number          VARCHAR(50)     NOT NULL UNIQUE,
    qualification           VARCHAR(500),
    years_of_experience     INTEGER,
    bio                     TEXT,
    consultation_fee_inr    DOUBLE PRECISION NOT NULL DEFAULT 500.0,
    max_patients_per_day    INTEGER         NOT NULL DEFAULT 10,
    is_available            BOOLEAN         NOT NULL DEFAULT TRUE,
    department              VARCHAR(100),
    created_at              TIMESTAMP       NOT NULL DEFAULT NOW(),
    updated_at              TIMESTAMP,
    created_by              VARCHAR(255),
    updated_by              VARCHAR(255),
    deleted_at              TIMESTAMP,
    version                 BIGINT          NOT NULL DEFAULT 0
);

CREATE INDEX idx_therapists_user_id   ON therapists(user_id);
CREATE INDEX idx_therapists_license   ON therapists(license_number) WHERE deleted_at IS NULL;
CREATE INDEX idx_therapists_available ON therapists(is_available)   WHERE deleted_at IS NULL;
CREATE INDEX idx_therapists_spec      ON therapists(specialization)  WHERE deleted_at IS NULL;

-- ============================================================
-- SEED: Default admin user (password: Admin@123 bcrypt)
-- ============================================================
INSERT INTO users (id, email, password_hash, first_name, last_name, role, is_active, is_email_verified)
VALUES (
    gen_random_uuid(),
    'admin@cimhans.com',
    '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQyCkJ0/MJQlTOiQhz.fD1BIy',
    'System', 'Admin', 'ADMIN', TRUE, TRUE
);
