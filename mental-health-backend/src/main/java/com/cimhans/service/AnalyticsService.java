package com.cimhans.service;

import com.cimhans.domain.enums.AppointmentStatus;
import com.cimhans.domain.enums.Role;
import com.cimhans.dto.response.DashboardStatsResponse;
import com.cimhans.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class AnalyticsService {

    private final PatientRepository patientRepository;
    private final TherapistRepository therapistRepository;
    private final AppointmentRepository appointmentRepository;
    private final SessionNoteRepository sessionNoteRepository;

    @Transactional(readOnly = true)
    @Cacheable(value = "dashboard-stats", key = "'admin-stats'")
    public DashboardStatsResponse getAdminDashboardStats() {
        LocalDate today = LocalDate.now();
        LocalDate firstOfMonth = today.withDayOfMonth(1);
        LocalDateTime startOfMonth = firstOfMonth.atStartOfDay();
        LocalDateTime endOfMonth = today.atTime(23, 59, 59);

        long totalPatients = patientRepository.countActivePatients();
        long activePatients = patientRepository.countActivePatients();
        long totalTherapists = therapistRepository.countByDeletedAtIsNull();

        long apptToday = appointmentRepository.countByStatusAndDate(today);
        long completedToday = appointmentRepository.countCompletedOnDate(today);
        long pending = appointmentRepository.countByStatus(AppointmentStatus.SCHEDULED)
                     + appointmentRepository.countByStatus(AppointmentStatus.CONFIRMED);
        long cancelledMonth = appointmentRepository.countCancelledInPeriod(startOfMonth, endOfMonth);
        long completedMonth = appointmentRepository.countCompletedInPeriod(startOfMonth, endOfMonth);
        long noShowMonth = appointmentRepository.countNoShowInPeriod(startOfMonth, endOfMonth);

        long totalDone = completedMonth + cancelledMonth + noShowMonth;
        double completionRate = totalDone > 0 ? (completedMonth * 100.0 / totalDone) : 0.0;

        return DashboardStatsResponse.builder()
                .totalPatients(totalPatients)
                .activePatients(activePatients)
                .totalTherapists(totalTherapists)
                .totalAppointmentsToday(apptToday)
                .completedAppointmentsToday(completedToday)
                .pendingAppointments(pending)
                .cancelledAppointmentsThisMonth(cancelledMonth)
                .completedAppointmentsThisMonth(completedMonth)
                .noShowAppointmentsThisMonth(noShowMonth)
                .completionRatePercent(Math.round(completionRate * 100.0) / 100.0)
                .build();
    }
}
