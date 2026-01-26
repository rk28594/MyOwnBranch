package com.sparks.patient.dto;

import java.time.LocalDateTime;
import java.time.LocalTime;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for Shift response - SCRUM-18: Shift Definition & Time-Slot Logic
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Shift response")
public class ShiftResponse {

    @Schema(description = "Shift ID", example = "1")
    private Long id;

    @Schema(description = "Doctor's unique identifier", example = "1")
    private Long doctorId;

    @Schema(description = "Shift start time", example = "09:00")
    private LocalTime startTime;

    @Schema(description = "Shift end time", example = "17:00")
    private LocalTime endTime;

    @Schema(description = "Room assigned for the shift", example = "Room-101")
    private String room;

    @Schema(description = "Shift record creation timestamp")
    private LocalDateTime createdAt;

    @Schema(description = "Shift record last update timestamp")
    private LocalDateTime updatedAt;
}
