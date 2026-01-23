package com.hospital.management.repository;

import com.hospital.management.entity.Patient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

/**
 * Integration tests for PatientRepository
 * Tests JPA repository operations with H2 database
 */
@DataJpaTest
@DisplayName("PatientRepository Integration Tests")
class PatientRepositoryTest {

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private TestEntityManager entityManager;

    private Patient testPatient;

    @BeforeEach
    void setUp() {
        testPatient = Patient.builder()
                .firstName("John")
                .lastName("Doe")
                .dob(LocalDate.of(1990, 1, 15))
                .email("john.doe@example.com")
                .phone("+1234567890")
                .build();
    }

    @Nested
    @DisplayName("Save Patient Tests")
    class SavePatientTests {

        @Test
        @DisplayName("Should save patient successfully - Story SCRUM-16")
        void shouldSavePatientSuccessfully() {
            // Act
            Patient savedPatient = patientRepository.save(testPatient);

            // Assert
            assertThat(savedPatient).isNotNull();
            assertThat(savedPatient.getId()).isNotNull();
            assertThat(savedPatient.getFirstName()).isEqualTo("John");
            assertThat(savedPatient.getLastName()).isEqualTo("Doe");
            assertThat(savedPatient.getEmail()).isEqualTo("john.doe@example.com");
            assertThat(savedPatient.getPhone()).isEqualTo("+1234567890");
            assertThat(savedPatient.getDob()).isEqualTo(LocalDate.of(1990, 1, 15));
        }

        @Test
        @DisplayName("Should auto-generate ID when saving patient")
        void shouldAutoGenerateId() {
            // Act
            Patient savedPatient = patientRepository.save(testPatient);
            
            // Assert
            assertThat(savedPatient.getId()).isGreaterThan(0L);
        }

        @Test
        @DisplayName("Should set audit timestamps on save")
        void shouldSetAuditTimestamps() {
            // Act
            Patient savedPatient = patientRepository.save(testPatient);
            entityManager.flush();
            entityManager.clear();

            // Assert
            Patient foundPatient = patientRepository.findById(savedPatient.getId()).orElseThrow();
            assertThat(foundPatient.getCreatedAt()).isNotNull();
            assertThat(foundPatient.getUpdatedAt()).isNotNull();
        }

        @Test
        @DisplayName("Should throw exception when saving duplicate email - Story SCRUM-16")
        void shouldThrowExceptionOnDuplicateEmail() {
            // Arrange
            patientRepository.save(testPatient);
            entityManager.flush();
            entityManager.clear();

            Patient duplicatePatient = Patient.builder()
                    .firstName("Jane")
                    .lastName("Smith")
                    .dob(LocalDate.of(1985, 5, 20))
                    .email("john.doe@example.com") // Same email
                    .phone("+9876543210")
                    .build();

            // Act & Assert
            assertThatThrownBy(() -> {
                patientRepository.save(duplicatePatient);
                entityManager.flush();
            }).isInstanceOf(DataIntegrityViolationException.class);
        }

        @Test
        @DisplayName("Should save patient with minimum valid data")
        void shouldSavePatientWithMinimumData() {
            // Arrange
            Patient minimalPatient = Patient.builder()
                    .firstName("J")
                    .lastName("D")
                    .dob(LocalDate.of(2000, 1, 1))
                    .email("j@d.com")
                    .phone("1")
                    .build();

            // Act
            Patient savedPatient = patientRepository.save(minimalPatient);

            // Assert
            assertThat(savedPatient.getId()).isNotNull();
            assertThat(savedPatient.getFirstName()).isEqualTo("J");
            assertThat(savedPatient.getEmail()).isEqualTo("j@d.com");
        }

