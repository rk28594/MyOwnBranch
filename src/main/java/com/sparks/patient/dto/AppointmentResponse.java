package com.sparks.patient.dto;

import java.time.LocalDateTime;

import com.sparks.patient.entity.AppointmentStatus;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for Appointment response - SCRUM-22: Appointment Completion & Status Update
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Appointment response")
public class AppointmentResponse {

    @Schema(description = "Appointment ID", example = "1")
    private Long id;

    @Schema(description = "Patient's unique identifier", example = "1")
    private Long patientId;

    @Schema(description = "Doctor's unique identifier", example = "1")
    private Long doctorId;

    @Schema(description = "Shift's unique identifier", example = "1")
    private Long shiftId;

    @Schema(description = "Appointment status", example = "SCHEDULED")
    private AppointmentStatus status;

    @Schema(description = "Scheduled appointment time")
    private LocalDateTime scheduledAt;

    @Schema(description = "Completion timestamp (logged when status moves to COMPLETED)")
    private LocalDateTime completedAt;

    @Schema(description = "Appointment record creation timestamp")
    private LocalDateTime createdAt;

    @Schema(description = "Appointment record last update timestamp")
    private LocalDateTime updatedAt;
}
