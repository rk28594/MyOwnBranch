package com.hospital.management.repository;

import com.hospital.management.entity.Doctor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

/**
 * Integration tests for DoctorRepository - Story SCRUM-20: Doctor Profile Management
 * Tests JPA repository operations with H2 database
 */
@DataJpaTest
@DisplayName("DoctorRepository Integration Tests - SCRUM-20")
class DoctorRepositoryTest {

    @Autowired
    private DoctorRepository doctorRepository;

    @Autowired
    private TestEntityManager entityManager;

    private Doctor testDoctor;

    @BeforeEach
    void setUp() {
        testDoctor = Doctor.builder()
                .fullName("Dr. John Smith")
                .licenseNumber("LIC-12345")
                .specialization("Cardiology")
                .deptId(1L)
                .build();
    }

    @Nested
    @DisplayName("Save Doctor Tests")
    class SaveDoctorTests {

        @Test
        @DisplayName("Should save doctor successfully - Story SCRUM-20")
        void shouldSaveDoctorSuccessfully() {
            // Act
            Doctor savedDoctor = doctorRepository.save(testDoctor);

            // Assert
            assertThat(savedDoctor).isNotNull();
            assertThat(savedDoctor.getId()).isNotNull();
            assertThat(savedDoctor.getFullName()).isEqualTo("Dr. John Smith");
            assertThat(savedDoctor.getLicenseNumber()).isEqualTo("LIC-12345");
            assertThat(savedDoctor.getSpecialization()).isEqualTo("Cardiology");
            assertThat(savedDoctor.getDeptId()).isEqualTo(1L);
        }

        @Test
        @DisplayName("Should auto-generate ID when saving doctor")
        void shouldAutoGenerateId() {
            // Act
            Doctor savedDoctor = doctorRepository.save(testDoctor);
            
            // Assert
            assertThat(savedDoctor.getId()).isGreaterThan(0L);
        }

        @Test
        @DisplayName("Should set audit timestamps on save")
        void shouldSetAuditTimestamps() {
            // Act
            Doctor savedDoctor = doctorRepository.save(testDoctor);
            entityManager.flush();
            entityManager.clear();

            // Assert
            Doctor foundDoctor = doctorRepository.findById(savedDoctor.getId()).orElseThrow();
            assertThat(foundDoctor.getCreatedAt()).isNotNull();
            assertThat(foundDoctor.getUpdatedAt()).isNotNull();
        }

        @Test
        @DisplayName("Should throw exception when saving duplicate license number - SCRUM-20 AC")
        void shouldThrowExceptionOnDuplicateLicenseNumber() {
            // Arrange
            doctorRepository.save(testDoctor);
            entityManager.flush();
            entityManager.clear();

            Doctor duplicateDoctor = Doctor.builder()
                    .fullName("Dr. Jane Doe")
                    .licenseNumber("LIC-12345") // Same license
                    .specialization("Neurology")
                    .deptId(2L)
                    .build();

            // Act & Assert
            assertThatThrownBy(() -> {
                doctorRepository.save(duplicateDoctor);
                entityManager.flush();
            }).isInstanceOf(DataIntegrityViolationException.class);
        }

        @Test
        @DisplayName("Should save multiple doctors with different license numbers")
        void shouldSaveMultipleDoctorsWithDifferentLicenses() {
            // Arrange
            Doctor doctor1 = testDoctor;
            Doctor doctor2 = Doctor.builder()
                    .fullName("Dr. Jane Doe")
                    .licenseNumber("LIC-67890")
                    .specialization("Neurology")
                    .deptId(2L)
                    .build();

            // Act
            Doctor savedDoctor1 = doctorRepository.save(doctor1);
            Doctor savedDoctor2 = doctorRepository.save(doctor2);

            // Assert
            assertThat(savedDoctor1.getId()).isNotEqualTo(savedDoctor2.getId());
            assertThat(savedDoctor1.getLicenseNumber()).isNotEqualTo(savedDoctor2.getLicenseNumber());
        }
    }

