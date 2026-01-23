package com.hospital.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for Patient model
 * Tests data validation and business logic
 */
@DisplayName("Patient Model Tests")
class PatientTest {

    private Patient patient;

    @BeforeEach
    void setUp() {
        patient = new Patient(
                "John",
                "Doe",
                LocalDate.of(1985, 5, 12),
                Patient.Gender.MALE,
                "Cardiology",
                false
        );
        patient.setBloodGroup("O+");
    }

    @Test
    @DisplayName("Should create patient with valid data")
    void testCreatePatientWithValidData() {
        assertNotNull(patient);
        assertEquals("John", patient.getFirstName());
        assertEquals("Doe", patient.getLastName());
        assertEquals(LocalDate.of(1985, 5, 12), patient.getDateOfBirth());
        assertEquals(Patient.Gender.MALE, patient.getGender());
        assertEquals("Cardiology", patient.getDepartment());
        assertFalse(patient.getIsCritical());
    }

    @Test
    @DisplayName("Should calculate age correctly")
    void testCalculateAge() {
        int age = patient.getAge();
        int expectedAge = LocalDate.now().getYear() - 1985;
        assertEquals(expectedAge, age);
    }

    @Test
    @DisplayName("Should validate that age is not negative")
    void testAgeIsNotNegative() {
        assertTrue(patient.isValidAge());
    }

    @Test
    @DisplayName("Should detect invalid age for future date of birth")
    void testInvalidAgeForFutureDateOfBirth() {
        Patient futurePatient = new Patient(
                "Future",
                "Baby",
                LocalDate.now().plusYears(1),
                Patient.Gender.MALE,
                "Pediatrics",
                false
        );
        assertFalse(futurePatient.isValidAge());
    }

    @Test
    @DisplayName("Should set critical status correctly")
    void testSetCriticalStatus() {
        patient.setIsCritical(true);
        assertTrue(patient.getIsCritical());
    }

    @Test
    @DisplayName("Should update patient details")
    void testUpdatePatientDetails() {
        patient.setDepartment("Emergency");
        patient.setIsCritical(true);
        
        assertEquals("Emergency", patient.getDepartment());
        assertTrue(patient.getIsCritical());
    }

    @Test
    @DisplayName("Should handle null critical flag with default false")
    void testNullCriticalFlagDefaultsToFalse() {
        Patient patientWithNullCritical = new Patient(
                "Jane",
                "Smith",
                LocalDate.of(1990, 1, 1),
                Patient.Gender.FEMALE,
                "Cardiology",
                null
        );
        assertFalse(patientWithNullCritical.getIsCritical());
    }

    @Test
    @DisplayName("Should test equality based on ID")
    void testEqualityBasedOnId() {
        Patient patient1 = new Patient();
        patient1.setId(1L);
        
        Patient patient2 = new Patient();
        patient2.setId(1L);
        
        assertEquals(patient1, patient2);
    }

    @Test
    @DisplayName("Should test Gender enum display names")
    void testGenderEnumDisplayNames() {
        assertEquals("Male", Patient.Gender.MALE.getDisplayName());
        assertEquals("Female", Patient.Gender.FEMALE.getDisplayName());
        assertEquals("Other", Patient.Gender.OTHER.getDisplayName());
    }

    @Test
    @DisplayName("Should generate toString correctly")
    void testToString() {
        String result = patient.toString();
        assertTrue(result.contains("John"));
        assertTrue(result.contains("Doe"));
        assertTrue(result.contains("MALE"));
        assertTrue(result.contains("Cardiology"));
    }

    @Test
    @DisplayName("Should handle zero age for today's birthday")
    void testZeroAgeForTodayBirthday() {
        Patient newborn = new Patient(
                "Baby",
                "Smith",
                LocalDate.now(),
                Patient.Gender.FEMALE,
                "Pediatrics",
                true
        );
        assertEquals(0, newborn.getAge());
        assertTrue(newborn.isValidAge());
    }

    @Test
    @DisplayName("Should set and get blood group correctly")
    void testBloodGroupSetterGetter() {
        assertNotNull(patient.getBloodGroup());
        assertEquals("O+", patient.getBloodGroup());
        
        // Test setter with different blood groups
        patient.setBloodGroup("A+");
        assertEquals("A+", patient.getBloodGroup());
        
        patient.setBloodGroup("B-");
        assertEquals("B-", patient.getBloodGroup());
        
        patient.setBloodGroup("AB+");
        assertEquals("AB+", patient.getBloodGroup());
    }

    @Test
    @DisplayName("Should handle null blood group")
    void testNullBloodGroup() {
        Patient patientWithoutBloodGroup = new Patient(
                "Jane",
                "Smith",
                LocalDate.of(1990, 1, 1),
                Patient.Gender.FEMALE,
                "Cardiology",
                false
        );
        assertNull(patientWithoutBloodGroup.getBloodGroup());
        
        // Set blood group later
        patientWithoutBloodGroup.setBloodGroup("A-");
        assertEquals("A-", patientWithoutBloodGroup.getBloodGroup());
    }

    @Test
    @DisplayName("Should create patient with blood group using full constructor")
    void testCreatePatientWithBloodGroup() {
        Patient patientWithBloodGroup = new Patient(
                "Michael",
                "Johnson",
                LocalDate.of(1978, 3, 15),
                Patient.Gender.MALE,
                "Pediatrics",
                false,
                "O-"
        );
        assertEquals("O-", patientWithBloodGroup.getBloodGroup());
    }

    @Test
    @DisplayName("Should include blood group in toString")
    void testToStringWithBloodGroup() {
        String result = patient.toString();
        assertTrue(result.contains("bloodGroup='O+'"));
    }
}
