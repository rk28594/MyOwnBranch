package com.hospital.management.service;

import com.hospital.management.dto.PatientRequest;
import com.hospital.management.dto.PatientResponse;
import com.hospital.management.entity.Patient;
import com.hospital.management.exception.DuplicateResourceException;
import com.hospital.management.exception.ResourceNotFoundException;
import com.hospital.management.repository.PatientRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Patient Service - Implements Stories SCRUM-14 & SCRUM-15
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PatientService {

    private final PatientRepository patientRepository;

    /**
     * Create a new patient - Story SCRUM-14: Patient Onboarding API
     * AC: POST /api/v1/patients returns 201 Created; persists data
     */
    @Transactional
    public PatientResponse createPatient(PatientRequest request) {
        log.info("Creating new patient with email: {}", request.getEmail());
        
        // Check for duplicate email
        if (patientRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Patient", "email", request.getEmail());
        }
        
        Patient patient = Patient.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .dob(request.getDob())
                .email(request.getEmail())
                .phone(request.getPhone())
                .build();
        
        Patient savedPatient = patientRepository.save(patient);
        log.info("Patient created successfully with ID: {}", savedPatient.getId());
        
        return mapToResponse(savedPatient);
    }

    /**
     * Get patient by ID - Story SCRUM-15: Patient Search & Profile Retrieval
     * AC: GET /api/v1/patients/{id} returns the profile; returns 404 for invalid IDs
     */
    @Transactional(readOnly = true)
    public PatientResponse getPatientById(Long id) {
        log.info("Fetching patient with ID: {}", id);
        
        Patient patient = patientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Patient", "id", id));
        
        return mapToResponse(patient);
    }

    /**
     * Get all patients
     */
    @Transactional(readOnly = true)
    public List<PatientResponse> getAllPatients() {
        log.info("Fetching all patients");
        
        return patientRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Update patient
     */
    @Transactional
    public PatientResponse updatePatient(Long id, PatientRequest request) {
        log.info("Updating patient with ID: {}", id);
        
        Patient patient = patientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Patient", "id", id));
        
        // Check if email is being changed and if it's already taken
        if (!patient.getEmail().equals(request.getEmail()) && 
            patientRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Patient", "email", request.getEmail());
        }
        
        patient.setFirstName(request.getFirstName());
        patient.setLastName(request.getLastName());
        patient.setDob(request.getDob());
        patient.setEmail(request.getEmail());
        patient.setPhone(request.getPhone());
        
        Patient updatedPatient = patientRepository.save(patient);
        log.info("Patient updated successfully with ID: {}", updatedPatient.getId());
        
        return mapToResponse(updatedPatient);
    }

    /**
     * Delete patient
     */
    @Transactional
    public void deletePatient(Long id) {
        log.info("Deleting patient with ID: {}", id);
        
        if (!patientRepository.existsById(id)) {
            throw new ResourceNotFoundException("Patient", "id", id);
        }
        
        patientRepository.deleteById(id);
        log.info("Patient deleted successfully with ID: {}", id);
    }

    /**
     * Map Patient entity to PatientResponse DTO
     */
    private PatientResponse mapToResponse(Patient patient) {
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
}
