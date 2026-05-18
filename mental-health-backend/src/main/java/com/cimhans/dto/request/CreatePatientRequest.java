package com.cimhans.dto.request;

import com.cimhans.domain.enums.Gender;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;

@Data
public class CreatePatientRequest {

    // User account fields
    @NotBlank @Size(min = 2, max = 100)
    private String firstName;

    @NotBlank @Size(min = 2, max = 100)
    private String lastName;

    @NotBlank @Email
    private String email;

    @NotBlank @Size(min = 8, max = 128)
    private String password;

    @Pattern(regexp = "^[+]?[0-9]{7,15}$")
    private String phone;

    // Patient-specific fields
    @NotNull(message = "Date of birth is required")
    @Past(message = "Date of birth must be in the past")
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

    // Emergency Contact
    @NotBlank(message = "Emergency contact name is required")
    private String emergencyContactName;

    @NotBlank(message = "Emergency contact phone is required")
    @Pattern(regexp = "^[+]?[0-9]{7,15}$")
    private String emergencyContactPhone;

    @NotBlank(message = "Emergency contact relation is required")
    private String emergencyContactRelation;
}
