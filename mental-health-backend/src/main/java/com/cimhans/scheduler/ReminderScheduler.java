package com.cimhans.scheduler;

import com.cimhans.domain.entity.Appointment;
import com.cimhans.domain.enums.NotificationType;
import com.cimhans.repository.AppointmentRepository;
import com.cimhans.repository.RefreshTokenRepository;
import com.cimhans.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Scheduled jobs for automated reminders and housekeeping tasks.
 * All cron expressions are in standard 6-field Spring format.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ReminderScheduler {

    private final AppointmentRepository appointmentRepository;
    private final NotificationService notificationService;
    private final RefreshTokenRepository refreshTokenRepository;

    /**
     * Runs daily at 8:00 AM — sends appointment reminders for the next day.
     */
    @Scheduled(cron = "0 0 8 * * *")
    @Transactional
    public void sendAppointmentReminders() {
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        List<Appointment> appointments = appointmentRepository.findAppointmentsForReminder(tomorrow);

        log.info("Sending reminders for {} appointments on {}", appointments.size(), tomorrow);

        for (Appointment appt : appointments) {
            try {
                String patientName = appt.getPatient().getUser().getFirstName();
                String therapistName = appt.getTherapist().getUser().getFullName();

                notificationService.createNotification(
                        appt.getPatient().getUser(),
                        NotificationType.APPOINTMENT_REMINDER,
                        "Appointment Tomorrow",
                        String.format("Reminder: Your appointment with Dr. %s is tomorrow at %s.",
                                therapistName, appt.getStartTime()),
                        appt.getId(), "Appointment");

                notificationService.sendEmail(
                        appt.getPatient().getUser().getEmail(),
                        "Appointment Reminder - CIMHANS",
                        String.format("Dear %s,\n\nThis is a reminder for your appointment tomorrow (%s) at %s with Dr. %s.\n\nPlease arrive 10 minutes early.\n\nCIMHANS",
                                patientName, tomorrow, appt.getStartTime(), therapistName));

                appt.setReminderSent(true);
                appointmentRepository.save(appt);
            } catch (Exception e) {
                log.error("Failed to send reminder for appointment {}: {}", appt.getId(), e.getMessage());
            }
        }
    }

    /**
     * Runs daily at midnight — cleans up expired and revoked refresh tokens.
     */
    @Scheduled(cron = "0 0 0 * * *")
    @Transactional
    public void cleanupExpiredTokens() {
        int deleted = refreshTokenRepository.deleteExpiredAndRevoked(LocalDateTime.now().minusDays(1));
        log.info("Cleaned up {} expired/revoked refresh tokens", deleted);
    }
}
