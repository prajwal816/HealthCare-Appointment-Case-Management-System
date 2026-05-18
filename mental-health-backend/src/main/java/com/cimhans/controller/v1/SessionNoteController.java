package com.cimhans.controller.v1;

import com.cimhans.dto.request.CreateSessionNoteRequest;
import com.cimhans.dto.response.ApiResponse;
import com.cimhans.dto.response.SessionNoteResponse;
import com.cimhans.service.SessionNoteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/session-notes")
@RequiredArgsConstructor
@Tag(name = "Session Notes", description = "Confidential encrypted clinical session notes")
public class SessionNoteController {

    private final SessionNoteService sessionNoteService;

    @PostMapping("/appointment/{appointmentId}")
    @PreAuthorize("hasAnyRole('PSYCHIATRIST','PSYCHOLOGIST')")
    @Operation(summary = "Create a session note for an appointment (therapist only)")
    public ResponseEntity<ApiResponse<SessionNoteResponse>> createNote(
            @PathVariable UUID appointmentId,
            @Valid @RequestBody CreateSessionNoteRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        UUID therapistUserId = UUID.fromString(userDetails.getUsername());
        SessionNoteResponse response = sessionNoteService.createNote(appointmentId, request, therapistUserId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Session note created", response));
    }

    @GetMapping("/appointment/{appointmentId}")
    @PreAuthorize("hasAnyRole('ADMIN','PSYCHIATRIST','PSYCHOLOGIST')")
    @Operation(summary = "Get session note for an appointment")
    public ResponseEntity<ApiResponse<SessionNoteResponse>> getNoteByAppointment(
            @PathVariable UUID appointmentId,
            @AuthenticationPrincipal UserDetails userDetails) {
        UUID userId = UUID.fromString(userDetails.getUsername());
        String role = userDetails.getAuthorities().iterator().next().getAuthority();
        return ResponseEntity.ok(ApiResponse.ok(
                sessionNoteService.getNoteByAppointment(appointmentId, userId, role)));
    }

    @GetMapping("/patient/{patientId}")
    @PreAuthorize("hasAnyRole('ADMIN','PSYCHIATRIST','PSYCHOLOGIST')")
    @Operation(summary = "Get all session notes for a patient")
    public ResponseEntity<ApiResponse<Page<SessionNoteResponse>>> getPatientNotes(
            @PathVariable UUID patientId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal UserDetails userDetails) {
        UUID userId = UUID.fromString(userDetails.getUsername());
        String role = userDetails.getAuthorities().iterator().next().getAuthority();
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(ApiResponse.ok(
                sessionNoteService.getNotesForPatient(patientId, userId, role, pageable)));
    }

    @PatchMapping("/{noteId}/finalize")
    @PreAuthorize("hasAnyRole('PSYCHIATRIST','PSYCHOLOGIST')")
    @Operation(summary = "Finalize (lock) a session note")
    public ResponseEntity<ApiResponse<SessionNoteResponse>> finalizeNote(
            @PathVariable UUID noteId,
            @AuthenticationPrincipal UserDetails userDetails) {
        UUID therapistUserId = UUID.fromString(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.ok("Note finalized",
                sessionNoteService.finalizeNote(noteId, therapistUserId)));
    }
}
