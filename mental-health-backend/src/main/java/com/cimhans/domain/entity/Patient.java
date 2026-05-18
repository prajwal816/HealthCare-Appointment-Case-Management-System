package com.cimhans.domain.entity;

import com.cimhans.domain.enums.Gender;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Patient entity storing full demographic, medical history, and emergency contact data.
 * MRN (Medical Record Number) is the primary hospital identifier for patients.
 * Linked 1-to-1 with a User account for portal login.
 */
@Entity
@Table(name = "patients", indexes = {
    @Index(name = "idx_patients_mrn", columnList = "mrn", unique = true),
    @Index(name = "idx_patients_user_id", columnList = "user_id"),
    @Index(name = "idx_patients_deleted_at", columnList = "deleted_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Patient extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(name = "mrn", nullable = false, unique = true, length = 20)
    private String mrn;

    @Column(name = "date_of_birth", nullable = false)
    private LocalDate dateOfBirth;

    @Enumerated(EnumType.STRING)
    @Column(name = "gender", length = 20)
    private Gender gender;

    @Column(name = "address", length = 500)
    private String address;

    @Column(name = "city", length = 100)
    private String city;

    @Column(name = "state", length = 100)
    private String state;

    @Column(name = "pincode", length = 10)
    private String pincode;

    @Column(name = "blood_group", length = 10)
    private String bloodGroup;

    @Column(name = "occupation", length = 100)
    private String occupation;

    @Column(name = "marital_status", length = 20)
    private String maritalStatus;

    @Column(name = "referral_source", length = 200)
    private String referralSource;

    // Stored as plain text — detailed encryption handled at service layer for session notes
    @Column(name = "chief_complaint", columnDefinition = "TEXT")
    private String chiefComplaint;

    @Column(name = "medical_history", columnDefinition = "TEXT")
    private String medicalHistory;

    @Column(name = "current_medications", columnDefinition = "TEXT")
    private String currentMedications;

    @Column(name = "allergies", columnDefinition = "TEXT")
    private String allergies;

    @Column(name = "insurance_provider", length = 200)
    private String insuranceProvider;

    @Column(name = "insurance_number", length = 100)
    private String insuranceNumber;

    // Emergency Contact
    @Column(name = "emergency_contact_name", length = 200)
    private String emergencyContactName;

    @Column(name = "emergency_contact_phone", length = 20)
    private String emergencyContactPhone;

    @Column(name = "emergency_contact_relation", length = 50)
    private String emergencyContactRelation;

    @Column(name = "is_active_case", nullable = false)
    @Builder.Default
    private boolean isActiveCase = true;

    @OneToMany(mappedBy = "patient", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<Appointment> appointments = new ArrayList<>();

    @OneToMany(mappedBy = "patient", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<SessionNote> sessionNotes = new ArrayList<>();
}
