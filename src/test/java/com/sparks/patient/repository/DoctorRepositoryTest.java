package com.sparks.patient.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import com.sparks.patient.entity.Doctor;

/**
 * Repository Tests for DoctorRepository
 * SCRUM-20: Doctor Profile Management
 */
@DataJpaTest
@DisplayName("DoctorRepository Tests")
class DoctorRepositoryTest {

    @Autowired
    private DoctorRepository doctorRepository;

    private Doctor doctor;

    @BeforeEach
    void setUp() {
        doctor = Doctor.builder()
                .fullName("Dr. John Smith")
                .licenseNumber("MED-123456")
                .specialization("Cardiology")
                .deptId(1L)
                .build();
    }

    @Test
    @DisplayName("Should save doctor successfully")
    void shouldSaveDoctorSuccessfully() {
        // Act
        Doctor savedDoctor = doctorRepository.save(doctor);

        // Assert
        assertThat(savedDoctor).isNotNull();
        assertThat(savedDoctor.getId()).isNotNull();
        assertThat(savedDoctor.getFullName()).isEqualTo("Dr. John Smith");
        assertThat(savedDoctor.getLicenseNumber()).isEqualTo("MED-123456");
    }

    @Test
    @DisplayName("Should find doctor by license number")
    void shouldFindDoctorByLicenseNumber() {
        // Arrange
        doctorRepository.save(doctor);

        // Act
        Optional<Doctor> result = doctorRepository.findByLicenseNumber("MED-123456");

        // Assert
        assertThat(result).isPresent();
        assertThat(result.get().getFullName()).isEqualTo("Dr. John Smith");
    }

    @Test
    @DisplayName("Should return empty when license number not found")
    void shouldReturnEmptyWhenLicenseNumberNotFound() {
        // Act
        Optional<Doctor> result = doctorRepository.findByLicenseNumber("INVALID");

        // Assert
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Should check if doctor exists by license number")
    void shouldCheckIfDoctorExistsByLicenseNumber() {
        // Arrange
        doctorRepository.save(doctor);

        // Act
        boolean exists = doctorRepository.existsByLicenseNumber("MED-123456");

        // Assert
        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("Should return false when checking non-existent license number")
    void shouldReturnFalseWhenCheckingNonExistentLicenseNumber() {
        // Act
        boolean exists = doctorRepository.existsByLicenseNumber("INVALID");

        // Assert
        assertThat(exists).isFalse();
    }

    @Test
    @DisplayName("Should update doctor successfully")
    void shouldUpdateDoctorSuccessfully() {
        // Arrange
        Doctor savedDoctor = doctorRepository.save(doctor);
        savedDoctor.setFullName("Dr. John Smith Updated");
        savedDoctor.setSpecialization("Neurology");

        // Act
        Doctor updatedDoctor = doctorRepository.save(savedDoctor);

        // Assert
        assertThat(updatedDoctor.getFullName()).isEqualTo("Dr. John Smith Updated");
        assertThat(updatedDoctor.getSpecialization()).isEqualTo("Neurology");
    }

    @Test
    @DisplayName("Should delete doctor successfully")
    void shouldDeleteDoctorSuccessfully() {
        // Arrange
        Doctor savedDoctor = doctorRepository.save(doctor);

        // Act
        doctorRepository.delete(savedDoctor);

        // Assert
        Optional<Doctor> result = doctorRepository.findById(savedDoctor.getId());
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Should find doctor by ID")
    void shouldFindDoctorById() {
        // Arrange
        Doctor savedDoctor = doctorRepository.save(doctor);

        // Act
        Optional<Doctor> result = doctorRepository.findById(savedDoctor.getId());

        // Assert
        assertThat(result).isPresent();
        assertThat(result.get().getFullName()).isEqualTo("Dr. John Smith");
    }

    @Test
    @DisplayName("Should return all doctors")
    void shouldReturnAllDoctors() {
        // Arrange
        Doctor doctor1 = Doctor.builder()
                .fullName("Dr. John Smith")
                .licenseNumber("MED-123456")
                .specialization("Cardiology")
                .deptId(1L)
                .build();

        Doctor doctor2 = Doctor.builder()
                .fullName("Dr. Jane Doe")
                .licenseNumber("MED-654321")
                .specialization("Neurology")
                .deptId(2L)
                .build();

        doctorRepository.save(doctor1);
        doctorRepository.save(doctor2);

        // Act
        Iterable<Doctor> result = doctorRepository.findAll();

        // Assert
        assertThat(result).hasSize(2);
    }
}