    @Nested
    @DisplayName("Find Doctor Tests")
    class FindDoctorTests {

        @Test
        @DisplayName("Should find doctor by ID")
        void shouldFindDoctorById() {
            // Arrange
            Doctor savedDoctor = doctorRepository.save(testDoctor);
            entityManager.flush();
            entityManager.clear();

            // Act
            Optional<Doctor> foundDoctor = doctorRepository.findById(savedDoctor.getId());

            // Assert
            assertThat(foundDoctor).isPresent();
            assertThat(foundDoctor.get().getLicenseNumber()).isEqualTo("LIC-12345");
        }

        @Test
        @DisplayName("Should find doctor by license number")
        void shouldFindDoctorByLicenseNumber() {
            // Arrange
            doctorRepository.save(testDoctor);
            entityManager.flush();
            entityManager.clear();

            // Act
            Optional<Doctor> foundDoctor = doctorRepository.findByLicenseNumber("LIC-12345");

            // Assert
            assertThat(foundDoctor).isPresent();
            assertThat(foundDoctor.get().getFullName()).isEqualTo("Dr. John Smith");
        }

        @Test
        @DisplayName("Should return empty when license number not found")
        void shouldReturnEmptyWhenLicenseNotFound() {
            // Act
            Optional<Doctor> foundDoctor = doctorRepository.findByLicenseNumber("NONEXISTENT");

            // Assert
            assertThat(foundDoctor).isEmpty();
        }

        @Test
        @DisplayName("Should check if doctor exists by license number")
        void shouldCheckExistsByLicenseNumber() {
            // Arrange
            doctorRepository.save(testDoctor);
            entityManager.flush();

            // Act & Assert
            assertThat(doctorRepository.existsByLicenseNumber("LIC-12345")).isTrue();
            assertThat(doctorRepository.existsByLicenseNumber("NONEXISTENT")).isFalse();
        }

        @Test
        @DisplayName("Should find all doctors")
        void shouldFindAllDoctors() {
            // Arrange
            Doctor doctor2 = Doctor.builder()
                    .fullName("Dr. Jane Doe")
                    .licenseNumber("LIC-67890")
                    .specialization("Neurology")
                    .deptId(2L)
                    .build();

            doctorRepository.save(testDoctor);
            doctorRepository.save(doctor2);
            entityManager.flush();
            entityManager.clear();

            // Act
            List<Doctor> doctors = doctorRepository.findAll();

            // Assert
            assertThat(doctors).hasSize(2);
        }
    }

    @Nested
    @DisplayName("Update Doctor Tests")
    class UpdateDoctorTests {

        @Test
        @DisplayName("Should update doctor specialization")
        void shouldUpdateDoctorSpecialization() {
            // Arrange
            Doctor savedDoctor = doctorRepository.save(testDoctor);
            entityManager.flush();
            entityManager.clear();

            // Act
            Doctor doctorToUpdate = doctorRepository.findById(savedDoctor.getId()).orElseThrow();
            doctorToUpdate.setSpecialization("Oncology");
            doctorRepository.save(doctorToUpdate);
            entityManager.flush();
            entityManager.clear();

            // Assert
            Doctor updatedDoctor = doctorRepository.findById(savedDoctor.getId()).orElseThrow();
            assertThat(updatedDoctor.getSpecialization()).isEqualTo("Oncology");
        }
    }

    @Nested
    @DisplayName("Delete Doctor Tests")
    class DeleteDoctorTests {

        @Test
        @DisplayName("Should delete doctor by ID")
        void shouldDeleteDoctorById() {
            // Arrange
            Doctor savedDoctor = doctorRepository.save(testDoctor);
            entityManager.flush();
            Long doctorId = savedDoctor.getId();

            // Act
            doctorRepository.deleteById(doctorId);
            entityManager.flush();
            entityManager.clear();

            // Assert
            Optional<Doctor> deletedDoctor = doctorRepository.findById(doctorId);
            assertThat(deletedDoctor).isEmpty();
        }
    }
}
