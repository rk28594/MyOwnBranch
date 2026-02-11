package com.sparks.patient.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sparks.patient.dto.PatientRequest;
import com.sparks.patient.dto.PatientResponse;
import com.sparks.patient.entity.Patient;
import com.sparks.patient.exception.DuplicateEmailException;
import com.sparks.patient.exception.PatientNotFoundException;
import com.sparks.patient.mapper.PatientMapper;
import com.sparks.patient.repository.PatientRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Implementation of PatientService
 * Handles all patient-related business logic
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class PatientServiceImpl implements PatientService {

    private final PatientRepository patientRepository;
    private final PatientMapper patientMapper;

    /**
     * Create a new patient - SCRUM-14: Patient Onboarding API
     * POST /api/v1/patients returns 201 Created
     * Persists data to H2 database
     */
    @Override
    public PatientResponse createPatient(PatientRequest request) {
        log.info("Creating new patient with email: {}", request.getEmail());
        
        // Check for duplicate email
        if (patientRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateEmailException(request.getEmail());
        }
        
        Patient patient = patientMapper.toEntity(request);
        Patient savedPatient = patientRepository.save(patient);
        
        log.info("Patient created successfully with ID: {}", savedPatient.getId());
        return patientMapper.toResponse(savedPatient);
    }

    /**
     * Get patient by ID - SCRUM-15: Patient Search & Profile Retrieval
     * GET /api/v1/patients/{id} returns the profile
     * Returns 404 for invalid IDs
     */
    @Override
    @Transactional(readOnly = true)
    public PatientResponse getPatientById(Long id) {
        log.info("Fetching patient with ID: {}", id);
        
        Patient patient = patientRepository.findById(id)
                .orElseThrow(() -> new PatientNotFoundException(id));
        
        return patientMapper.toResponse(patient);
    }

    /**
     * Get all patients with pagination
     */
    @Override
    @Transactional(readOnly = true)
    public Page<PatientResponse> getAllPatients(Pageable pageable) {
        log.info("Fetching patients - page: {}, size: {}",
                pageable.getPageNumber(), pageable.getPageSize());

        return patientRepository.findAll(pageable)
                .map(patientMapper::toResponse);
    }

    /**
     * Update an existing patient
     */
    @Override
    public PatientResponse updatePatient(Long id, PatientRequest request) {
        log.info("Updating patient with ID: {}", id);
        
        Patient patient = patientRepository.findById(id)
                .orElseThrow(() -> new PatientNotFoundException(id));
        
        // Check for email conflict (if email is being changed)
        if (!patient.getEmail().equals(request.getEmail())
                && patientRepository.existsByEmail(request.getEmail())) {
            log.warn("Duplicate email detected - Patient ID: {}, Old email: {}, Attempted new email: {}",
                    id, patient.getEmail(), request.getEmail());
            throw new DuplicateEmailException(request.getEmail());
        }
        
        patientMapper.updateEntity(patient, request);
        Patient updatedPatient = patientRepository.save(patient);
        
        log.info("Patient updated successfully with ID: {}", updatedPatient.getId());
        return patientMapper.toResponse(updatedPatient);
    }

    /**
     * Delete a patient
     */
    @Override
    public void deletePatient(Long id) {
        log.info("Deleting patient with ID: {}", id);

        if (!patientRepository.existsById(id)) {
            throw new PatientNotFoundException(id);
        }

        patientRepository.deleteById(id);
        log.info("Patient deleted successfully with ID: {}", id);
    }

    /**
     * Search for a patient by phone number
     */
    @Override
    @Transactional(readOnly = true)
    public PatientResponse getPatientByPhone(String phone) {
        log.info("Searching for patient with phone: {}", phone);

        Patient patient = patientRepository.findByPhone(phone)
                .orElseThrow(() -> new PatientNotFoundException("Patient not found with phone: " + phone));

        return patientMapper.toResponse(patient);
    }

    /**
     * Search for patients by last name
     */
    @Override
    @Transactional(readOnly = true)
    public List<PatientResponse> getPatientsByLastName(String lastName) {
        log.info("Searching for patients with last name: {}", lastName);

        List<Patient> patients = patientRepository.findByLastName(lastName);

        if (patients.isEmpty()) {
            log.info("No patients found with last name: {}", lastName);
        } else {
            log.info("Found {} patient(s) with last name: {}", patients.size(), lastName);
        }

        return patients.stream()
                .map(patientMapper::toResponse)
                .collect(Collectors.toList());
    }
}
