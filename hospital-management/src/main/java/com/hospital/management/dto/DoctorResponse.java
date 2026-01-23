package com.hospital.management.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * DTO for Doctor responses - Story SCRUM-20
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DoctorResponse {

    private Long id;
    private String fullName;
    private String licenseNumber;
    private String specialization;
    private Long deptId;
    private LocalDate createdAt;
    private LocalDate updatedAt;
}
