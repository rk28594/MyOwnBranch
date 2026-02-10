package com.sparks.patient.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.sparks.patient.entity.Patient;

/**
 * Patient Repository - Data access layer for Patient entity
 */
@Repository
public interface PatientRepository extends JpaRepository<Patient, Long> {

    /**
     * Find patient by email address
     * @param email the email to search for
     * @return Optional containing the patient if found
     */
    Optional<Patient> findByEmail(String email);

    /**
     * Check if a patient exists with the given email
     * @param email the email to check
     * @return true if exists, false otherwise
     */
    boolean existsByEmail(String email);

    /**
     * Find patient by phone number
     * @param phone the phone number to search for
     * @return Optional containing the patient if found
     */
    Optional<Patient> findByPhone(String phone);
}
