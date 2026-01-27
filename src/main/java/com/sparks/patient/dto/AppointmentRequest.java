package com.sparks.patient.dto;

import java.time.LocalDateTime;

import javax.validation.constraints.NotNull;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for Appointment creation request - SCRUM-22: Appointment Completion & Status Update
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Appointment creation request")
public class AppointmentRequest {

    @NotNull(message = "Patient ID is required")
    @Schema(description = "Patient's unique identifier", example = "1", required = true)
    private Long patientId;

    @NotNull(message = "Doctor ID is required")
    @Schema(description = "Doctor's unique identifier", example = "1", required = true)
    private Long doctorId;

    @NotNull(message = "Shift ID is required")
    @Schema(description = "Shift's unique identifier", example = "1", required = true)
    private Long shiftId;

    @Schema(description = "Scheduled appointment time", example = "2026-01-27T14:00:00")
    private LocalDateTime scheduledAt;
}