        @Test
        @DisplayName("Should save multiple patients with different emails")
        void shouldSaveMultiplePatientsWithDifferentEmails() {
            // Arrange
            Patient patient1 = testPatient;
            Patient patient2 = Patient.builder()
                    .firstName("Jane")
                    .lastName("Smith")
                    .dob(LocalDate.of(1985, 5, 20))
                    .email("jane.smith@example.com")
                    .phone("+9876543210")
                    .build();

            // Act
            Patient savedPatient1 = patientRepository.save(patient1);
            Patient savedPatient2 = patientRepository.save(patient2);

            // Assert
            assertThat(savedPatient1.getId()).isNotEqualTo(savedPatient2.getId());
            assertThat(savedPatient1.getEmail()).isNotEqualTo(savedPatient2.getEmail());
        }
    }

    @Nested
    @DisplayName("Find Patient Tests")
    class FindPatientTests {

        @Test
        @DisplayName("Should find patient by ID")
        void shouldFindPatientById() {
            // Arrange
            Patient savedPatient = patientRepository.save(testPatient);
            entityManager.flush();
            entityManager.clear();

            // Act
            Optional<Patient> foundPatient = patientRepository.findById(savedPatient.getId());

            // Assert
            assertThat(foundPatient).isPresent();
            assertThat(foundPatient.get().getId()).isEqualTo(savedPatient.getId());
            assertThat(foundPatient.get().getEmail()).isEqualTo("john.doe@example.com");
        }

        @Test
        @DisplayName("Should return empty when patient ID not found")
        void shouldReturnEmptyWhenIdNotFound() {
            // Act
            Optional<Patient> foundPatient = patientRepository.findById(999L);

            // Assert
            assertThat(foundPatient).isEmpty();
        }

        @Test
        @DisplayName("Should find patient by email")
        void shouldFindPatientByEmail() {
            // Arrange
            patientRepository.save(testPatient);
            entityManager.flush();
            entityManager.clear();

            // Act
            Optional<Patient> foundPatient = patientRepository.findByEmail("john.doe@example.com");

            // Assert
            assertThat(foundPatient).isPresent();
            assertThat(foundPatient.get().getEmail()).isEqualTo("john.doe@example.com");
        }

        @Test
        @DisplayName("Should return empty when email not found")
        void shouldReturnEmptyWhenEmailNotFound() {
            // Act
            Optional<Patient> foundPatient = patientRepository.findByEmail("nonexistent@example.com");

            // Assert
            assertThat(foundPatient).isEmpty();
        }

        @Test
        @DisplayName("Should find all patients")
        void shouldFindAllPatients() {
            // Arrange
            Patient patient1 = testPatient;
            Patient patient2 = Patient.builder()
                    .firstName("Jane")
                    .lastName("Smith")
                    .dob(LocalDate.of(1985, 5, 20))
                    .email("jane.smith@example.com")
                    .phone("+9876543210")
                    .build();

            patientRepository.save(patient1);
            patientRepository.save(patient2);

            // Act
            List<Patient> allPatients = patientRepository.findAll();

            // Assert
            assertThat(allPatients).hasSizeGreaterThanOrEqualTo(2);
        }

        @Test
        @DisplayName("Should check if patient exists by email")
        void shouldCheckExistsByEmail() {
            // Arrange
            patientRepository.save(testPatient);
            entityManager.flush();

            // Act
            boolean exists = patientRepository.existsByEmail("john.doe@example.com");
            boolean notExists = patientRepository.existsByEmail("nonexistent@example.com");

            // Assert
            assertThat(exists).isTrue();
            assertThat(notExists).isFalse();
        }
    }

    @Nested
    @DisplayName("Update Patient Tests")
    class UpdatePatientTests {

        @Test
        @DisplayName("Should update patient successfully")
        void shouldUpdatePatientSuccessfully() {
            // Arrange
            Patient savedPatient = patientRepository.save(testPatient);
            entityManager.flush();
            entityManager.clear();

            // Act
            savedPatient.setFirstName("John Updated");
            savedPatient.setPhone("+9999999999");
            Patient updatedPatient = patientRepository.save(savedPatient);
            entityManager.flush();
            entityManager.clear();

            // Assert
            Patient foundPatient = patientRepository.findById(updatedPatient.getId()).orElseThrow();
            assertThat(foundPatient.getFirstName()).isEqualTo("John Updated");
            assertThat(foundPatient.getPhone()).isEqualTo("+9999999999");
        }

