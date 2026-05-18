package com.cimhans.domain.enums;

/**
 * Lifecycle states for an appointment with valid transitions:
 * SCHEDULED → CONFIRMED → COMPLETED
 * SCHEDULED / CONFIRMED → CANCELLED
 * SCHEDULED / CONFIRMED → NO_SHOW
 * CONFIRMED → RESCHEDULED → SCHEDULED (new appointment created)
 */
public enum AppointmentStatus {
    SCHEDULED,
    CONFIRMED,
    COMPLETED,
    CANCELLED,
    NO_SHOW,
    RESCHEDULED
}
