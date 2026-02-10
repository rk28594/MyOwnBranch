package com.sparks.patient.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.TestPropertySource;

import com.sparks.patient.entity.Patient;
import com.sparks.patient.test.IntegrationTest;

/**
 * Integration Tests for PatientRepository
 * SCRUM-16: Patient Schema & Entity Mapping
 * Tests database operations and constraints
 */
@IntegrationTest
@DataJpaTest
@TestPropertySource(properties = {"spring.jpa.show-sql=false", "logging.level.org.hibernate=ERROR", "logging.level.org.springframework=ERROR"})
@DisplayName("PatientRepository Integration Tests")
class PatientRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private PatientRepository patientRepository;

    private Patient testPatient;

    @BeforeEach
    void setUp() {
        testPatient = Patient.builder()
                .firstName("John")
                .lastName("Doe")
                .dob(LocalDate.of(1990, 5, 15))
                .email("john.doe@example.com")
                .phone("+1234567890")
                .build();
    }

    @Nested
    @DisplayName("SCRUM-16: Patient Schema Tests")
    class PatientSchemaTests {

        @Test
        @DisplayName("Should save patient with all fields correctly")
        void shouldSavePatientWithAllFields() {
            // When
            Patient savedPatient = patientRepository.save(testPatient);
            entityManager.flush();
            entityManager.clear();

            // Then
            Patient foundPatient = patientRepository.findById(savedPatient.getId()).orElse(null);
            assertThat(foundPatient).isNotNull();
            assertThat(foundPatient.getId()).isNotNull();
            assertThat(foundPatient.getFirstName()).isEqualTo("John");
            assertThat(foundPatient.getLastName()).isEqualTo("Doe");
            assertThat(foundPatient.getDob()).isEqualTo(LocalDate.of(1990, 5, 15));
            assertThat(foundPatient.getEmail()).isEqualTo("john.doe@example.com");
            assertThat(foundPatient.getPhone()).isEqualTo("+1234567890");
            assertThat(foundPatient.getCreatedAt()).isNotNull();
            assertThat(foundPatient.getUpdatedAt()).isNotNull();
        }

        @Test
        @DisplayName("Should auto-generate ID on save")
        void shouldAutoGenerateId() {
            // Given
            assertThat(testPatient.getId()).isNull();

            // When
            Patient savedPatient = patientRepository.save(testPatient);

            // Then
            assertThat(savedPatient.getId()).isNotNull();
            assertThat(savedPatient.getId()).isGreaterThan(0);
        }
    }

    @Nested
    @DisplayName("Find By Email Tests")
    class FindByEmailTests {

        @Test
        @DisplayName("Should find patient by email")
        void shouldFindPatientByEmail() {
            // Given
            entityManager.persistAndFlush(testPatient);
            entityManager.clear();

            // When
            Optional<Patient> found = patientRepository.findByEmail("john.doe@example.com");

            // Then
            assertThat(found).isPresent();
            assertThat(found.get().getFirstName()).isEqualTo("John");
        }

        @Test
        @DisplayName("Should return empty when email not found")
        void shouldReturnEmptyWhenEmailNotFound() {
            // When
            Optional<Patient> found = patientRepository.findByEmail("nonexistent@example.com");

            // Then
            assertThat(found).isEmpty();
        }
    }

    @Nested
    @DisplayName("Email Uniqueness Tests")
    class EmailUniquenessTests {

        @Test
        @DisplayName("Should check if email exists - true case")
        void shouldReturnTrueWhenEmailExists() {
            // Given
            entityManager.persistAndFlush(testPatient);
            entityManager.clear();

            // When
            boolean exists = patientRepository.existsByEmail("john.doe@example.com");

            // Then
            assertThat(exists).isTrue();
        }

        @Test
        @DisplayName("Should check if email exists - false case")
        void shouldReturnFalseWhenEmailNotExists() {
            // When
            boolean exists = patientRepository.existsByEmail("nonexistent@example.com");

            // Then
            assertThat(exists).isFalse();
        }
    }

    @Nested
    @DisplayName("CRUD Operations Tests")
    class CrudOperationsTests {

        @Test
        @DisplayName("Should update patient")
        void shouldUpdatePatient() {
            // Given
            Patient savedPatient = entityManager.persistAndFlush(testPatient);
            entityManager.clear();

            // When
            Patient patientToUpdate = patientRepository.findById(savedPatient.getId()).orElseThrow();
            patientToUpdate.setFirstName("Jane");
            patientToUpdate.setPhone("+9999999999");
            patientRepository.saveAndFlush(patientToUpdate);
            entityManager.clear();

            // Then
            Patient updatedPatient = patientRepository.findById(savedPatient.getId()).orElse(null);
            assertThat(updatedPatient).isNotNull();
            assertThat(updatedPatient.getFirstName()).isEqualTo("Jane");
            assertThat(updatedPatient.getPhone()).isEqualTo("+9999999999");
        }

        @Test
        @DisplayName("Should delete patient")
        void shouldDeletePatient() {
            // Given
            Patient savedPatient = entityManager.persistAndFlush(testPatient);
            Long patientId = savedPatient.getId();
            entityManager.clear();

            // When
            patientRepository.deleteById(patientId);
            entityManager.flush();

            // Then
            Optional<Patient> deleted = patientRepository.findById(patientId);
            assertThat(deleted).isEmpty();
        }

        @Test
        @DisplayName("Should find all patients")
        void shouldFindAllPatients() {
            // Given
            Patient patient2 = Patient.builder()
                    .firstName("Jane")
                    .lastName("Smith")
                    .dob(LocalDate.of(1985, 3, 20))
                    .email("jane.smith@example.com")
                    .phone("+9876543210")
                    .build();

            entityManager.persist(testPatient);
            entityManager.persistAndFlush(patient2);
            entityManager.clear();

            // When
            var patients = patientRepository.findAll();

            // Then
            assertThat(patients).hasSize(2);
        }
    }

    @Nested
    @DisplayName("Count By Last Name Tests")
    class CountByLastNameTests {

        @Test
        @DisplayName("Should count patients by last name")
        void shouldCountPatientsByLastName() {
            // Given - Create multiple patients with same last name
            Patient patient1 = Patient.builder()
                    .firstName("John")
                    .lastName("Smith")
                    .dob(LocalDate.of(1990, 5, 15))
                    .email("john.smith@example.com")
                    .phone("+1111111111")
                    .build();
            Patient patient2 = Patient.builder()
                    .firstName("Jane")
                    .lastName("Smith")
                    .dob(LocalDate.of(1992, 3, 20))
                    .email("jane.smith@example.com")
                    .phone("+2222222222")
                    .build();
            Patient patient3 = Patient.builder()
                    .firstName("Bob")
                    .lastName("Jones")
                    .dob(LocalDate.of(1985, 8, 10))
                    .email("bob.jones@example.com")
                    .phone("+3333333333")
                    .build();

            patientRepository.save(patient1);
            patientRepository.save(patient2);
            patientRepository.save(patient3);
            entityManager.flush();

            // When
            long smithCount = patientRepository.countByLastName("Smith");
            long jonesCount = patientRepository.countByLastName("Jones");
            long nonExistentCount = patientRepository.countByLastName("NonExistent");

            // Then
            assertThat(smithCount).isEqualTo(2);
            assertThat(jonesCount).isEqualTo(1);
            assertThat(nonExistentCount).isEqualTo(0);
        }
    }
}
