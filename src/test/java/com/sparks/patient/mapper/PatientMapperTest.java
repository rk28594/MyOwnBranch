package com.sparks.patient.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.sparks.patient.dto.PatientRequest;
import com.sparks.patient.dto.PatientResponse;
import com.sparks.patient.entity.Patient;

/**
 * Unit Tests for PatientMapper
 */
@DisplayName("PatientMapper Unit Tests")
class PatientMapperTest {

    private PatientMapper patientMapper;

    @BeforeEach
    void setUp() {
        patientMapper = new PatientMapper();
    }

    @Test
    @DisplayName("Should convert PatientRequest to Patient entity")
    void shouldConvertRequestToEntity() {
        // Given
        PatientRequest request = PatientRequest.builder()
                .firstName("John")
                .lastName("Doe")
                .dob(LocalDate.of(1990, 5, 15))
                .email("john.doe@example.com")
                .phone("+1234567890")
                .build();

        // When
        Patient patient = patientMapper.toEntity(request);

        // Then
        assertThat(patient).isNotNull();
        assertThat(patient.getId()).isNull();
        assertThat(patient.getFirstName()).isEqualTo("John");
        assertThat(patient.getLastName()).isEqualTo("Doe");
        assertThat(patient.getDob()).isEqualTo(LocalDate.of(1990, 5, 15));
        assertThat(patient.getEmail()).isEqualTo("john.doe@example.com");
        assertThat(patient.getPhone()).isEqualTo("+1234567890");
    }

    @Test
    @DisplayName("Should return null when converting null request")
    void shouldReturnNullForNullRequest() {
        // When
        Patient patient = patientMapper.toEntity(null);

        // Then
        assertThat(patient).isNull();
    }

    @Test
    @DisplayName("Should convert Patient entity to PatientResponse")
    void shouldConvertEntityToResponse() {
        // Given
        LocalDateTime now = LocalDateTime.now();
        Patient patient = Patient.builder()
                .id(1L)
                .firstName("John")
                .lastName("Doe")
                .dob(LocalDate.of(1990, 5, 15))
                .email("john.doe@example.com")
                .phone("+1234567890")
                .createdAt(now)
                .updatedAt(now)
                .build();

        // When
        PatientResponse response = patientMapper.toResponse(patient);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getFirstName()).isEqualTo("John");
        assertThat(response.getLastName()).isEqualTo("Doe");
        assertThat(response.getDob()).isEqualTo(LocalDate.of(1990, 5, 15));
        assertThat(response.getEmail()).isEqualTo("john.doe@example.com");
        assertThat(response.getPhone()).isEqualTo("+1234567890");
        assertThat(response.getCreatedAt()).isEqualTo(now);
        assertThat(response.getUpdatedAt()).isEqualTo(now);
    }

    @Test
    @DisplayName("Should return null when converting null entity")
    void shouldReturnNullForNullEntity() {
        // When
        PatientResponse response = patientMapper.toResponse(null);

        // Then
        assertThat(response).isNull();
    }

    @Test
    @DisplayName("Should update existing entity with request data")
    void shouldUpdateEntityFromRequest() {
        // Given
        Patient patient = Patient.builder()
                .id(1L)
                .firstName("Old Name")
                .lastName("Old Last")
                .dob(LocalDate.of(1985, 1, 1))
                .email("old@example.com")
                .phone("+1111111111")
                .build();

        PatientRequest request = PatientRequest.builder()
                .firstName("New Name")
                .lastName("New Last")
                .dob(LocalDate.of(1990, 5, 15))
                .email("new@example.com")
                .phone("+2222222222")
                .build();

        // When
        patientMapper.updateEntity(patient, request);

        // Then
        assertThat(patient.getId()).isEqualTo(1L); // ID should remain unchanged
        assertThat(patient.getFirstName()).isEqualTo("New Name");
        assertThat(patient.getLastName()).isEqualTo("New Last");
        assertThat(patient.getDob()).isEqualTo(LocalDate.of(1990, 5, 15));
        assertThat(patient.getEmail()).isEqualTo("new@example.com");
        assertThat(patient.getPhone()).isEqualTo("+2222222222");
    }
}
