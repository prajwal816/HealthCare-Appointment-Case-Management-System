package com.cimhans.domain.entity;

import com.cimhans.domain.enums.TherapistSpecialization;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "therapists", indexes = {
    @Index(name = "idx_therapists_user_id", columnList = "user_id", unique = true),
    @Index(name = "idx_therapists_license", columnList = "license_number")
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Therapist extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "specialization", nullable = false, length = 50)
    private TherapistSpecialization specialization;

    @Column(name = "license_number", nullable = false, unique = true, length = 50)
    private String licenseNumber;

    @Column(name = "qualification", length = 500)
    private String qualification;

    @Column(name = "years_of_experience")
    private Integer yearsOfExperience;

    @Column(name = "bio", columnDefinition = "TEXT")
    private String bio;

    @Column(name = "consultation_fee_inr", nullable = false)
    private Double consultationFeeInr;

    @Column(name = "max_patients_per_day", nullable = false)
    @Builder.Default
    private Integer maxPatientsPerDay = 10;

    @Column(name = "is_available", nullable = false)
    @Builder.Default
    private boolean isAvailable = true;

    @Column(name = "department", length = 100)
    private String department;

    @OneToMany(mappedBy = "therapist", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<Appointment> appointments = new ArrayList<>();

    @OneToMany(mappedBy = "therapist", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<AppointmentSlot> slots = new ArrayList<>();
}
