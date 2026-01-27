package com.sparks.patient.dto;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for Doctor response - SCRUM-20: Doctor Profile Management
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Doctor response")
public class DoctorResponse {

    @Schema(description = "Doctor ID", example = "1")
    private Long id;

    @Schema(description = "Doctor's full name", example = "Dr. John Smith")
    private String fullName;

    @Schema(description = "Doctor's license number", example = "MED-123456")
    private String licenseNumber;

    @Schema(description = "Doctor's specialization", example = "Cardiology")
    private String specialization;

    @Schema(description = "Department ID", example = "1")
    private Long deptId;

    @Schema(description = "Created timestamp")
    private LocalDateTime createdAt;

    @Schema(description = "Last updated timestamp")
    private LocalDateTime updatedAt;
}
