package com.sparks.patient.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import com.sparks.patient.dto.PatientRequest;
import com.sparks.patient.dto.PatientResponse;
import com.sparks.patient.entity.Patient;
import com.sparks.patient.exception.DuplicateEmailException;
import com.sparks.patient.exception.PatientNotFoundException;
import com.sparks.patient.mapper.PatientMapper;
import com.sparks.patient.repository.PatientRepository;
import com.sparks.patient.test.UnitTest;

/**
 * Unit Tests for PatientServiceImpl
 * Tests business logic in isolation using mocks
 */
@UnitTest
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
    @DisplayName("Get All Patients with Pagination Tests")
    class GetAllPatientsPaginatedTests {

        @Test
        @DisplayName("Should return paginated patients with default page size")
        void shouldReturnPaginatedPatients() {
            // Given
            Pageable pageable = PageRequest.of(0, 20);
            Patient patient2 = Patient.builder()
                    .id(2L)
                    .firstName("Jane")
                    .lastName("Smith")
                    .email("jane.smith@example.com")
                    .build();

            Page<Patient> patientPage = new PageImpl<>(
                Arrays.asList(patient, patient2), pageable, 2);

            when(patientRepository.findAll(pageable)).thenReturn(patientPage);
            when(patientMapper.toResponse(any(Patient.class))).thenReturn(patientResponse);

            // When
            Page<PatientResponse> result = patientService.getAllPatients(pageable);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getTotalElements()).isEqualTo(2);
            assertThat(result.getTotalPages()).isEqualTo(1);
            assertThat(result.getSize()).isEqualTo(20);

            verify(patientRepository).findAll(pageable);
        }

        @Test
        @DisplayName("Should return empty page when no patients exist")
        void shouldReturnEmptyPageWhenNoPatientsExist() {
            // Given
            Pageable pageable = PageRequest.of(0, 20);
            Page<Patient> emptyPage = new PageImpl<>(Arrays.asList(), pageable, 0);

            when(patientRepository.findAll(pageable)).thenReturn(emptyPage);

            // When
            Page<PatientResponse> result = patientService.getAllPatients(pageable);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getTotalElements()).isEqualTo(0);
            assertThat(result.isEmpty()).isTrue();
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

    @Nested
    @DisplayName("Search Patient by Phone Tests")
    class SearchPatientByPhoneTests {

        @Test
        @DisplayName("Should find patient by phone number successfully")
        void shouldFindPatientByPhoneSuccessfully() {
            // Given
            String phoneNumber = "+1234567890";
            when(patientRepository.findByPhone(phoneNumber)).thenReturn(Optional.of(patient));
            when(patientMapper.toResponse(patient)).thenReturn(patientResponse);

            // When
            PatientResponse result = patientService.getPatientByPhone(phoneNumber);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(1L);
            assertThat(result.getPhone()).isEqualTo(phoneNumber);

            verify(patientRepository).findByPhone(phoneNumber);
            verify(patientMapper).toResponse(patient);
        }

        @Test
        @DisplayName("Should throw exception when patient not found by phone")
        void shouldThrowExceptionWhenPatientNotFoundByPhone() {
            // Given
            String phoneNumber = "+9999999999";
            when(patientRepository.findByPhone(phoneNumber)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> patientService.getPatientByPhone(phoneNumber))
                    .isInstanceOf(PatientNotFoundException.class)
                    .hasMessageContaining("Patient not found with phone");

            verify(patientRepository).findByPhone(phoneNumber);
        }
    }

    @Nested
    @DisplayName("Search Patients by Last Name Tests")
    class SearchPatientsByLastNameTests {

        @Test
        @DisplayName("Should find patients by last name successfully")
        void shouldFindPatientsByLastNameSuccessfully() {
            // Given
            String lastName = "Doe";
            Patient patient2 = Patient.builder()
                    .id(2L)
                    .firstName("Jane")
                    .lastName("Doe")
                    .dob(LocalDate.of(1992, 3, 20))
                    .email("jane.doe@example.com")
                    .phone("+9876543210")
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();

            List<Patient> patients = Arrays.asList(patient, patient2);
            when(patientRepository.findByLastName(lastName)).thenReturn(patients);
            when(patientMapper.toResponse(any(Patient.class))).thenReturn(patientResponse);

            // When
            List<PatientResponse> results = patientService.getPatientsByLastName(lastName);

            // Then
            assertThat(results).isNotNull();
            assertThat(results).hasSize(2);

            verify(patientRepository).findByLastName(lastName);
            verify(patientMapper, times(2)).toResponse(any(Patient.class));
        }

        @Test
        @DisplayName("Should return empty list when no patients found by last name")
        void shouldReturnEmptyListWhenNoPatientsFoundByLastName() {
            // Given
            String lastName = "NonExistent";
            when(patientRepository.findByLastName(lastName)).thenReturn(Arrays.asList());

            // When
            List<PatientResponse> results = patientService.getPatientsByLastName(lastName);

            // Then
            assertThat(results).isNotNull();
            assertThat(results).isEmpty();

            verify(patientRepository).findByLastName(lastName);
            verify(patientMapper, never()).toResponse(any(Patient.class));
        }

        @Test
        @DisplayName("Should find single patient when only one matches last name")
        void shouldFindSinglePatientWhenOnlyOneMatchesLastName() {
            // Given
            String lastName = "Unique";
            when(patientRepository.findByLastName(lastName)).thenReturn(Arrays.asList(patient));
            when(patientMapper.toResponse(patient)).thenReturn(patientResponse);

            // When
            List<PatientResponse> results = patientService.getPatientsByLastName(lastName);

            // Then
            assertThat(results).isNotNull();
            assertThat(results).hasSize(1);
            assertThat(results.get(0).getId()).isEqualTo(1L);

            verify(patientRepository).findByLastName(lastName);
            verify(patientMapper).toResponse(patient);
        }
    }
}
