package com.sparks.patient.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for Patient response - SCRUM-15: Patient Search & Profile Retrieval
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Patient profile response")
public class PatientResponse {

    @Schema(description = "Patient ID", example = "1")
    private Long id;

    @Schema(description = "Patient's first name", example = "John")
    private String firstName;

    @Schema(description = "Patient's last name", example = "Doe")
    private String lastName;

    @Schema(description = "Patient's date of birth", example = "1990-05-15")
    private LocalDate dob;

    @Schema(description = "Patient's email address", example = "john.doe@example.com")
    private String email;

    @Schema(description = "Patient's phone number", example = "+1234567890")
    private String phone;

    @Schema(description = "Patient record creation timestamp")
    private LocalDateTime createdAt;

    @Schema(description = "Patient record last update timestamp")
    private LocalDateTime updatedAt;
}
