package com.sparks.patient.mapper;

import org.springframework.stereotype.Component;

import com.sparks.patient.dto.DoctorRequest;
import com.sparks.patient.dto.DoctorResponse;
import com.sparks.patient.entity.Doctor;

/**
 * Mapper for converting between Doctor entity and DTOs
 * SCRUM-20: Doctor Profile Management
 */
@Component
public class DoctorMapper {

    /**
     * Convert DoctorRequest DTO to Doctor entity
     */
    public Doctor toEntity(DoctorRequest request) {
        if (request == null) {
            return null;
        }
        
        return Doctor.builder()
                .fullName(request.getFullName())
                .licenseNumber(request.getLicenseNumber())
                .specialization(request.getSpecialization())
                .deptId(request.getDeptId())
                .build();
    }

    /**
     * Convert Doctor entity to DoctorResponse DTO
     */
    public DoctorResponse toResponse(Doctor doctor) {
        if (doctor == null) {
            return null;
        }
        
        return DoctorResponse.builder()
                .id(doctor.getId())
                .fullName(doctor.getFullName())
                .licenseNumber(doctor.getLicenseNumber())
                .specialization(doctor.getSpecialization())
                .deptId(doctor.getDeptId())
                .createdAt(doctor.getCreatedAt())
                .updatedAt(doctor.getUpdatedAt())
                .build();
    }

    /**
     * Update Doctor entity with data from DoctorRequest DTO
     */
    public void updateEntity(DoctorRequest request, Doctor doctor) {
        if (request == null || doctor == null) {
            return;
        }
        
        doctor.setFullName(request.getFullName());
        doctor.setLicenseNumber(request.getLicenseNumber());
        doctor.setSpecialization(request.getSpecialization());
        doctor.setDeptId(request.getDeptId());
    }
}
