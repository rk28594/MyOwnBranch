package com.hospital.management.service;

import com.hospital.management.dto.PatientRequest;
import com.hospital.management.dto.PatientResponse;
import com.hospital.management.entity.Patient;
import com.hospital.management.exception.DuplicateResourceException;
import com.hospital.management.exception.ResourceNotFoundException;
import com.hospital.management.repository.PatientRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Unit tests for PatientService
 * Tests cover all business logic including edge cases
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("PatientService Unit Tests")
class PatientServiceTest {

    @Mock
    private PatientRepository patientRepository;

    @InjectMocks
    private PatientService patientService;

    private PatientRequest validRequest;
    private Patient validPatient;

    @BeforeEach
    void setUp() {
        validRequest = PatientRequest.builder()
                .firstName("John")
                .lastName("Doe")
                .dob(LocalDate.of(1990, 1, 15))
                .email("john.doe@example.com")
                .phone("+1234567890")
                .build();

        validPatient = Patient.builder()
                .id(1L)
                .firstName("John")
                .lastName("Doe")
                .dob(LocalDate.of(1990, 1, 15))
                .email("john.doe@example.com")
                .phone("+1234567890")
                .createdAt(LocalDate.now())
                .updatedAt(LocalDate.now())
                .build();
    }

    @Nested
    @DisplayName("Create Patient Tests")
    class CreatePatientTests {

        @Test
        @DisplayName("Should create patient successfully with valid data")
        void shouldCreatePatientSuccessfully() {
            // Arrange
            when(patientRepository.existsByEmail(validRequest.getEmail())).thenReturn(false);
            when(patientRepository.save(any(Patient.class))).thenReturn(validPatient);

            // Act
            PatientResponse response = patientService.createPatient(validRequest);

            // Assert
            assertThat(response).isNotNull();
            assertThat(response.getId()).isEqualTo(1L);
            assertThat(response.getFirstName()).isEqualTo("John");
            assertThat(response.getLastName()).isEqualTo("Doe");
            assertThat(response.getEmail()).isEqualTo("john.doe@example.com");
            
            verify(patientRepository).existsByEmail(validRequest.getEmail());
            verify(patientRepository).save(any(Patient.class));
        }

        @Test
        @DisplayName("Should throw DuplicateResourceException when email already exists")
        void shouldThrowExceptionWhenEmailExists() {
            // Arrange
            when(patientRepository.existsByEmail(validRequest.getEmail())).thenReturn(true);

            // Act & Assert
            assertThatThrownBy(() -> patientService.createPatient(validRequest))
                    .isInstanceOf(DuplicateResourceException.class)
                    .hasMessageContaining("Patient already exists with email");

            verify(patientRepository).existsByEmail(validRequest.getEmail());
            verify(patientRepository, never()).save(any(Patient.class));
        }

        @Test
        @DisplayName("Should capture and save patient with correct field values")
        void shouldSavePatientWithCorrectValues() {
            // Arrange
            when(patientRepository.existsByEmail(anyString())).thenReturn(false);
            when(patientRepository.save(any(Patient.class))).thenReturn(validPatient);
            
            ArgumentCaptor<Patient> patientCaptor = ArgumentCaptor.forClass(Patient.class);

            // Act
            patientService.createPatient(validRequest);

            // Assert
            verify(patientRepository).save(patientCaptor.capture());
            Patient capturedPatient = patientCaptor.getValue();
            
            assertThat(capturedPatient.getFirstName()).isEqualTo(validRequest.getFirstName());
            assertThat(capturedPatient.getLastName()).isEqualTo(validRequest.getLastName());
            assertThat(capturedPatient.getDob()).isEqualTo(validRequest.getDob());
            assertThat(capturedPatient.getEmail()).isEqualTo(validRequest.getEmail());
            assertThat(capturedPatient.getPhone()).isEqualTo(validRequest.getPhone());
        }

