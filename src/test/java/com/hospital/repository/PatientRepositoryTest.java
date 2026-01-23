package com.hospital.repository;

import com.hospital.model.Patient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Repository tests using @DataJpaTest
 * Verifies database operations with H2 in-memory database
 */
@DataJpaTest
@DisplayName("Patient Repository Tests")
class PatientRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private PatientRepository patientRepository;

    private Patient patient1;
    private Patient patient2;

    @BeforeEach
    void setUp() {
        patient1 = new Patient(
                "John",
                "Doe",
                LocalDate.of(1985, 5, 12),
                Patient.Gender.MALE,
                "Cardiology",
                false
        );
        patient1.setBloodGroup("O+");

        patient2 = new Patient(
                "Sarah",
                "Smith",
                LocalDate.of(1992, 10, 24),
                Patient.Gender.FEMALE,
                "Emergency",
                true
        );
        patient2.setBloodGroup("A-");
    }

    @Test
    @DisplayName("Should save and retrieve patient from H2 database")
    void testSaveAndRetrievePatient() {
        // Save patient
        Patient savedPatient = patientRepository.save(patient1);
        entityManager.flush();

        // Retrieve patient
        Optional<Patient> foundPatient = patientRepository.findById(savedPatient.getId());

        // Verify
        assertTrue(foundPatient.isPresent());
        assertEquals("John", foundPatient.get().getFirstName());
        assertEquals("Doe", foundPatient.get().getLastName());
        assertEquals("Cardiology", foundPatient.get().getDepartment());
        assertNotNull(foundPatient.get().getAdmittedDate());
    }

    @Test
    @DisplayName("Should find all patients")
    void testFindAllPatients() {
        patientRepository.save(patient1);
        patientRepository.save(patient2);
        entityManager.flush();

        List<Patient> patients = patientRepository.findAll();

        assertEquals(2, patients.size());
    }

    @Test
    @DisplayName("Should find patients by department")
    void testFindByDepartment() {
        patientRepository.save(patient1);
        patientRepository.save(patient2);
        entityManager.flush();

        List<Patient> cardiologyPatients = patientRepository.findByDepartment("Cardiology");

        assertEquals(1, cardiologyPatients.size());
        assertEquals("John", cardiologyPatients.get(0).getFirstName());
    }

    @Test
    @DisplayName("Should find critical patients")
    void testFindCriticalPatients() {
        patientRepository.save(patient1);
        patientRepository.save(patient2);
        entityManager.flush();

        List<Patient> criticalPatients = patientRepository.findByIsCriticalTrue();

        assertEquals(1, criticalPatients.size());
        assertEquals("Sarah", criticalPatients.get(0).getFirstName());
        assertTrue(criticalPatients.get(0).getIsCritical());
    }

    @Test
    @DisplayName("Should find patients by last name")
    void testFindByLastName() {
        patientRepository.save(patient1);
        patientRepository.save(patient2);
        entityManager.flush();

        List<Patient> smithPatients = patientRepository.findByLastName("Smith");

        assertEquals(1, smithPatients.size());
        assertEquals("Sarah", smithPatients.get(0).getFirstName());
    }

    @Test
    @DisplayName("Should find critical patients in specific department")
    void testFindCriticalPatientsByDepartment() {
        patientRepository.save(patient1);
        patientRepository.save(patient2);
        entityManager.flush();

        List<Patient> criticalEmergencyPatients = 
            patientRepository.findByDepartmentAndIsCriticalTrue("Emergency");

        assertEquals(1, criticalEmergencyPatients.size());
        assertEquals("Sarah", criticalEmergencyPatients.get(0).getFirstName());
    }

    @Test
    @DisplayName("Should delete patient from database")
    void testDeletePatient() {
        Patient savedPatient = patientRepository.save(patient1);
        entityManager.flush();

        Long patientId = savedPatient.getId();
        patientRepository.deleteById(patientId);
        entityManager.flush();

        Optional<Patient> deletedPatient = patientRepository.findById(patientId);
        assertFalse(deletedPatient.isPresent());
    }

    @Test
    @DisplayName("Should update patient information")
    void testUpdatePatient() {
        Patient savedPatient = patientRepository.save(patient1);
        entityManager.flush();

        savedPatient.setDepartment("Emergency");
        savedPatient.setIsCritical(true);
        patientRepository.save(savedPatient);
        entityManager.flush();

        Optional<Patient> updatedPatient = patientRepository.findById(savedPatient.getId());
        assertTrue(updatedPatient.isPresent());
        assertEquals("Emergency", updatedPatient.get().getDepartment());
        assertTrue(updatedPatient.get().getIsCritical());
    }

    @Test
    @DisplayName("Should auto-generate admitted date timestamp")
    void testAutoGenerateAdmittedDate() {
        Patient savedPatient = patientRepository.save(patient1);
        entityManager.flush();

        assertNotNull(savedPatient.getAdmittedDate());
    }

    @Test
    @DisplayName("Should return empty list when no patients match criteria")
    void testEmptyResultForNonExistentDepartment() {
        patientRepository.save(patient1);
        entityManager.flush();

        List<Patient> patients = patientRepository.findByDepartment("Neurology");

        assertTrue(patients.isEmpty());
    }

    @Test
    @DisplayName("Should save and retrieve patient with blood group")
    void testSaveAndRetrievePatientWithBloodGroup() {
        Patient savedPatient = patientRepository.save(patient1);
        entityManager.flush();

        Optional<Patient> foundPatient = patientRepository.findById(savedPatient.getId());

        assertTrue(foundPatient.isPresent());
        assertEquals("O+", foundPatient.get().getBloodGroup());
    }

    @Test
    @DisplayName("Should update patient blood group")
    void testUpdatePatientBloodGroup() {
        Patient savedPatient = patientRepository.save(patient1);
        entityManager.flush();

        savedPatient.setBloodGroup("AB+");
        patientRepository.save(savedPatient);
        entityManager.flush();

        Optional<Patient> updatedPatient = patientRepository.findById(savedPatient.getId());
        assertTrue(updatedPatient.isPresent());
        assertEquals("AB+", updatedPatient.get().getBloodGroup());
    }

    @Test
    @DisplayName("Should handle null blood group in database")
    void testNullBloodGroupInDatabase() {
        Patient patientWithoutBloodGroup = new Patient(
                "Michael",
                "Johnson",
                LocalDate.of(1978, 3, 15),
                Patient.Gender.MALE,
                "Pediatrics",
                false
        );
        
        Patient savedPatient = patientRepository.save(patientWithoutBloodGroup);
        entityManager.flush();

        Optional<Patient> foundPatient = patientRepository.findById(savedPatient.getId());
        assertTrue(foundPatient.isPresent());
        assertNull(foundPatient.get().getBloodGroup());
    }
}
