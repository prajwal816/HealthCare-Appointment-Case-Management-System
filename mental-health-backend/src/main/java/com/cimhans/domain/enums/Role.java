package com.cimhans.domain.enums;

/**
 * System roles with hierarchical access control.
 * ADMIN has full system access.
 * PSYCHIATRIST / PSYCHOLOGIST are therapist types with clinical access.
 * RECEPTIONIST manages bookings and patient registration.
 * PATIENT has limited self-service access.
 */
public enum Role {
    ADMIN,
    PSYCHIATRIST,
    PSYCHOLOGIST,
    RECEPTIONIST,
    PATIENT
}
