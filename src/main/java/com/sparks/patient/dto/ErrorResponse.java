package com.sparks.patient.dto;

import java.time.LocalDateTime;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Error response DTO for API errors
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Error response")
public class ErrorResponse {

    @Schema(description = "HTTP status code", example = "404")
    private int status;

    @Schema(description = "Error message", example = "Patient not found")
    private String message;

    @Schema(description = "Error timestamp")
    private LocalDateTime timestamp;

    @Schema(description = "Request path", example = "/api/v1/patients/999")
    private String path;

    @Schema(description = "Validation errors (if any)")
    private Map<String, String> errors;
}
