-- ============================================================
-- V2: Appointment Slots + Appointments
-- ============================================================

-- ============================================================
-- APPOINTMENT SLOTS (Therapist Availability Windows)
-- ============================================================
CREATE TABLE appointment_slots (
    id               UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    therapist_id     UUID            NOT NULL REFERENCES therapists(id),
    slot_date        DATE            NOT NULL,
    start_time       TIME            NOT NULL,
    end_time         TIME            NOT NULL,
    is_booked        BOOLEAN         NOT NULL DEFAULT FALSE,
    duration_minutes INTEGER         NOT NULL DEFAULT 60,
    created_at       TIMESTAMP       NOT NULL DEFAULT NOW(),
    updated_at       TIMESTAMP,
    created_by       VARCHAR(255),
    updated_by       VARCHAR(255),
    deleted_at       TIMESTAMP,
    version          BIGINT          NOT NULL DEFAULT 0,
    CONSTRAINT chk_slot_times CHECK (end_time > start_time),
    CONSTRAINT uq_therapist_slot UNIQUE (therapist_id, slot_date, start_time)
);

CREATE INDEX idx_slots_therapist_date ON appointment_slots(therapist_id, slot_date) WHERE deleted_at IS NULL;
CREATE INDEX idx_slots_available      ON appointment_slots(slot_date, is_booked)    WHERE deleted_at IS NULL;

-- ============================================================
-- APPOINTMENTS
-- ============================================================
CREATE TABLE appointments (
    id                   UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    patient_id           UUID         NOT NULL REFERENCES patients(id),
    therapist_id         UUID         NOT NULL REFERENCES therapists(id),
    slot_id              UUID         REFERENCES appointment_slots(id),
    appointment_date     DATE         NOT NULL,
    start_time           TIME         NOT NULL,
    end_time             TIME         NOT NULL,
    type                 VARCHAR(40)  NOT NULL,
    status               VARCHAR(20)  NOT NULL DEFAULT 'SCHEDULED'
                           CHECK (status IN ('SCHEDULED','CONFIRMED','COMPLETED','CANCELLED','NO_SHOW','RESCHEDULED')),
    reason_for_visit     TEXT,
    cancellation_reason  TEXT,
    reschedule_reason    TEXT,
    rescheduled_to_id    UUID,
    is_reminder_sent     BOOLEAN      NOT NULL DEFAULT FALSE,
    booked_by_user_id    UUID,
    notes_for_therapist  TEXT,
    created_at           TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at           TIMESTAMP,
    created_by           VARCHAR(255),
    updated_by           VARCHAR(255),
    deleted_at           TIMESTAMP,
    version              BIGINT       NOT NULL DEFAULT 0,
    CONSTRAINT chk_appt_times CHECK (end_time > start_time)
);

CREATE INDEX idx_appt_patient      ON appointments(patient_id)                   WHERE deleted_at IS NULL;
CREATE INDEX idx_appt_therapist    ON appointments(therapist_id)                 WHERE deleted_at IS NULL;
CREATE INDEX idx_appt_date_status  ON appointments(appointment_date, status)     WHERE deleted_at IS NULL;
CREATE INDEX idx_appt_reminder     ON appointments(appointment_date, is_reminder_sent)
    WHERE deleted_at IS NULL AND status = 'CONFIRMED' AND is_reminder_sent = FALSE;
CREATE INDEX idx_appt_deleted_at   ON appointments(deleted_at);
