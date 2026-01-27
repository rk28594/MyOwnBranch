package com.sparks.patient.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.sparks.patient.dto.DoctorRequest;
import com.sparks.patient.dto.DoctorResponse;
import com.sparks.patient.entity.Doctor;

/**
 * Unit Tests for DoctorMapper
 * SCRUM-20: Doctor Profile Management
 */
@SpringBootTest
@DisplayName("DoctorMapper Unit Tests")
class DoctorMapperTest {

    @Autowired
    private DoctorMapper doctorMapper;

    private DoctorRequest doctorRequest;
    private Doctor doctor;

    @BeforeEach
    void setUp() {
        doctorRequest = DoctorRequest.builder()
                .fullName("Dr. John Smith")
                .licenseNumber("MED-123456")
                .specialization("Cardiology")
                .deptId(1L)
                .build();

        doctor = Doctor.builder()
                .id(1L)
                .fullName("Dr. John Smith")
                .licenseNumber("MED-123456")
                .specialization("Cardiology")
                .deptId(1L)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("Should convert DoctorRequest to Doctor entity")
    void shouldConvertRequestToEntity() {
        // Act
        Doctor result = doctorMapper.toEntity(doctorRequest);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getFullName()).isEqualTo("Dr. John Smith");
        assertThat(result.getLicenseNumber()).isEqualTo("MED-123456");
        assertThat(result.getSpecialization()).isEqualTo("Cardiology");
        assertThat(result.getDeptId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("Should convert Doctor entity to DoctorResponse DTO")
    void shouldConvertEntityToResponse() {
        // Act
        DoctorResponse result = doctorMapper.toResponse(doctor);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getFullName()).isEqualTo("Dr. John Smith");
        assertThat(result.getLicenseNumber()).isEqualTo("MED-123456");
        assertThat(result.getSpecialization()).isEqualTo("Cardiology");
        assertThat(result.getDeptId()).isEqualTo(1L);
        assertThat(result.getCreatedAt()).isNotNull();
        assertThat(result.getUpdatedAt()).isNotNull();
    }

    @Test
    @DisplayName("Should update Doctor entity with DoctorRequest data")
    void shouldUpdateEntity() {
        // Arrange
        DoctorRequest updateRequest = DoctorRequest.builder()
                .fullName("Dr. John Smith Updated")
                .licenseNumber("MED-789012")
                .specialization("Neurology")
                .deptId(2L)
                .build();

        // Act
        doctorMapper.updateEntity(updateRequest, doctor);

        // Assert
        assertThat(doctor.getFullName()).isEqualTo("Dr. John Smith Updated");
        assertThat(doctor.getLicenseNumber()).isEqualTo("MED-789012");
        assertThat(doctor.getSpecialization()).isEqualTo("Neurology");
        assertThat(doctor.getDeptId()).isEqualTo(2L);
    }

    @Test
    @DisplayName("Should handle null DoctorRequest gracefully")
    void shouldHandleNullRequest() {
        // Act
        Doctor result = doctorMapper.toEntity(null);

        // Assert
        assertThat(result).isNull();
    }

    @Test
    @DisplayName("Should handle null Doctor entity gracefully")
    void shouldHandleNullEntity() {
        // Act
        DoctorResponse result = doctorMapper.toResponse(null);

        // Assert
        assertThat(result).isNull();
    }

    @Test
    @DisplayName("Should handle null Doctor entity in updateEntity gracefully")
    void shouldHandleNullEntityInUpdate() {
        // Act
        doctorMapper.updateEntity(doctorRequest, null);

        // Assert
        // Should not throw exception
    }

    @Test
    @DisplayName("Should handle null DoctorRequest in updateEntity gracefully")
    void shouldHandleNullRequestInUpdate() {
        // Act
        doctorMapper.updateEntity(null, doctor);

        // Assert
        // Should not throw exception
        assertThat(doctor.getFullName()).isEqualTo("Dr. John Smith");
    }
}
