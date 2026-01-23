package com.hospital.management.repository;

import com.hospital.management.entity.Patient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Patient Repository
 * Provides CRUD operations for Patient entity
 */
@Repository
public interface PatientRepository extends JpaRepository<Patient, Long> {
    
    /**
     * Find patient by email
     * @param email the email address
     * @return Optional containing patient if found
     */
    Optional<Patient> findByEmail(String email);
    
    /**
     * Check if patient exists by email
     * @param email the email address
     * @return true if patient exists
     */
    boolean existsByEmail(String email);
}