        @Test
        @DisplayName("Should handle patient with minimum valid data")
        void shouldHandleMinimumValidData() {
            // Arrange
            PatientRequest minimalRequest = PatientRequest.builder()
                    .firstName("J")
                    .lastName("D")
                    .dob(LocalDate.of(2000, 1, 1))
                    .email("j@d.com")
                    .phone("1")
                    .build();

            Patient minimalPatient = Patient.builder()
                    .id(1L)
                    .firstName("J")
                    .lastName("D")
                    .dob(LocalDate.of(2000, 1, 1))
                    .email("j@d.com")
                    .phone("1")
                    .createdAt(LocalDate.now())
                    .updatedAt(LocalDate.now())
                    .build();

            when(patientRepository.existsByEmail(anyString())).thenReturn(false);
            when(patientRepository.save(any(Patient.class))).thenReturn(minimalPatient);

            // Act
            PatientResponse response = patientService.createPatient(minimalRequest);

            // Assert
            assertThat(response).isNotNull();
            assertThat(response.getFirstName()).isEqualTo("J");
            assertThat(response.getEmail()).isEqualTo("j@d.com");
        }
    }

    @Nested
    @DisplayName("Get Patient By ID Tests")
    class GetPatientByIdTests {

        @Test
        @DisplayName("Should return patient when ID exists")
        void shouldReturnPatientWhenIdExists() {
            // Arrange
            when(patientRepository.findById(1L)).thenReturn(Optional.of(validPatient));

            // Act
            PatientResponse response = patientService.getPatientById(1L);

            // Assert
            assertThat(response).isNotNull();
            assertThat(response.getId()).isEqualTo(1L);
            assertThat(response.getEmail()).isEqualTo("john.doe@example.com");
            
            verify(patientRepository).findById(1L);
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when ID does not exist")
        void shouldThrowExceptionWhenIdNotFound() {
            // Arrange
            when(patientRepository.findById(999L)).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> patientService.getPatientById(999L))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Patient not found with id: '999'");

            verify(patientRepository).findById(999L);
        }

        @Test
        @DisplayName("Should handle zero ID")
        void shouldHandleZeroId() {
            // Arrange
            when(patientRepository.findById(0L)).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> patientService.getPatientById(0L))
                    .isInstanceOf(ResourceNotFoundException.class);

            verify(patientRepository).findById(0L);
        }

        @Test
        @DisplayName("Should handle negative ID")
        void shouldHandleNegativeId() {
            // Arrange
            when(patientRepository.findById(-1L)).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> patientService.getPatientById(-1L))
                    .isInstanceOf(ResourceNotFoundException.class);

