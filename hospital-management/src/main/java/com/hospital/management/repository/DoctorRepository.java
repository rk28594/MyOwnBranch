package com.hospital.management.repository;

import com.hospital.management.entity.Doctor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Doctor Repository - Story SCRUM-20: Doctor Profile Management
 * Provides CRUD operations for Doctor entity
 */
@Repository
public interface DoctorRepository extends JpaRepository<Doctor, Long> {
    
    /**
     * Find doctor by license number
     * @param licenseNumber the license number
     * @return Optional containing doctor if found
     */
    Optional<Doctor> findByLicenseNumber(String licenseNumber);
    
    /**
     * Check if doctor exists by license number
     * @param licenseNumber the license number
     * @return true if doctor exists
     */
    boolean existsByLicenseNumber(String licenseNumber);
}
