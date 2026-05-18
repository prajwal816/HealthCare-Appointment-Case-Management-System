package com.cimhans.controller.v1;

import com.cimhans.domain.entity.Therapist;
import com.cimhans.dto.response.ApiResponse;
import com.cimhans.repository.TherapistRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/therapists")
@RequiredArgsConstructor
@Tag(name = "Therapists", description = "Therapist profiles and availability")
public class TherapistController {

    private final TherapistRepository therapistRepository;

    @GetMapping("/available")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get all available therapists")
    public ResponseEntity<ApiResponse<List<Therapist>>> getAvailableTherapists() {
        List<Therapist> therapists = therapistRepository.findAvailableTherapists();
        return ResponseEntity.ok(ApiResponse.ok(therapists));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get therapist by ID")
    public ResponseEntity<ApiResponse<Therapist>> getTherapistById(@PathVariable UUID id) {
        return therapistRepository.findById(id)
                .map(t -> ResponseEntity.ok(ApiResponse.ok(t)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PatchMapping("/{id}/availability")
    @PreAuthorize("hasAnyRole('ADMIN','PSYCHIATRIST','PSYCHOLOGIST')")
    @Operation(summary = "Toggle therapist availability")
    public ResponseEntity<ApiResponse<Void>> toggleAvailability(
            @PathVariable UUID id,
            @RequestParam boolean available) {
        therapistRepository.findById(id).ifPresent(t -> {
            t.setAvailable(available);
            therapistRepository.save(t);
        });
        return ResponseEntity.ok(ApiResponse.ok("Availability updated", null));
    }
}
