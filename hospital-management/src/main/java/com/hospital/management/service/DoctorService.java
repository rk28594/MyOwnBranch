package com.hospital.management.service;

import com.hospital.management.dto.DoctorRequest;
import com.hospital.management.dto.DoctorResponse;
import com.hospital.management.entity.Doctor;
import com.hospital.management.exception.DuplicateResourceException;
import com.hospital.management.exception.ResourceNotFoundException;
import com.hospital.management.repository.DoctorRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Doctor Service - Story SCRUM-20: Doctor Profile Management
 * 
 * AC: Prevents duplicate licenseNumber
 * Test Scenario: When two doctors are registered with the same license, Then a 409 Conflict is returned
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DoctorService {

    private final DoctorRepository doctorRepository;

    /**
     * Create a new doctor
     * @param request the doctor details
     * @return created doctor response
     * @throws DuplicateResourceException if license number already exists
     */
    @Transactional
    public DoctorResponse createDoctor(DoctorRequest request) {
        log.info("Creating new doctor with license number: {}", request.getLicenseNumber());
        
        // Check for duplicate license number - Story SCRUM-20 AC
        if (doctorRepository.existsByLicenseNumber(request.getLicenseNumber())) {
            throw new DuplicateResourceException("Doctor", "licenseNumber", request.getLicenseNumber());
        }
        
        Doctor doctor = Doctor.builder()
                .fullName(request.getFullName())
                .licenseNumber(request.getLicenseNumber())
                .specialization(request.getSpecialization())
                .deptId(request.getDeptId())
                .build();
        
        Doctor savedDoctor = doctorRepository.save(doctor);
        log.info("Doctor created successfully with ID: {}", savedDoctor.getId());
        
        return mapToResponse(savedDoctor);
    }

    /**
     * Get doctor by ID
     * @param id the doctor ID
     * @return doctor response
     * @throws ResourceNotFoundException if doctor not found
     */
    @Transactional(readOnly = true)
    public DoctorResponse getDoctorById(Long id) {
        log.info("Fetching doctor with ID: {}", id);
        Doctor doctor = doctorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor", "id", id.toString()));
        return mapToResponse(doctor);
    }

    /**
     * Get all doctors
     * @return list of all doctors
     */
    @Transactional(readOnly = true)
    public List<DoctorResponse> getAllDoctors() {
        log.info("Fetching all doctors");
        return doctorRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Update an existing doctor
     * @param id the doctor ID
     * @param request the updated doctor details
     * @return updated doctor response
     */
    @Transactional
    public DoctorResponse updateDoctor(Long id, DoctorRequest request) {
        log.info("Updating doctor with ID: {}", id);
        
        Doctor doctor = doctorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor", "id", id.toString()));
        
        // Check for duplicate license number if changed
        if (!doctor.getLicenseNumber().equals(request.getLicenseNumber()) 
                && doctorRepository.existsByLicenseNumber(request.getLicenseNumber())) {
            throw new DuplicateResourceException("Doctor", "licenseNumber", request.getLicenseNumber());
        }
        
        doctor.setFullName(request.getFullName());
        doctor.setLicenseNumber(request.getLicenseNumber());
        doctor.setSpecialization(request.getSpecialization());
        doctor.setDeptId(request.getDeptId());
        
        Doctor updatedDoctor = doctorRepository.save(doctor);
        log.info("Doctor updated successfully with ID: {}", updatedDoctor.getId());
        
        return mapToResponse(updatedDoctor);
    }

    /**
     * Delete a doctor by ID
     * @param id the doctor ID
     */
    @Transactional
    public void deleteDoctor(Long id) {
        log.info("Deleting doctor with ID: {}", id);
        
        if (!doctorRepository.existsById(id)) {
            throw new ResourceNotFoundException("Doctor", "id", id.toString());
        }
        
        doctorRepository.deleteById(id);
        log.info("Doctor deleted successfully with ID: {}", id);
    }

    /**
     * Check if doctor exists by ID
     * @param id the doctor ID
     * @return true if doctor exists
     */
    public boolean existsById(Long id) {
        return doctorRepository.existsById(id);
    }

    private DoctorResponse mapToResponse(Doctor doctor) {
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
}
