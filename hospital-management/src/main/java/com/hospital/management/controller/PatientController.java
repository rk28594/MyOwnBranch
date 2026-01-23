package com.hospital.management.controller;

import com.hospital.management.dto.PatientRequest;
import com.hospital.management.dto.PatientResponse;
import com.hospital.management.service.PatientService;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Patient REST Controller - Implements Stories SCRUM-14 & SCRUM-15
 * 
 * Story SCRUM-14: Patient Onboarding API
 * AC: POST /api/v1/patients returns 201 Created; persists data
 * Test Scenario: Given valid JSON, When posted, Then the record is searchable in the database
 * 
 * Story SCRUM-15: Patient Search & Profile Retrieval
 * AC: GET /api/v1/patients/{id} returns the profile; returns 404 for invalid IDs
 * Test Scenario: When searching for a non-existent ID, Then a JSON error body is returned
 */
@RestController
@RequestMapping("/api/v1/patients")
@RequiredArgsConstructor
@Slf4j
public class PatientController {

    private final PatientService patientService;

    /**
     * Create a new patient - Story SCRUM-14
     * @param request the patient details
     * @return created patient with 201 status
     */
    @PostMapping
    public ResponseEntity<PatientResponse> createPatient(@Valid @RequestBody PatientRequest request) {
        log.info("REST request to create patient: {}", request.getEmail());
        PatientResponse response = patientService.createPatient(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /**
     * Get patient by ID - Story SCRUM-15
     * @param id the patient ID
     * @return patient details or 404 if not found
     */
    @GetMapping("/{id}")
    public ResponseEntity<PatientResponse> getPatientById(@PathVariable Long id) {
        log.info("REST request to get patient by ID: {}", id);
        PatientResponse response = patientService.getPatientById(id);
        return ResponseEntity.ok(response);
    }

    /**
     * Get all patients
     * @return list of all patients
     */
    @GetMapping
    public ResponseEntity<List<PatientResponse>> getAllPatients() {
        log.info("REST request to get all patients");
        List<PatientResponse> responses = patientService.getAllPatients();
        return ResponseEntity.ok(responses);
    }

    /**
     * Update patient
     * @param id the patient ID
     * @param request the updated patient details
     * @return updated patient details
     */
    @PutMapping("/{id}")
    public ResponseEntity<PatientResponse> updatePatient(
            @PathVariable Long id, 
            @Valid @RequestBody PatientRequest request) {
        log.info("REST request to update patient with ID: {}", id);
        PatientResponse response = patientService.updatePatient(id, request);
        return ResponseEntity.ok(response);
    }

    /**
     * Delete patient
     * @param id the patient ID
     * @return no content status
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePatient(@PathVariable Long id) {
        log.info("REST request to delete patient with ID: {}", id);
        patientService.deletePatient(id);
        return ResponseEntity.noContent().build();
    }
}
