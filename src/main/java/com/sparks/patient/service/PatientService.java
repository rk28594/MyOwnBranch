package com.sparks.patient.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.sparks.patient.dto.PatientRequest;
import com.sparks.patient.dto.PatientResponse;

/**
 * Service interface for Patient operations
 */
public interface PatientService {

    /**
     * Create a new patient - SCRUM-14: Patient Onboarding API
     * @param request the patient creation request
     * @return the created patient response
     */
    PatientResponse createPatient(PatientRequest request);

    /**
     * Get patient by ID - SCRUM-15: Patient Search & Profile Retrieval
     * @param id the patient ID
     * @return the patient response
     */
    PatientResponse getPatientById(Long id);

    /**
     * Get all patients with pagination
     * @param pageable pagination parameters (page, size, sort)
     * @return paginated list of patients
     */
    Page<PatientResponse> getAllPatients(Pageable pageable);

    /**
     * Update an existing patient
     * @param id the patient ID
     * @param request the update request
     * @return the updated patient response
     */
    PatientResponse updatePatient(Long id, PatientRequest request);

    /**
     * Delete a patient
     * @param id the patient ID
     */
    void deletePatient(Long id);

    /**
     * Search for a patient by phone number
     * @param phone the phone number to search for
     * @return the patient response
     */
    PatientResponse getPatientByPhone(String phone);

    /**
     * Search for patients by last name
     * @param lastName the last name to search for
     * @return list of matching patients
     */
    List<PatientResponse> getPatientsByLastName(String lastName);
}