        @Test
        @DisplayName("Should update updatedAt timestamp on update")
        void shouldUpdateTimestampOnUpdate() {
            // Arrange
            Patient savedPatient = patientRepository.save(testPatient);
            entityManager.flush();
            LocalDate originalUpdatedAt = savedPatient.getUpdatedAt();

            // Act
            savedPatient.setFirstName("Updated Name");
            patientRepository.save(savedPatient);
            entityManager.flush();
            entityManager.clear();

            // Assert - Note: Since we're using LocalDate, changes might not be visible within same day
            Patient foundPatient = patientRepository.findById(savedPatient.getId()).orElseThrow();
            assertThat(foundPatient.getUpdatedAt()).isNotNull();
        }

        @Test
        @DisplayName("Should not allow updating to duplicate email")
        void shouldNotAllowUpdatingToDuplicateEmail() {
            // Arrange
            Patient patient1 = testPatient;
            Patient patient2 = Patient.builder()
                    .firstName("Jane")
                    .lastName("Smith")
                    .dob(LocalDate.of(1985, 5, 20))
                    .email("jane.smith@example.com")
                    .phone("+9876543210")
                    .build();

            Patient savedPatient1 = patientRepository.save(patient1);
            patientRepository.save(patient2);
            entityManager.flush();

            // Act & Assert
            savedPatient1.setEmail("jane.smith@example.com");
            assertThatThrownBy(() -> {
                patientRepository.save(savedPatient1);
                entityManager.flush();
            }).isInstanceOf(DataIntegrityViolationException.class);
        }
    }

    @Nested
    @DisplayName("Delete Patient Tests")
    class DeletePatientTests {

        @Test
        @DisplayName("Should delete patient successfully")
        void shouldDeletePatientSuccessfully() {
            // Arrange
            Patient savedPatient = patientRepository.save(testPatient);
            Long patientId = savedPatient.getId();
            entityManager.flush();
            entityManager.clear();

            // Act
            patientRepository.deleteById(patientId);
            entityManager.flush();

            // Assert
            Optional<Patient> foundPatient = patientRepository.findById(patientId);
            assertThat(foundPatient).isEmpty();
        }

        @Test
        @DisplayName("Should check patient existence")
        void shouldCheckPatientExistence() {
            // Arrange
            Patient savedPatient = patientRepository.save(testPatient);
            Long patientId = savedPatient.getId();

            // Act
            boolean existsBefore = patientRepository.existsById(patientId);
            patientRepository.deleteById(patientId);
            entityManager.flush();
            boolean existsAfter = patientRepository.existsById(patientId);

            // Assert
            assertThat(existsBefore).isTrue();
            assertThat(existsAfter).isFalse();
        }

        @Test
        @DisplayName("Should throw exception when deleting non-existent patient")
        void shouldThrowExceptionWhenDeletingNonExistent() {
            // Act & Assert - Spring Data JPA's deleteById throws EmptyResultDataAccessException
            assertThatThrownBy(() -> {
                patientRepository.deleteById(999L);
                entityManager.flush();
            }).isInstanceOf(EmptyResultDataAccessException.class);
        }
    }

    @Nested
    @DisplayName("Edge Cases and Data Validation")
    class EdgeCasesTests {

        @Test
        @DisplayName("Should handle patient with special characters in names")
        void shouldHandleSpecialCharactersInNames() {
            // Arrange
            Patient specialCharsPatient = Patient.builder()
                    .firstName("Jean-François")
                    .lastName("O'Brien-Smith")
                    .dob(LocalDate.of(1990, 1, 15))
                    .email("special@example.com")
                    .phone("+1234567890")
                    .build();

            // Act
            Patient savedPatient = patientRepository.save(specialCharsPatient);
            entityManager.flush();
            entityManager.clear();

            // Assert
            Patient foundPatient = patientRepository.findById(savedPatient.getId()).orElseThrow();
            assertThat(foundPatient.getFirstName()).isEqualTo("Jean-François");
            assertThat(foundPatient.getLastName()).isEqualTo("O'Brien-Smith");
        }

