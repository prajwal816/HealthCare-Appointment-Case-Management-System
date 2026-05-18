package com.cimhans.controller.v1;

import com.cimhans.dto.response.ApiResponse;
import com.cimhans.dto.response.DashboardStatsResponse;
import com.cimhans.service.AnalyticsService;
import com.cimhans.service.PatientService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin Dashboard", description = "System-wide analytics and administration")
public class AdminController {

    private final AnalyticsService analyticsService;

    @GetMapping("/dashboard/stats")
    @Operation(summary = "Get system-wide dashboard statistics")
    public ResponseEntity<ApiResponse<DashboardStatsResponse>> getDashboardStats() {
        return ResponseEntity.ok(ApiResponse.ok(analyticsService.getAdminDashboardStats()));
    }
}
