package com.sparks.patient.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for Doctor creation request - SCRUM-20: Doctor Profile Management
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Doctor creation request")
public class DoctorRequest {

    @NotBlank(message = "Full name is required")
    @Size(min = 2, max = 100, message = "Full name must be between 2 and 100 characters")
    @Schema(description = "Doctor's full name", example = "Dr. John Smith", required = true)
    private String fullName;

    @NotBlank(message = "License number is required")
    @Size(min = 5, max = 50, message = "License number must be between 5 and 50 characters")
    @Schema(description = "Doctor's license number", example = "MED-123456", required = true)
    private String licenseNumber;

    @NotBlank(message = "Specialization is required")
    @Size(min = 2, max = 100, message = "Specialization must be between 2 and 100 characters")
    @Schema(description = "Doctor's specialization", example = "Cardiology", required = true)
    private String specialization;

    @NotNull(message = "Department ID is required")
    @Schema(description = "Department ID", example = "1", required = true)
    private Long deptId;
}