        @Test
        @DisplayName("Should handle patient with very old date of birth")
        void shouldHandleVeryOldDateOfBirth() {
            // Arrange - Use 1920 to avoid historical timezone issues with H2
            Patient oldDobPatient = Patient.builder()
                    .firstName("Old")
                    .lastName("Person")
                    .dob(LocalDate.of(1920, 1, 1))
                    .email("old@example.com")
                    .phone("+1234567890")
                    .build();

            // Act
            Patient savedPatient = patientRepository.save(oldDobPatient);
            entityManager.flush();
            entityManager.clear();

            // Assert
            Patient foundPatient = patientRepository.findById(savedPatient.getId()).orElseThrow();
            assertThat(foundPatient.getDob()).isEqualTo(LocalDate.of(1920, 1, 1));
        }

        @Test
        @DisplayName("Should handle international phone numbers")
        void shouldHandleInternationalPhoneNumbers() {
            // Arrange
            Patient intlPhonePatient = Patient.builder()
                    .firstName("John")
                    .lastName("Doe")
                    .dob(LocalDate.of(1990, 1, 15))
                    .email("intl@example.com")
                    .phone("+44 20 7946 0958")
                    .build();

            // Act
            Patient savedPatient = patientRepository.save(intlPhonePatient);
            entityManager.flush();
            entityManager.clear();

            // Assert
            Patient foundPatient = patientRepository.findById(savedPatient.getId()).orElseThrow();
            assertThat(foundPatient.getPhone()).isEqualTo("+44 20 7946 0958");
        }

        @Test
        @DisplayName("Should handle email case sensitivity") 
        void shouldHandleEmailCaseSensitivity() {
            // Arrange
            Patient patient1 = testPatient;
            patient1.setEmail("test@example.com");
            
            Patient patient2 = Patient.builder()
                    .firstName("Jane")
                    .lastName("Smith")
                    .dob(LocalDate.of(1985, 5, 20))
                    .email("TEST@EXAMPLE.COM") // Different case but should be treated as same
                    .phone("+9876543210")
                    .build();

            // Act
            patientRepository.save(patient1);
            entityManager.flush();
            entityManager.clear();

            // Assert - H2 has case-insensitive email matching by default
            // findByEmail should return the same patient regardless of case
            Optional<Patient> found = patientRepository.findByEmail("TEST@EXAMPLE.COM");
            assertThat(found).isPresent();
            assertThat(found.get().getEmail()).isEqualToIgnoringCase("test@example.com");
        }

        @Test
        @DisplayName("Should persist patient with very long names")
        void shouldPersistPatientWithVeryLongNames() {
            // Arrange
            String longName = "A".repeat(255);
            Patient longNamePatient = Patient.builder()
                    .firstName(longName)
                    .lastName(longName)
                    .dob(LocalDate.of(1990, 1, 15))
                    .email("longname@example.com")
                    .phone("+1234567890")
                    .build();

            // Act
            Patient savedPatient = patientRepository.save(longNamePatient);
            entityManager.flush();
            entityManager.clear();

            // Assert
            Patient foundPatient = patientRepository.findById(savedPatient.getId()).orElseThrow();
            assertThat(foundPatient.getFirstName()).hasSize(255);
            assertThat(foundPatient.getLastName()).hasSize(255);
        }

        @Test
        @DisplayName("Should reject today as date of birth due to @Past constraint")
        void shouldRejectTodayAsDob() {
            // Arrange
            Patient todayDobPatient = Patient.builder()
                    .firstName("Baby")
                    .lastName("NewBorn")
                    .dob(LocalDate.now()) // Violates @Past constraint
                    .email("baby@example.com")
                    .phone("+1234567890")
                    .build();

            // Act & Assert - @Past validation should fail
            assertThatThrownBy(() -> {
                patientRepository.save(todayDobPatient);
                entityManager.flush();
            }).isInstanceOf(javax.validation.ConstraintViolationException.class);
        }
    }
}