            verify(patientRepository).findById(-1L);
        }
    }

    @Nested
    @DisplayName("Get All Patients Tests")
    class GetAllPatientsTests {

        @Test
        @DisplayName("Should return all patients")
        void shouldReturnAllPatients() {
            // Arrange
            Patient patient2 = Patient.builder()
                    .id(2L)
                    .firstName("Jane")
                    .lastName("Smith")
                    .dob(LocalDate.of(1985, 5, 20))
                    .email("jane.smith@example.com")
                    .phone("+9876543210")
                    .createdAt(LocalDate.now())
                    .updatedAt(LocalDate.now())
                    .build();

            when(patientRepository.findAll()).thenReturn(Arrays.asList(validPatient, patient2));

            // Act
            List<PatientResponse> responses = patientService.getAllPatients();

            // Assert
            assertThat(responses).hasSize(2);
            assertThat(responses.get(0).getId()).isEqualTo(1L);
            assertThat(responses.get(1).getId()).isEqualTo(2L);
            
            verify(patientRepository).findAll();
        }

        @Test
        @DisplayName("Should return empty list when no patients exist")
        void shouldReturnEmptyListWhenNoPatientsExist() {
            // Arrange
            when(patientRepository.findAll()).thenReturn(Arrays.asList());

            // Act
            List<PatientResponse> responses = patientService.getAllPatients();

            // Assert
            assertThat(responses).isEmpty();
            verify(patientRepository).findAll();
        }

        @Test
        @DisplayName("Should handle large number of patients")
        void shouldHandleLargeNumberOfPatients() {
            // Arrange
            List<Patient> largeList = new ArrayList<>();
            for (int i = 0; i < 1000; i++) {
                Patient patient = new Patient();
                patient.setId((long) i);
                patient.setFirstName("Patient" + i);
                patient.setLastName("Test" + i);
                patient.setEmail("patient" + i + "@test.com");
                patient.setPhone("+123456789" + i);
                patient.setDob(LocalDate.of(1980, 1, 1));
                patient.setCreatedAt(LocalDate.now());
                patient.setUpdatedAt(LocalDate.now());
                largeList.add(patient);
            }
            when(patientRepository.findAll()).thenReturn(largeList);

            // Act
            List<PatientResponse> responses = patientService.getAllPatients();

            // Assert
            assertThat(responses).hasSize(1000);
            verify(patientRepository).findAll();
        }
    }

    @Nested
    @DisplayName("Update Patient Tests")
    class UpdatePatientTests {

        @Test
        @DisplayName("Should update patient successfully")
        void shouldUpdatePatientSuccessfully() {
            // Arrange
            PatientRequest updateRequest = PatientRequest.builder()
                    .firstName("John Updated")
                    .lastName("Doe Updated")
                    .dob(LocalDate.of(1990, 1, 15))
                    .email("john.updated@example.com")
                    .phone("+1111111111")
                    .build();

            Patient updatedPatient = Patient.builder()
                    .id(1L)
                    .firstName("John Updated")
                    .lastName("Doe Updated")
                    .dob(LocalDate.of(1990, 1, 15))
                    .email("john.updated@example.com")
                    .phone("+1111111111")
                    .createdAt(LocalDate.now())
                    .updatedAt(LocalDate.now())
                    .build();

            when(patientRepository.findById(1L)).thenReturn(Optional.of(validPatient));
            when(patientRepository.existsByEmail("john.updated@example.com")).thenReturn(false);
            when(patientRepository.save(any(Patient.class))).thenReturn(updatedPatient);

            // Act
            PatientResponse response = patientService.updatePatient(1L, updateRequest);

            // Assert
            assertThat(response.getFirstName()).isEqualTo("John Updated");
            assertThat(response.getEmail()).isEqualTo("john.updated@example.com");
            
            verify(patientRepository).findById(1L);
            verify(patientRepository).save(any(Patient.class));
        }

        @Test
        @DisplayName("Should throw exception when updating non-existent patient")
        void shouldThrowExceptionWhenUpdatingNonExistentPatient() {
            // Arrange
            when(patientRepository.findById(999L)).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> patientService.updatePatient(999L, validRequest))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Patient not found with id: '999'");

            verify(patientRepository).findById(999L);
            verify(patientRepository, never()).save(any(Patient.class));
        }

        @Test
        @DisplayName("Should allow updating with same email")
        void shouldAllowUpdatingWithSameEmail() {
            // Arrange
            when(patientRepository.findById(1L)).thenReturn(Optional.of(validPatient));
            when(patientRepository.save(any(Patient.class))).thenReturn(validPatient);

            // Act
            PatientResponse response = patientService.updatePatient(1L, validRequest);

            // Assert
            assertThat(response).isNotNull();
            verify(patientRepository).findById(1L);
            verify(patientRepository, never()).existsByEmail(anyString());
        }

        @Test
        @DisplayName("Should throw exception when email is taken by another patient")
        void shouldThrowExceptionWhenEmailTakenByAnotherPatient() {
            // Arrange
            PatientRequest updateRequest = PatientRequest.builder()
                    .firstName("John")
                    .lastName("Doe")
                    .dob(LocalDate.of(1990, 1, 15))
                    .email("another@example.com")
                    .phone("+1234567890")
                    .build();

            when(patientRepository.findById(1L)).thenReturn(Optional.of(validPatient));
            when(patientRepository.existsByEmail("another@example.com")).thenReturn(true);

            // Act & Assert
            assertThatThrownBy(() -> patientService.updatePatient(1L, updateRequest))
                    .isInstanceOf(DuplicateResourceException.class)
                    .hasMessageContaining("Patient already exists with email");

            verify(patientRepository).findById(1L);
            verify(patientRepository).existsByEmail("another@example.com");
            verify(patientRepository, never()).save(any(Patient.class));
        }

        @Test
        @DisplayName("Should update all fields correctly")
        void shouldUpdateAllFieldsCorrectly() {
            // Arrange
            PatientRequest updateRequest = PatientRequest.builder()
                    .firstName("Updated First")
                    .lastName("Updated Last")
                    .dob(LocalDate.of(1995, 12, 31))
                    .email("updated@example.com")
                    .phone("+9999999999")
                    .build();

            when(patientRepository.findById(1L)).thenReturn(Optional.of(validPatient));
            when(patientRepository.existsByEmail("updated@example.com")).thenReturn(false);
            when(patientRepository.save(any(Patient.class))).thenAnswer(invocation -> invocation.getArgument(0));

            ArgumentCaptor<Patient> patientCaptor = ArgumentCaptor.forClass(Patient.class);

            // Act
            patientService.updatePatient(1L, updateRequest);

            // Assert
            verify(patientRepository).save(patientCaptor.capture());
            Patient capturedPatient = patientCaptor.getValue();
            
            assertThat(capturedPatient.getFirstName()).isEqualTo("Updated First");
            assertThat(capturedPatient.getLastName()).isEqualTo("Updated Last");
            assertThat(capturedPatient.getDob()).isEqualTo(LocalDate.of(1995, 12, 31));
            assertThat(capturedPatient.getEmail()).isEqualTo("updated@example.com");
            assertThat(capturedPatient.getPhone()).isEqualTo("+9999999999");
        }
    }

    @Nested
    @DisplayName("Delete Patient Tests")
    class DeletePatientTests {

        @Test
        @DisplayName("Should delete patient successfully")
        void shouldDeletePatientSuccessfully() {
            // Arrange
            when(patientRepository.existsById(1L)).thenReturn(true);
            doNothing().when(patientRepository).deleteById(1L);

            // Act
            patientService.deletePatient(1L);

            // Assert
            verify(patientRepository).existsById(1L);
            verify(patientRepository).deleteById(1L);
        }

        @Test
        @DisplayName("Should throw exception when deleting non-existent patient")
        void shouldThrowExceptionWhenDeletingNonExistentPatient() {
            // Arrange
            when(patientRepository.existsById(999L)).thenReturn(false);

            // Act & Assert
            assertThatThrownBy(() -> patientService.deletePatient(999L))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Patient not found with id: '999'");

            verify(patientRepository).existsById(999L);
            verify(patientRepository, never()).deleteById(anyLong());
        }

        @Test
        @DisplayName("Should handle deletion of patient with ID 0")
        void shouldHandleDeletionWithZeroId() {
            // Arrange
            when(patientRepository.existsById(0L)).thenReturn(false);

            // Act & Assert
            assertThatThrownBy(() -> patientService.deletePatient(0L))
                    .isInstanceOf(ResourceNotFoundException.class);

            verify(patientRepository).existsById(0L);
            verify(patientRepository, never()).deleteById(anyLong());
        }

        @Test
        @DisplayName("Should verify deleteById is called exactly once")
        void shouldCallDeleteByIdExactlyOnce() {
            // Arrange
            when(patientRepository.existsById(1L)).thenReturn(true);
            doNothing().when(patientRepository).deleteById(1L);

            // Act
            patientService.deletePatient(1L);

            // Assert
            verify(patientRepository, times(1)).deleteById(1L);
        }
    }

    @Nested
    @DisplayName("Edge Cases and Boundary Tests")
    class EdgeCasesTests {

        @Test
        @DisplayName("Should handle patient with very long names")
        void shouldHandleVeryLongNames() {
            // Arrange
            String longName = "A".repeat(255);
            PatientRequest longNameRequest = PatientRequest.builder()
                    .firstName(longName)
                    .lastName(longName)
                    .dob(LocalDate.of(1990, 1, 15))
                    .email("long@example.com")
                    .phone("+1234567890")
                    .build();

            Patient longNamePatient = Patient.builder()
                    .id(1L)
                    .firstName(longName)
                    .lastName(longName)
                    .dob(LocalDate.of(1990, 1, 15))
                    .email("long@example.com")
                    .phone("+1234567890")
                    .createdAt(LocalDate.now())
                    .updatedAt(LocalDate.now())
                    .build();

            when(patientRepository.existsByEmail(anyString())).thenReturn(false);
            when(patientRepository.save(any(Patient.class))).thenReturn(longNamePatient);

            // Act
            PatientResponse response = patientService.createPatient(longNameRequest);

            // Assert
            assertThat(response.getFirstName()).hasSize(255);
        }

        @Test
        @DisplayName("Should handle patient with very old date of birth")
        void shouldHandleVeryOldDateOfBirth() {
            // Arrange
            LocalDate oldDate = LocalDate.of(1900, 1, 1);
            PatientRequest oldDobRequest = PatientRequest.builder()
                    .firstName("Old")
                    .lastName("Person")
                    .dob(oldDate)
                    .email("old@example.com")
                    .phone("+1234567890")
                    .build();

            Patient oldDobPatient = Patient.builder()
                    .id(1L)
                    .firstName("Old")
                    .lastName("Person")
                    .dob(oldDate)
                    .email("old@example.com")
                    .phone("+1234567890")
                    .createdAt(LocalDate.now())
                    .updatedAt(LocalDate.now())
                    .build();

            when(patientRepository.existsByEmail(anyString())).thenReturn(false);
            when(patientRepository.save(any(Patient.class))).thenReturn(oldDobPatient);

            // Act
            PatientResponse response = patientService.createPatient(oldDobRequest);

            // Assert
            assertThat(response.getDob()).isEqualTo(oldDate);
        }

        @Test
        @DisplayName("Should handle special characters in names")
        void shouldHandleSpecialCharactersInNames() {
            // Arrange
            PatientRequest specialCharsRequest = PatientRequest.builder()
                    .firstName("Jean-François")
                    .lastName("O'Brien-Smith")
                    .dob(LocalDate.of(1990, 1, 15))
                    .email("special@example.com")
                    .phone("+1234567890")
                    .build();

            Patient specialCharsPatient = Patient.builder()
                    .id(1L)
                    .firstName("Jean-François")
                    .lastName("O'Brien-Smith")
                    .dob(LocalDate.of(1990, 1, 15))
                    .email("special@example.com")
                    .phone("+1234567890")
                    .createdAt(LocalDate.now())
                    .updatedAt(LocalDate.now())
                    .build();

            when(patientRepository.existsByEmail(anyString())).thenReturn(false);
            when(patientRepository.save(any(Patient.class))).thenReturn(specialCharsPatient);

            // Act
            PatientResponse response = patientService.createPatient(specialCharsRequest);

            // Assert
            assertThat(response.getFirstName()).isEqualTo("Jean-François");
            assertThat(response.getLastName()).isEqualTo("O'Brien-Smith");
        }

        @Test
        @DisplayName("Should handle international phone numbers")
        void shouldHandleInternationalPhoneNumbers() {
            // Arrange
            PatientRequest intlPhoneRequest = PatientRequest.builder()
                    .firstName("John")
                    .lastName("Doe")
                    .dob(LocalDate.of(1990, 1, 15))
                    .email("intl@example.com")
                    .phone("+44 20 7946 0958")
                    .build();

            Patient intlPhonePatient = Patient.builder()
                    .id(1L)
                    .firstName("John")
                    .lastName("Doe")
                    .dob(LocalDate.of(1990, 1, 15))
                    .email("intl@example.com")
                    .phone("+44 20 7946 0958")
                    .createdAt(LocalDate.now())
                    .updatedAt(LocalDate.now())
                    .build();

            when(patientRepository.existsByEmail(anyString())).thenReturn(false);
            when(patientRepository.save(any(Patient.class))).thenReturn(intlPhonePatient);

            // Act
            PatientResponse response = patientService.createPatient(intlPhoneRequest);

            // Assert
            assertThat(response.getPhone()).isEqualTo("+44 20 7946 0958");
        }
    }
}
