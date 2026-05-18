package com.cimhans.controller.v1;

import com.cimhans.dto.request.CreatePatientRequest;
import com.cimhans.dto.response.ApiResponse;
import com.cimhans.dto.response.PatientResponse;
import com.cimhans.service.PatientService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/patients")
@RequiredArgsConstructor
@Tag(name = "Patient Management", description = "Patient CRUD, search, and profile management")
public class PatientController {

    private final PatientService patientService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','RECEPTIONIST')")
    @Operation(summary = "Register a new patient")
    public ResponseEntity<ApiResponse<PatientResponse>> createPatient(
            @Valid @RequestBody CreatePatientRequest request) {
        PatientResponse response = patientService.createPatient(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Patient registered successfully", response));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','RECEPTIONIST','PSYCHIATRIST','PSYCHOLOGIST')")
    @Operation(summary = "Get patient by ID")
    public ResponseEntity<ApiResponse<PatientResponse>> getPatient(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(patientService.getPatientById(id)));
    }

    @GetMapping("/mrn/{mrn}")
    @PreAuthorize("hasAnyRole('ADMIN','RECEPTIONIST','PSYCHIATRIST','PSYCHOLOGIST')")
    @Operation(summary = "Get patient by MRN")
    public ResponseEntity<ApiResponse<PatientResponse>> getPatientByMrn(@PathVariable String mrn) {
        return ResponseEntity.ok(ApiResponse.ok(patientService.getPatientByMrn(mrn)));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','RECEPTIONIST','PSYCHIATRIST','PSYCHOLOGIST')")
    @Operation(summary = "List all patients with pagination")
    public ResponseEntity<ApiResponse<Page<PatientResponse>>> getAllPatients(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        return ResponseEntity.ok(ApiResponse.ok(patientService.getAllPatients(pageable)));
    }

    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('ADMIN','RECEPTIONIST','PSYCHIATRIST','PSYCHOLOGIST')")
    @Operation(summary = "Search patients by name, email, phone, or MRN")
    public ResponseEntity<ApiResponse<Page<PatientResponse>>> searchPatients(
            @RequestParam String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(ApiResponse.ok(patientService.searchPatients(q, pageable)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Soft-delete a patient record")
    public ResponseEntity<ApiResponse<Void>> deletePatient(@PathVariable UUID id) {
        patientService.softDeletePatient(id);
        return ResponseEntity.ok(ApiResponse.ok("Patient record deactivated", null));
    }
}
