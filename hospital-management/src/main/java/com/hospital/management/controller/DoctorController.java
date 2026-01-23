package com.hospital.management.controller;

import com.hospital.management.dto.DoctorRequest;
import com.hospital.management.dto.DoctorResponse;
import com.hospital.management.service.DoctorService;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Doctor REST Controller - Story SCRUM-20: Doctor Profile Management
 * 
 * Summary: CRUD operations for doctor identities
 * Fields: fullName, licenseNumber, specialization, deptId
 * AC: Prevents duplicate licenseNumber
 * Test Scenario: When two doctors are registered with the same license, Then a 409 Conflict is returned
 */
@RestController
@RequestMapping("/api/v1/doctors")
@RequiredArgsConstructor
@Slf4j
public class DoctorController {

    private final DoctorService doctorService;

    /**
     * Create a new doctor
     * @param request the doctor details
     * @return created doctor with 201 status
     */
    @PostMapping
    public ResponseEntity<DoctorResponse> createDoctor(@Valid @RequestBody DoctorRequest request) {
        log.info("REST request to create doctor: {}", request.getLicenseNumber());
        DoctorResponse response = doctorService.createDoctor(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /**
     * Get doctor by ID
     * @param id the doctor ID
     * @return doctor details or 404 if not found
     */
    @GetMapping("/{id}")
    public ResponseEntity<DoctorResponse> getDoctorById(@PathVariable Long id) {
        log.info("REST request to get doctor by ID: {}", id);
        DoctorResponse response = doctorService.getDoctorById(id);
        return ResponseEntity.ok(response);
    }

    /**
     * Get all doctors
     * @return list of all doctors
     */
    @GetMapping
    public ResponseEntity<List<DoctorResponse>> getAllDoctors() {
        log.info("REST request to get all doctors");
        List<DoctorResponse> responses = doctorService.getAllDoctors();
        return ResponseEntity.ok(responses);
    }

    /**
     * Update an existing doctor
     * @param id the doctor ID
     * @param request the updated doctor details
     * @return updated doctor details
     */
    @PutMapping("/{id}")
    public ResponseEntity<DoctorResponse> updateDoctor(
            @PathVariable Long id,
            @Valid @RequestBody DoctorRequest request) {
        log.info("REST request to update doctor with ID: {}", id);
        DoctorResponse response = doctorService.updateDoctor(id, request);
        return ResponseEntity.ok(response);
    }

    /**
     * Delete a doctor
     * @param id the doctor ID
     * @return 204 No Content on success
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDoctor(@PathVariable Long id) {
        log.info("REST request to delete doctor with ID: {}", id);
        doctorService.deleteDoctor(id);
        return ResponseEntity.noContent().build();
    }
}
