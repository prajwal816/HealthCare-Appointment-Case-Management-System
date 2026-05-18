package com.cimhans.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
public class SessionNoteResponse {
    private String id;
    private String appointmentId;
    private String patientId;
    private String patientName;
    private String therapistId;
    private String therapistName;
    private String content;           // Decrypted content — only returned to authorized caller
    private String followUpInstructions;
    private Integer moodRating;
    private String riskLevel;
    private boolean followUpRequired;
    private LocalDate followUpDate;
    private String diagnosisCodes;
    private String treatmentPlan;
    private boolean isFinalized;
    private LocalDate appointmentDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
