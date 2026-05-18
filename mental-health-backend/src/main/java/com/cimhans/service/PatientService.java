package com.cimhans.service;

import com.cimhans.domain.entity.Patient;
import com.cimhans.domain.entity.User;
import com.cimhans.domain.enums.Role;
import com.cimhans.dto.request.CreatePatientRequest;
import com.cimhans.dto.response.PatientResponse;
import com.cimhans.exception.BusinessException;
import com.cimhans.exception.ResourceNotFoundException;
import com.cimhans.repository.PatientRepository;
import com.cimhans.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Service
@RequiredArgsConstructor
public class PatientService {

    private final PatientRepository patientRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditService auditService;
    private final NotificationService notificationService;

    // Atomic counter for MRN generation — in production use a DB sequence
    private static final AtomicInteger mrnCounter = new AtomicInteger(1000);

    @Transactional
    @CacheEvict(value = "patients", allEntries = true)
    public PatientResponse createPatient(CreatePatientRequest request) {
        if (userRepository.existsByEmailAndDeletedAtIsNull(request.getEmail())) {
            throw new BusinessException("A patient with this email already exists: " + request.getEmail());
        }

        User user = User.builder()
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .phone(request.getPhone())
                .role(Role.PATIENT)
                .isActive(true)
                .build();
        user = userRepository.save(user);

        String mrn = generateMrn();
        Patient patient = Patient.builder()
                .user(user)
                .mrn(mrn)
                .dateOfBirth(request.getDateOfBirth())
                .gender(request.getGender())
                .address(request.getAddress())
                .city(request.getCity())
                .state(request.getState())
                .pincode(request.getPincode())
                .bloodGroup(request.getBloodGroup())
                .occupation(request.getOccupation())
                .maritalStatus(request.getMaritalStatus())
                .referralSource(request.getReferralSource())
                .chiefComplaint(request.getChiefComplaint())
                .medicalHistory(request.getMedicalHistory())
                .currentMedications(request.getCurrentMedications())
                .allergies(request.getAllergies())
                .insuranceProvider(request.getInsuranceProvider())
                .insuranceNumber(request.getInsuranceNumber())
                .emergencyContactName(request.getEmergencyContactName())
                .emergencyContactPhone(request.getEmergencyContactPhone())
                .emergencyContactRelation(request.getEmergencyContactRelation())
                .build();

        patient = patientRepository.save(patient);
        auditService.logSuccess("PATIENT_CREATED", "Patient", patient.getId(), "MRN: " + mrn);
        log.info("Patient created: {} (MRN: {})", user.getEmail(), mrn);

        return toResponse(patient, 0);
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "patients", key = "#id")
    public PatientResponse getPatientById(UUID id) {
        Patient patient = patientRepository.findById(id)
                .filter(p -> p.getDeletedAt() == null)
                .orElseThrow(() -> new ResourceNotFoundException("Patient", "id", id));
        int apptCount = patient.getAppointments() != null ? patient.getAppointments().size() : 0;
        return toResponse(patient, apptCount);
    }

    @Transactional(readOnly = true)
    public PatientResponse getPatientByMrn(String mrn) {
        Patient patient = patientRepository.findByMrnAndDeletedAtIsNull(mrn)
                .orElseThrow(() -> new ResourceNotFoundException("Patient", "MRN", mrn));
        return toResponse(patient, 0);
    }

    @Transactional(readOnly = true)
    public Page<PatientResponse> getAllPatients(Pageable pageable) {
        return patientRepository.findAllActivePatients(pageable)
                .map(p -> toResponse(p, 0));
    }

    @Transactional(readOnly = true)
    public Page<PatientResponse> searchPatients(String query, Pageable pageable) {
        return patientRepository.searchPatients(query, pageable)
                .map(p -> toResponse(p, 0));
    }

    @Transactional
    @CacheEvict(value = "patients", key = "#id")
    public void softDeletePatient(UUID id) {
        Patient patient = patientRepository.findById(id)
                .filter(p -> p.getDeletedAt() == null)
                .orElseThrow(() -> new ResourceNotFoundException("Patient", "id", id));
        patient.softDelete();
        patient.getUser().softDelete();
        patientRepository.save(patient);
        userRepository.save(patient.getUser());
        auditService.logSuccess("PATIENT_DELETED", "Patient", id, "Soft deleted");
    }

    private String generateMrn() {
        return "CIMH-" + String.format("%06d", mrnCounter.getAndIncrement());
    }

    public PatientResponse toResponse(Patient p, int apptCount) {
        return PatientResponse.builder()
                .id(p.getId().toString())
                .mrn(p.getMrn())
                .firstName(p.getUser().getFirstName())
                .lastName(p.getUser().getLastName())
                .email(p.getUser().getEmail())
                .phone(p.getUser().getPhone())
                .dateOfBirth(p.getDateOfBirth())
                .gender(p.getGender())
                .address(p.getAddress())
                .city(p.getCity())
                .state(p.getState())
                .pincode(p.getPincode())
                .bloodGroup(p.getBloodGroup())
                .occupation(p.getOccupation())
                .maritalStatus(p.getMaritalStatus())
                .referralSource(p.getReferralSource())
                .chiefComplaint(p.getChiefComplaint())
                .medicalHistory(p.getMedicalHistory())
                .currentMedications(p.getCurrentMedications())
                .allergies(p.getAllergies())
                .insuranceProvider(p.getInsuranceProvider())
                .insuranceNumber(p.getInsuranceNumber())
                .emergencyContactName(p.getEmergencyContactName())
                .emergencyContactPhone(p.getEmergencyContactPhone())
                .emergencyContactRelation(p.getEmergencyContactRelation())
                .isActiveCase(p.isActiveCase())
                .totalAppointments(apptCount)
                .createdAt(p.getCreatedAt())
                .updatedAt(p.getUpdatedAt())
                .build();
    }
}
