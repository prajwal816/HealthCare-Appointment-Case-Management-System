package com.cimhans.dto.request;

import com.cimhans.domain.enums.AppointmentType;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

@Data
public class BookAppointmentRequest {

    @NotNull(message = "Patient ID is required")
    private UUID patientId;

    @NotNull(message = "Therapist ID is required")
    private UUID therapistId;

    @NotNull(message = "Slot ID is required")
    private UUID slotId;

    @NotNull(message = "Appointment date is required")
    @Future(message = "Appointment date must be in the future")
    private LocalDate appointmentDate;

    @NotNull(message = "Start time is required")
    private LocalTime startTime;

    @NotNull(message = "End time is required")
    private LocalTime endTime;

    @NotNull(message = "Appointment type is required")
    private AppointmentType type;

    @Size(max = 2000, message = "Reason must not exceed 2000 characters")
    private String reasonForVisit;

    @Size(max = 1000)
    private String notesForTherapist;
}
