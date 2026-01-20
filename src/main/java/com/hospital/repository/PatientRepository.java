package com.hospital.repository;

import com.hospital.model.Patient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository interface for Patient entity.
 * Provides CRUD operations and custom query methods.
 */
@Repository
public interface PatientRepository extends JpaRepository<Patient, Long> {

    /**
     * Find patients by department
     */
    List<Patient> findByDepartment(String department);

    /**
     * Find critical patients
     */
    List<Patient> findByIsCriticalTrue();

    /**
     * Find patients by last name
     */
    List<Patient> findByLastName(String lastName);

    /**
     * Find critical patients in a specific department
     */
    List<Patient> findByDepartmentAndIsCriticalTrue(String department);
}
