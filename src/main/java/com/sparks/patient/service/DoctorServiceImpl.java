package com.sparks.patient.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.sparks.patient.dto.DoctorRequest;
import com.sparks.patient.dto.DoctorResponse;
import com.sparks.patient.entity.Doctor;
import com.sparks.patient.exception.DoctorNotFoundException;
import com.sparks.patient.mapper.DoctorMapper;
import com.sparks.patient.repository.DoctorRepository;

/**
 * Service implementation for Doctor operations
 * SCRUM-20: Doctor Profile Management
 * 
 * Prevents duplicate licenseNumber registrations
 */
@Service
public class DoctorServiceImpl implements DoctorService {

    @Autowired
    private DoctorRepository doctorRepository;

    @Autowired
    private DoctorMapper doctorMapper;

    /**
     * Create a new doctor
     * Validates that the license number is unique
     * @param request the doctor creation request
     * @return the created doctor response
     * @throws IllegalArgumentException if license number already exists
     */
    @Override
    public DoctorResponse createDoctor(DoctorRequest request) {
        // Check for duplicate license number
        if (doctorRepository.existsByLicenseNumber(request.getLicenseNumber())) {
            throw new IllegalArgumentException(
                "Doctor with license number '" + request.getLicenseNumber() + "' already exists"
            );
        }

        Doctor doctor = doctorMapper.toEntity(request);
        Doctor savedDoctor = doctorRepository.save(doctor);
        return doctorMapper.toResponse(savedDoctor);
    }

    /**
     * Get doctor by ID
     * @param id the doctor ID
     * @return the doctor response
     */
    @Override
    public DoctorResponse getDoctorById(Long id) {
        Doctor doctor = doctorRepository.findById(id)
            .orElseThrow(() -> new DoctorNotFoundException("Doctor not found with id: " + id));
        return doctorMapper.toResponse(doctor);
    }

    /**
     * Get doctor by license number
     * @param licenseNumber the license number
     * @return the doctor response
     */
    @Override
    public DoctorResponse getDoctorByLicenseNumber(String licenseNumber) {
        Doctor doctor = doctorRepository.findByLicenseNumber(licenseNumber)
            .orElseThrow(() -> new DoctorNotFoundException(
                "Doctor not found with license number: " + licenseNumber
            ));
        return doctorMapper.toResponse(doctor);
    }

    /**
     * Get all doctors
     * @return list of all doctors
     */
    @Override
    public List<DoctorResponse> getAllDoctors() {
        List<Doctor> doctors = doctorRepository.findAll();
        return doctors.stream()
            .map(doctorMapper::toResponse)
            .collect(Collectors.toList());
    }

    /**
     * Update an existing doctor
     * Validates that the license number is unique if changed
     * @param id the doctor ID
     * @param request the update request
     * @return the updated doctor response
     * @throws IllegalArgumentException if license number already exists for another doctor
     */
    @Override
    public DoctorResponse updateDoctor(Long id, DoctorRequest request) {
        Doctor doctor = doctorRepository.findById(id)
            .orElseThrow(() -> new DoctorNotFoundException("Doctor not found with id: " + id));

        // Check if license number is being changed and if new license number already exists
        if (!doctor.getLicenseNumber().equals(request.getLicenseNumber()) &&
            doctorRepository.existsByLicenseNumber(request.getLicenseNumber())) {
            throw new IllegalArgumentException(
                "Doctor with license number '" + request.getLicenseNumber() + "' already exists"
            );
        }

        doctorMapper.updateEntity(request, doctor);
        Doctor updatedDoctor = doctorRepository.save(doctor);
        return doctorMapper.toResponse(updatedDoctor);
    }

    /**
     * Delete a doctor
     * @param id the doctor ID
     */
    @Override
    public void deleteDoctor(Long id) {
        Doctor doctor = doctorRepository.findById(id)
            .orElseThrow(() -> new DoctorNotFoundException("Doctor not found with id: " + id));
        doctorRepository.delete(doctor);
    }
}
