package com.sparks.patient.mapper;

import org.springframework.stereotype.Component;

import com.sparks.patient.dto.PatientRequest;
import com.sparks.patient.dto.PatientResponse;
import com.sparks.patient.entity.Patient;

/**
 * Mapper for converting between Patient entity and DTOs
 */
@Component
public class PatientMapper {

    /**
     * Convert PatientRequest DTO to Patient entity
     */
    public Patient toEntity(PatientRequest request) {
        if (request == null) {
            return null;
        }
        
        return Patient.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .dob(request.getDob())
                .email(request.getEmail())
                .phone(request.getPhone())
                .build();
    }

    /**
     * Convert Patient entity to PatientResponse DTO
     */
    public PatientResponse toResponse(Patient patient) {
        if (patient == null) {
            return null;
        }
        
        return PatientResponse.builder()
                .id(patient.getId())
                .firstName(patient.getFirstName())
                .lastName(patient.getLastName())
                .dob(patient.getDob())
                .email(patient.getEmail())
                .phone(patient.getPhone())
                .createdAt(patient.getCreatedAt())
                .updatedAt(patient.getUpdatedAt())
                .build();
    }

    /**
     * Update existing Patient entity with data from PatientRequest
     */
    public void updateEntity(Patient patient, PatientRequest request) {
        if (patient == null || request == null) {
            return;
        }
        
        patient.setFirstName(request.getFirstName());
        patient.setLastName(request.getLastName());
        patient.setDob(request.getDob());
        patient.setEmail(request.getEmail());
        patient.setPhone(request.getPhone());
    }
}
