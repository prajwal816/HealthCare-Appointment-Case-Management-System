package com.cimhans.domain.entity;

import com.cimhans.domain.enums.AppointmentStatus;
import com.cimhans.domain.enums.AppointmentType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Appointment entity representing a scheduled session between a patient and therapist.
 * Transitions through a state machine: SCHEDULED → CONFIRMED → COMPLETED | CANCELLED | NO_SHOW.
 * Links to an AppointmentSlot to enforce therapist availability and prevent double-booking.
 */
@Entity
@Table(name = "appointments", indexes = {
    @Index(name = "idx_appt_patient", columnList = "patient_id"),
    @Index(name = "idx_appt_therapist", columnList = "therapist_id"),
    @Index(name = "idx_appt_date_status", columnList = "appointment_date, status"),
    @Index(name = "idx_appt_deleted_at", columnList = "deleted_at")
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Appointment extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "therapist_id", nullable = false)
    private Therapist therapist;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "slot_id")
    private AppointmentSlot slot;

    @Column(name = "appointment_date", nullable = false)
    private LocalDate appointmentDate;

    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 40)
    private AppointmentType type;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private AppointmentStatus status = AppointmentStatus.SCHEDULED;

    @Column(name = "reason_for_visit", columnDefinition = "TEXT")
    private String reasonForVisit;

    @Column(name = "cancellation_reason", columnDefinition = "TEXT")
    private String cancellationReason;

    @Column(name = "reschedule_reason", columnDefinition = "TEXT")
    private String rescheduleReason;

    /** Reference to the appointment that replaced this one after rescheduling */
    @Column(name = "rescheduled_to_id", columnDefinition = "uuid")
    private java.util.UUID rescheduledToId;

    @Column(name = "is_reminder_sent", nullable = false)
    @Builder.Default
    private boolean isReminderSent = false;

    @Column(name = "booked_by_user_id", columnDefinition = "uuid")
    private java.util.UUID bookedByUserId;

    @Column(name = "notes_for_therapist", columnDefinition = "TEXT")
    private String notesForTherapist;

    @OneToOne(mappedBy = "appointment", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private SessionNote sessionNote;
}
