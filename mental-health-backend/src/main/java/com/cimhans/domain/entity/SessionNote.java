package com.cimhans.domain.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * Confidential session notes written by therapists post-appointment.
 * The 'encryptedContent' field stores AES-256 encrypted clinical notes.
 * Only the assigned therapist and ADMIN can read the decrypted content.
 * Every access is logged in the audit_logs table.
 */
@Entity
@Table(name = "session_notes", indexes = {
    @Index(name = "idx_notes_appointment", columnList = "appointment_id"),
    @Index(name = "idx_notes_patient", columnList = "patient_id"),
    @Index(name = "idx_notes_therapist", columnList = "therapist_id")
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class SessionNote extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "appointment_id", nullable = false, unique = true)
    private Appointment appointment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "therapist_id", nullable = false)
    private Therapist therapist;

    /** AES-256 encrypted clinical notes — never stored in plaintext */
    @Column(name = "encrypted_content", columnDefinition = "TEXT", nullable = false)
    private String encryptedContent;

    /** Unencrypted follow-up instructions visible to patient if shared */
    @Column(name = "follow_up_instructions", columnDefinition = "TEXT")
    private String followUpInstructions;

    @Column(name = "mood_rating")
    private Integer moodRating;

    @Column(name = "risk_level", length = 20)
    private String riskLevel;

    @Column(name = "follow_up_required", nullable = false)
    @Builder.Default
    private boolean followUpRequired = false;

    @Column(name = "follow_up_date")
    private java.time.LocalDate followUpDate;

    @Column(name = "diagnosis_codes", length = 500)
    private String diagnosisCodes;

    @Column(name = "treatment_plan", columnDefinition = "TEXT")
    private String treatmentPlan;

    /** Whether therapist has marked this note as finalized (locked from edits) */
    @Column(name = "is_finalized", nullable = false)
    @Builder.Default
    private boolean isFinalized = false;
}
