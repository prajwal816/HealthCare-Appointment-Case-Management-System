package com.cimhans.dto.response;

import com.cimhans.domain.enums.AppointmentStatus;
import com.cimhans.domain.enums.AppointmentType;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
@Builder
public class AppointmentResponse {
    private String id;
    private String patientId;
    private String patientName;
    private String patientMrn;
    private String therapistId;
    private String therapistName;
    private String therapistSpecialization;
    private LocalDate appointmentDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private AppointmentType type;
    private AppointmentStatus status;
    private String reasonForVisit;
    private String cancellationReason;
    private boolean hasSessionNote;
    private boolean reminderSent;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
