package com.cimhans.dto.response;

import com.cimhans.domain.enums.Gender;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
public class PatientResponse {
    private String id;
    private String mrn;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private LocalDate dateOfBirth;
    private Gender gender;
    private String address;
    private String city;
    private String state;
    private String pincode;
    private String bloodGroup;
    private String occupation;
    private String maritalStatus;
    private String referralSource;
    private String chiefComplaint;
    private String medicalHistory;
    private String currentMedications;
    private String allergies;
    private String insuranceProvider;
    private String insuranceNumber;
    private String emergencyContactName;
    private String emergencyContactPhone;
    private String emergencyContactRelation;
    private boolean isActiveCase;
    private int totalAppointments;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
