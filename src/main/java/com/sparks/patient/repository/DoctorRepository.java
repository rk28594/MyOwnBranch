package com.sparks.patient.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.sparks.patient.entity.Doctor;

/**
 * Doctor Repository - Data access layer for Doctor entity
 * SCRUM-20: Doctor Profile Management
 */
@Repository
public interface DoctorRepository extends JpaRepository<Doctor, Long> {

    /**
     * Find doctor by license number
     * @param licenseNumber the license number to search for
     * @return Optional containing the doctor if found
     */
    Optional<Doctor> findByLicenseNumber(String licenseNumber);

    /**
     * Check if doctor exists by license number
     * @param licenseNumber the license number to check
     * @return true if doctor exists, false otherwise
     */
    boolean existsByLicenseNumber(String licenseNumber);
}
