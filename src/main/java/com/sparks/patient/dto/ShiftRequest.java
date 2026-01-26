package com.sparks.patient.dto;

import java.time.LocalTime;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for Shift creation/update request - SCRUM-18: Shift Definition & Time-Slot Logic
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Shift creation/update request")
public class ShiftRequest {

    @NotNull(message = "Doctor ID is required")
    @Schema(description = "Doctor's unique identifier", example = "1", required = true)
    private Long doctorId;

    @NotNull(message = "Start time is required")
    @Schema(description = "Shift start time", example = "09:00", required = true)
    private LocalTime startTime;

    @NotNull(message = "End time is required")
    @Schema(description = "Shift end time (must be strictly after start time)", example = "17:00", required = true)
    private LocalTime endTime;

    @NotBlank(message = "Room is required")
    @Size(min = 1, max = 50, message = "Room must be between 1 and 50 characters")
    @Schema(description = "Room assigned for the shift", example = "Room-101", required = true)
    private String room;
}
