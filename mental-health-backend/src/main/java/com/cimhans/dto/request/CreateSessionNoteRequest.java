package com.cimhans.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;

@Data
public class CreateSessionNoteRequest {

    @NotBlank(message = "Session note content is required")
    @Size(min = 10, max = 10000)
    private String content;

    @Size(max = 2000)
    private String followUpInstructions;

    @Min(1) @Max(10)
    private Integer moodRating;

    @Pattern(regexp = "LOW|MODERATE|HIGH|CRITICAL", message = "Invalid risk level")
    private String riskLevel;

    private boolean followUpRequired;

    private LocalDate followUpDate;

    @Size(max = 500)
    private String diagnosisCodes;

    @Size(max = 5000)
    private String treatmentPlan;
}
