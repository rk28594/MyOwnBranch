package com.sparks.patient.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.sparks.patient.dto.PatientRequest;
import com.sparks.patient.dto.PatientResponse;
import com.sparks.patient.entity.Patient;
import com.sparks.patient.exception.DuplicateEmailException;
import com.sparks.patient.exception.PatientNotFoundException;
import com.sparks.patient.mapper.PatientMapper;
import com.sparks.patient.repository.PatientRepository;

/**
 * Unit Tests for PatientServiceImpl
 * Tests business logic in isolation using mocks
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("PatientService Unit Tests")
class PatientServiceImplTest {

    @Mock
    private PatientRepository patientRepository;

    @Mock
    private PatientMapper patientMapper;

    @InjectMocks
    private PatientServiceImpl patientService;

    private PatientRequest patientRequest;
    private Patient patient;
    private PatientResponse patientResponse;

    @BeforeEach
    void setUp() {
        patientRequest = PatientRequest.builder()
                .firstName("John")
                .lastName("Doe")
                .dob(LocalDate.of(1990, 5, 15))
                .email("john.doe@example.com")
                .phone("+1234567890")
                .build();

        patient = Patient.builder()
                .id(1L)
                .firstName("John")
                .lastName("Doe")
                .dob(LocalDate.of(1990, 5, 15))
                .email("john.doe@example.com")
                .phone("+1234567890")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        patientResponse = PatientResponse.builder()
                .id(1L)
                .firstName("John")
                .lastName("Doe")
                .dob(LocalDate.of(1990, 5, 15))
                .email("john.doe@example.com")
                .phone("+1234567890")
                .createdAt(patient.getCreatedAt())
                .updatedAt(patient.getUpdatedAt())
                .build();
    }

    @Nested
    @DisplayName("SCRUM-14: Patient Onboarding API - Create Patient Tests")
    class CreatePatientTests {

        @Test
        @DisplayName("Should create patient successfully when valid data provided")
        void shouldCreatePatientSuccessfully() {
            // Given
            when(patientRepository.existsByEmail(patientRequest.getEmail())).thenReturn(false);
            when(patientMapper.toEntity(patientRequest)).thenReturn(patient);
            when(patientRepository.save(patient)).thenReturn(patient);
            when(patientMapper.toResponse(patient)).thenReturn(patientResponse);

            // When
            PatientResponse result = patientService.createPatient(patientRequest);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(1L);
            assertThat(result.getFirstName()).isEqualTo("John");
            assertThat(result.getLastName()).isEqualTo("Doe");
            assertThat(result.getEmail()).isEqualTo("john.doe@example.com");
            
            verify(patientRepository).existsByEmail(patientRequest.getEmail());
            verify(patientRepository).save(patient);
        }

        @Test
        @DisplayName("Should throw DuplicateEmailException when email already exists")
        void shouldThrowExceptionWhenEmailExists() {
            // Given
            when(patientRepository.existsByEmail(patientRequest.getEmail())).thenReturn(true);

            // When/Then
            assertThatThrownBy(() -> patientService.createPatient(patientRequest))
                    .isInstanceOf(DuplicateEmailException.class)
                    .hasMessageContaining("john.doe@example.com");

            verify(patientRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("SCRUM-15: Patient Search & Profile Retrieval Tests")
    class GetPatientTests {

        @Test
        @DisplayName("Should return patient when valid ID provided")
        void shouldReturnPatientWhenFound() {
            // Given
            when(patientRepository.findById(1L)).thenReturn(Optional.of(patient));
            when(patientMapper.toResponse(patient)).thenReturn(patientResponse);

            // When
            PatientResponse result = patientService.getPatientById(1L);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(1L);
            assertThat(result.getFirstName()).isEqualTo("John");
            
            verify(patientRepository).findById(1L);
        }

        @Test
        @DisplayName("Should throw PatientNotFoundException when ID not found")
        void shouldThrowExceptionWhenPatientNotFound() {
            // Given
            when(patientRepository.findById(999L)).thenReturn(Optional.empty());

            // When/Then
            assertThatThrownBy(() -> patientService.getPatientById(999L))
                    .isInstanceOf(PatientNotFoundException.class)
                    .hasMessageContaining("999");
        }
    }

    @Nested
    @DisplayName("Get All Patients Tests")
    class GetAllPatientsTests {

        @Test
        @DisplayName("Should return all patients")
        void shouldReturnAllPatients() {
            // Given
            Patient patient2 = Patient.builder()
                    .id(2L)
                    .firstName("Jane")
                    .lastName("Smith")
                    .email("jane.smith@example.com")
                    .build();
            
            PatientResponse response2 = PatientResponse.builder()
                    .id(2L)
                    .firstName("Jane")
                    .lastName("Smith")
                    .email("jane.smith@example.com")
                    .build();

            when(patientRepository.findAll()).thenReturn(Arrays.asList(patient, patient2));
            when(patientMapper.toResponse(patient)).thenReturn(patientResponse);
            when(patientMapper.toResponse(patient2)).thenReturn(response2);

            // When
            List<PatientResponse> results = patientService.getAllPatients();

            // Then
            assertThat(results).hasSize(2);
            assertThat(results.get(0).getFirstName()).isEqualTo("John");
            assertThat(results.get(1).getFirstName()).isEqualTo("Jane");
        }

        @Test
        @DisplayName("Should return empty list when no patients exist")
        void shouldReturnEmptyListWhenNoPatientsExist() {
            // Given
            when(patientRepository.findAll()).thenReturn(Arrays.asList());

            // When
            List<PatientResponse> results = patientService.getAllPatients();

            // Then
            assertThat(results).isEmpty();
        }
    }

    @Nested
    @DisplayName("Update Patient Tests")
    class UpdatePatientTests {

        @Test
        @DisplayName("Should update patient successfully")
        void shouldUpdatePatientSuccessfully() {
            // Given
            PatientRequest updateRequest = PatientRequest.builder()
                    .firstName("John Updated")
                    .lastName("Doe Updated")
                    .dob(LocalDate.of(1990, 5, 15))
                    .email("john.doe@example.com")
                    .phone("+9876543210")
                    .build();

            when(patientRepository.findById(1L)).thenReturn(Optional.of(patient));
            when(patientRepository.save(patient)).thenReturn(patient);
            when(patientMapper.toResponse(patient)).thenReturn(patientResponse);

            // When
            PatientResponse result = patientService.updatePatient(1L, updateRequest);

            // Then
            assertThat(result).isNotNull();
            verify(patientMapper).updateEntity(patient, updateRequest);
            verify(patientRepository).save(patient);
        }

        @Test
        @DisplayName("Should throw exception when updating non-existent patient")
        void shouldThrowExceptionWhenUpdatingNonExistentPatient() {
            // Given
            when(patientRepository.findById(999L)).thenReturn(Optional.empty());

            // When/Then
            assertThatThrownBy(() -> patientService.updatePatient(999L, patientRequest))
                    .isInstanceOf(PatientNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("Delete Patient Tests")
    class DeletePatientTests {

        @Test
        @DisplayName("Should delete patient successfully")
        void shouldDeletePatientSuccessfully() {
            // Given
            when(patientRepository.existsById(1L)).thenReturn(true);

            // When
            patientService.deletePatient(1L);

            // Then
            verify(patientRepository).deleteById(1L);
        }

        @Test
        @DisplayName("Should throw exception when deleting non-existent patient")
        void shouldThrowExceptionWhenDeletingNonExistentPatient() {
            // Given
            when(patientRepository.existsById(999L)).thenReturn(false);

            // When/Then
            assertThatThrownBy(() -> patientService.deletePatient(999L))
                    .isInstanceOf(PatientNotFoundException.class);
            
            verify(patientRepository, never()).deleteById(any());
        }
    }
}
