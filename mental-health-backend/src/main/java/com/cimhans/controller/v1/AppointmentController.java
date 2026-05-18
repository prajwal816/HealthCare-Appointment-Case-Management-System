package com.cimhans.controller.v1;

import com.cimhans.dto.request.BookAppointmentRequest;
import com.cimhans.dto.response.ApiResponse;
import com.cimhans.dto.response.AppointmentResponse;
import com.cimhans.service.AppointmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/appointments")
@RequiredArgsConstructor
@Tag(name = "Appointment Management", description = "Book, manage, and track appointments")
public class AppointmentController {

    private final AppointmentService appointmentService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','RECEPTIONIST','PATIENT')")
    @Operation(summary = "Book a new appointment")
    public ResponseEntity<ApiResponse<AppointmentResponse>> bookAppointment(
            @Valid @RequestBody BookAppointmentRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        UUID bookedBy = UUID.fromString(userDetails.getUsername());
        AppointmentResponse response = appointmentService.bookAppointment(request, bookedBy);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Appointment booked successfully", response));
    }

    @PatchMapping("/{id}/confirm")
    @PreAuthorize("hasAnyRole('ADMIN','RECEPTIONIST','PSYCHIATRIST','PSYCHOLOGIST')")
    @Operation(summary = "Confirm a scheduled appointment")
    public ResponseEntity<ApiResponse<AppointmentResponse>> confirmAppointment(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok("Appointment confirmed",
                appointmentService.confirmAppointment(id)));
    }

    @PatchMapping("/{id}/complete")
    @PreAuthorize("hasAnyRole('ADMIN','PSYCHIATRIST','PSYCHOLOGIST')")
    @Operation(summary = "Mark appointment as completed")
    public ResponseEntity<ApiResponse<AppointmentResponse>> completeAppointment(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok("Appointment completed",
                appointmentService.completeAppointment(id)));
    }

    @PatchMapping("/{id}/cancel")
    @PreAuthorize("hasAnyRole('ADMIN','RECEPTIONIST','PSYCHIATRIST','PSYCHOLOGIST','PATIENT')")
    @Operation(summary = "Cancel an appointment")
    public ResponseEntity<ApiResponse<AppointmentResponse>> cancelAppointment(
            @PathVariable UUID id,
            @RequestBody Map<String, String> body,
            @AuthenticationPrincipal UserDetails userDetails) {
        String reason = body.getOrDefault("reason", "Cancelled by user");
        UUID cancelledBy = UUID.fromString(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.ok("Appointment cancelled",
                appointmentService.cancelAppointment(id, reason, cancelledBy)));
    }

    @GetMapping("/patient/{patientId}")
    @PreAuthorize("hasAnyRole('ADMIN','RECEPTIONIST','PSYCHIATRIST','PSYCHOLOGIST','PATIENT')")
    @Operation(summary = "Get all appointments for a patient")
    public ResponseEntity<ApiResponse<Page<AppointmentResponse>>> getPatientAppointments(
            @PathVariable UUID patientId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(ApiResponse.ok(
                appointmentService.getAppointmentsForPatient(patientId, pageable)));
    }

    @GetMapping("/therapist/{therapistId}")
    @PreAuthorize("hasAnyRole('ADMIN','PSYCHIATRIST','PSYCHOLOGIST','RECEPTIONIST')")
    @Operation(summary = "Get all appointments for a therapist")
    public ResponseEntity<ApiResponse<Page<AppointmentResponse>>> getTherapistAppointments(
            @PathVariable UUID therapistId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(ApiResponse.ok(
                appointmentService.getAppointmentsForTherapist(therapistId, pageable)));
    }

    @GetMapping("/therapist/{therapistId}/schedule")
    @PreAuthorize("hasAnyRole('ADMIN','PSYCHIATRIST','PSYCHOLOGIST','RECEPTIONIST')")
    @Operation(summary = "Get therapist daily schedule")
    public ResponseEntity<ApiResponse<List<AppointmentResponse>>> getDailySchedule(
            @PathVariable UUID therapistId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(ApiResponse.ok(
                appointmentService.getTherapistDailySchedule(therapistId, date)));
    }
}
