package com.hospital.controller;

import com.hospital.model.Patient;
import com.hospital.service.PatientService;
import javax.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for Patient management
 * Provides endpoints for CRUD operations on patients
 */
@RestController
@RequestMapping("/api/v1/patients")
public class PatientController {

    @Autowired
    private PatientService patientService;

    /**
     * Get all patients
     * @return List of all patients
     */
    @GetMapping
    public ResponseEntity<List<Patient>> getAllPatients() {
        List<Patient> patients = patientService.getAllPatients();
        return ResponseEntity.ok(patients);
    }

    /**
     * Get patient by ID
     * @param id Patient ID (MRN)
     * @return Patient if found
     */
    @GetMapping("/{id}")
    public ResponseEntity<Patient> getPatientById(@PathVariable Long id) {
        return patientService.getPatientById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Create a new patient
     * @param patient Patient details
     * @return Created patient with generated ID
     */
    @PostMapping
    public ResponseEntity<Patient> createPatient(@Valid @RequestBody Patient patient) {
        Patient savedPatient = patientService.savePatient(patient);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedPatient);
    }

    /**
     * Update existing patient
     * @param id Patient ID
     * @param patientDetails Updated patient details
     * @return Updated patient
     */
    @PutMapping("/{id}")
    public ResponseEntity<Patient> updatePatient(@PathVariable Long id, 
                                                  @Valid @RequestBody Patient patientDetails) {
        return patientService.getPatientById(id)
                .map(patient -> {
                    patient.setFirstName(patientDetails.getFirstName());
                    patient.setLastName(patientDetails.getLastName());
                    patient.setDateOfBirth(patientDetails.getDateOfBirth());
                    patient.setGender(patientDetails.getGender());
                    patient.setDepartment(patientDetails.getDepartment());
                    patient.setIsCritical(patientDetails.getIsCritical());
                    Patient updatedPatient = patientService.savePatient(patient);
                    return ResponseEntity.ok(updatedPatient);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Delete patient
     * @param id Patient ID
     * @return 204 No Content if successful
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePatient(@PathVariable Long id) {
        if (patientService.getPatientById(id).isPresent()) {
            patientService.deletePatient(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    /**
     * Get all critical patients
     * @return List of critical patients
     */
    @GetMapping("/critical")
    public ResponseEntity<List<Patient>> getCriticalPatients() {
        List<Patient> criticalPatients = patientService.getCriticalPatients();
        return ResponseEntity.ok(criticalPatients);
    }

    /**
     * Get patients by department
     * @param department Department name
     * @return List of patients in the department
     */
    @GetMapping("/department/{department}")
    public ResponseEntity<List<Patient>> getPatientsByDepartment(@PathVariable String department) {
        List<Patient> patients = patientService.getPatientsByDepartment(department);
        return ResponseEntity.ok(patients);
    }
}
