package com.sparks.patient.service;

import java.util.List;

import com.sparks.patient.dto.DoctorRequest;
import com.sparks.patient.dto.DoctorResponse;

/**
 * Service interface for Doctor operations
 * SCRUM-20: Doctor Profile Management
 */
public interface DoctorService {

    /**
     * Create a new doctor
     * @param request the doctor creation request
     * @return the created doctor response
     * @throws IllegalArgumentException if license number already exists
     */
    DoctorResponse createDoctor(DoctorRequest request);

    /**
     * Get doctor by ID
     * @param id the doctor ID
     * @return the doctor response
     */
    DoctorResponse getDoctorById(Long id);

    /**
     * Get doctor by license number
     * @param licenseNumber the license number
     * @return the doctor response
     */
    DoctorResponse getDoctorByLicenseNumber(String licenseNumber);

    /**
     * Get all doctors
     * @return list of all doctors
     */
    List<DoctorResponse> getAllDoctors();

    /**
     * Update an existing doctor
     * @param id the doctor ID
     * @param request the update request
     * @return the updated doctor response
     * @throws IllegalArgumentException if license number already exists for another doctor
     */
    DoctorResponse updateDoctor(Long id, DoctorRequest request);

    /**
     * Delete a doctor
     * @param id the doctor ID
     */
    void deleteDoctor(Long id);
}
