package com.cimhans.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DashboardStatsResponse {
    private long totalPatients;
    private long activePatients;
    private long totalTherapists;
    private long totalAppointmentsToday;
    private long completedAppointmentsToday;
    private long pendingAppointments;
    private long cancelledAppointmentsThisMonth;
    private long completedAppointmentsThisMonth;
    private long noShowAppointmentsThisMonth;
    private double completionRatePercent;
    private long totalSessionNotes;
    private long pendingFollowUps;
    private long unreadNotifications;
}
