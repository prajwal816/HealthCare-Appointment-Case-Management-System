package com.cimhans.service;

import com.cimhans.domain.entity.*;
import com.cimhans.domain.enums.AppointmentStatus;
import com.cimhans.domain.enums.NotificationType;
import com.cimhans.dto.request.BookAppointmentRequest;
import com.cimhans.dto.response.AppointmentResponse;
import com.cimhans.exception.BusinessException;
import com.cimhans.exception.ResourceNotFoundException;
import com.cimhans.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final PatientRepository patientRepository;
    private final TherapistRepository therapistRepository;
    private final AppointmentSlotRepository slotRepository;
    private final NotificationService notificationService;
    private final AuditService auditService;

    @Transactional
    public AppointmentResponse bookAppointment(BookAppointmentRequest request, UUID bookedByUserId) {
        Patient patient = patientRepository.findById(request.getPatientId())
                .filter(p -> p.getDeletedAt() == null)
                .orElseThrow(() -> new ResourceNotFoundException("Patient", "id", request.getPatientId()));

        Therapist therapist = therapistRepository.findById(request.getTherapistId())
                .filter(t -> t.getDeletedAt() == null)
                .orElseThrow(() -> new ResourceNotFoundException("Therapist", "id", request.getTherapistId()));

        if (!therapist.isAvailable()) {
            throw new BusinessException("Therapist is currently not accepting appointments");
        }

        AppointmentSlot slot = slotRepository.findById(request.getSlotId())
                .orElseThrow(() -> new ResourceNotFoundException("Slot", "id", request.getSlotId()));

        if (slot.isBooked()) {
            throw new BusinessException("This slot is already booked. Please choose another time.");
        }

        // Mark slot as booked
        slot.setBooked(true);
        slotRepository.save(slot);

        Appointment appointment = Appointment.builder()
                .patient(patient)
                .therapist(therapist)
                .slot(slot)
                .appointmentDate(request.getAppointmentDate())
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .type(request.getType())
                .status(AppointmentStatus.SCHEDULED)
                .reasonForVisit(request.getReasonForVisit())
                .notesForTherapist(request.getNotesForTherapist())
                .bookedByUserId(bookedByUserId)
                .build();

        appointment = appointmentRepository.save(appointment);

        // Notify patient
        notificationService.createNotification(patient.getUser(), NotificationType.APPOINTMENT_CONFIRMED,
                "Appointment Scheduled",
                String.format("Your appointment with Dr. %s on %s at %s has been scheduled.",
                        therapist.getUser().getFullName(), request.getAppointmentDate(), request.getStartTime()),
                appointment.getId(), "Appointment");

        // Send email async
        notificationService.sendEmail(patient.getUser().getEmail(),
                "Appointment Confirmation - CIMHANS",
                buildConfirmationEmail(appointment, patient, therapist));

        auditService.logSuccess("APPOINTMENT_BOOKED", "Appointment", appointment.getId(),
                "Patient: " + patient.getMrn() + " | Therapist: " + therapist.getLicenseNumber());

        return toResponse(appointment);
    }

    @Transactional
    public AppointmentResponse cancelAppointment(UUID appointmentId, String reason, UUID cancelledByUserId) {
        Appointment appointment = appointmentRepository.findByIdAndDeletedAtIsNull(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment", "id", appointmentId));

        if (appointment.getStatus() == AppointmentStatus.COMPLETED) {
            throw new BusinessException("Cannot cancel a completed appointment");
        }
        if (appointment.getStatus() == AppointmentStatus.CANCELLED) {
            throw new BusinessException("Appointment is already cancelled");
        }

        appointment.setStatus(AppointmentStatus.CANCELLED);
        appointment.setCancellationReason(reason);

        // Free the slot
        if (appointment.getSlot() != null) {
            appointment.getSlot().setBooked(false);
            slotRepository.save(appointment.getSlot());
        }

        appointment = appointmentRepository.save(appointment);

        notificationService.createNotification(appointment.getPatient().getUser(),
                NotificationType.APPOINTMENT_CANCELLED,
                "Appointment Cancelled",
                "Your appointment on " + appointment.getAppointmentDate() + " has been cancelled. Reason: " + reason,
                appointment.getId(), "Appointment");

        auditService.logSuccess("APPOINTMENT_CANCELLED", "Appointment", appointmentId, "Reason: " + reason);
        return toResponse(appointment);
    }

    @Transactional
    public AppointmentResponse confirmAppointment(UUID appointmentId) {
        Appointment appointment = appointmentRepository.findByIdAndDeletedAtIsNull(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment", "id", appointmentId));

        if (appointment.getStatus() != AppointmentStatus.SCHEDULED) {
            throw new BusinessException("Only SCHEDULED appointments can be confirmed");
        }

        appointment.setStatus(AppointmentStatus.CONFIRMED);
        appointment = appointmentRepository.save(appointment);
        auditService.logSuccess("APPOINTMENT_CONFIRMED", "Appointment", appointmentId, "");
        return toResponse(appointment);
    }

    @Transactional
    public AppointmentResponse completeAppointment(UUID appointmentId) {
        Appointment appointment = appointmentRepository.findByIdAndDeletedAtIsNull(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment", "id", appointmentId));

        if (appointment.getStatus() != AppointmentStatus.CONFIRMED) {
            throw new BusinessException("Only CONFIRMED appointments can be marked as completed");
        }

        appointment.setStatus(AppointmentStatus.COMPLETED);
        appointment = appointmentRepository.save(appointment);
        auditService.logSuccess("APPOINTMENT_COMPLETED", "Appointment", appointmentId, "");
        return toResponse(appointment);
    }

    @Transactional(readOnly = true)
    public Page<AppointmentResponse> getAppointmentsForPatient(UUID patientId, Pageable pageable) {
        return appointmentRepository.findByPatientIdAndDeletedAtIsNull(patientId, pageable)
                .map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<AppointmentResponse> getAppointmentsForTherapist(UUID therapistId, Pageable pageable) {
        return appointmentRepository.findByTherapistIdAndDeletedAtIsNull(therapistId, pageable)
                .map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public List<AppointmentResponse> getTherapistDailySchedule(UUID therapistId, LocalDate date) {
        return appointmentRepository.findTherapistScheduleForDate(therapistId, date)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    private String buildConfirmationEmail(Appointment a, Patient p, Therapist t) {
        return String.format("""
            Dear %s,
            
            Your appointment has been successfully scheduled.
            
            Details:
            - Doctor: Dr. %s (%s)
            - Date: %s
            - Time: %s - %s
            - Type: %s
            
            Please arrive 10 minutes early.
            
            CIMHANS Mental Health Centre
            """,
                p.getUser().getFirstName(),
                t.getUser().getFullName(), t.getSpecialization(),
                a.getAppointmentDate(), a.getStartTime(), a.getEndTime(),
                a.getType().name().replace("_", " "));
    }

    public AppointmentResponse toResponse(Appointment a) {
        boolean hasNote = a.getSessionNote() != null;
        return AppointmentResponse.builder()
                .id(a.getId().toString())
                .patientId(a.getPatient().getId().toString())
                .patientName(a.getPatient().getUser().getFullName())
                .patientMrn(a.getPatient().getMrn())
                .therapistId(a.getTherapist().getId().toString())
                .therapistName(a.getTherapist().getUser().getFullName())
                .therapistSpecialization(a.getTherapist().getSpecialization().name())
                .appointmentDate(a.getAppointmentDate())
                .startTime(a.getStartTime())
                .endTime(a.getEndTime())
                .type(a.getType())
                .status(a.getStatus())
                .reasonForVisit(a.getReasonForVisit())
                .cancellationReason(a.getCancellationReason())
                .hasSessionNote(hasNote)
                .reminderSent(a.isReminderSent())
                .createdAt(a.getCreatedAt())
                .updatedAt(a.getUpdatedAt())
                .build();
    }
}
