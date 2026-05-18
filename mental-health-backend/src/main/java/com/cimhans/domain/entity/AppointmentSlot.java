package com.cimhans.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Represents a bookable availability window for a therapist.
 * A slot is reserved when an Appointment links to it.
 */
@Entity
@Table(name = "appointment_slots", indexes = {
    @Index(name = "idx_slots_therapist_date", columnList = "therapist_id, slot_date"),
    @Index(name = "idx_slots_available", columnList = "is_booked, slot_date")
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class AppointmentSlot extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "therapist_id", nullable = false)
    private Therapist therapist;

    @Column(name = "slot_date", nullable = false)
    private LocalDate slotDate;

    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;

    @Column(name = "is_booked", nullable = false)
    @Builder.Default
    private boolean isBooked = false;

    @Column(name = "duration_minutes", nullable = false)
    @Builder.Default
    private Integer durationMinutes = 60;

    @OneToOne(mappedBy = "slot", fetch = FetchType.LAZY)
    private Appointment appointment;
}
