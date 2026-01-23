package com.hospital.management.service;

import com.hospital.management.dto.DoctorRequest;
import com.hospital.management.dto.DoctorResponse;
import com.hospital.management.entity.Doctor;
import com.hospital.management.exception.DuplicateResourceException;
import com.hospital.management.exception.ResourceNotFoundException;
import com.hospital.management.repository.DoctorRepository;
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
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Unit tests for DoctorService - Story SCRUM-20: Doctor Profile Management
 * Test Scenario: When two doctors are registered with the same license, Then a 409 Conflict is returned
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("DoctorService Unit Tests - SCRUM-20")
class DoctorServiceTest {

    @Mock
    private DoctorRepository doctorRepository;

    @InjectMocks
    private DoctorService doctorService;

    private DoctorRequest validRequest;
    private Doctor validDoctor;

    @BeforeEach
    void setUp() {
        validRequest = DoctorRequest.builder()
                .fullName("Dr. John Smith")
                .licenseNumber("LIC-12345")
                .specialization("Cardiology")
                .deptId(1L)
                .build();

        validDoctor = Doctor.builder()
                .id(1L)
                .fullName("Dr. John Smith")
                .licenseNumber("LIC-12345")
                .specialization("Cardiology")
                .deptId(1L)
                .createdAt(LocalDate.now())
                .updatedAt(LocalDate.now())
                .build();
    }

    @Nested
    @DisplayName("Create Doctor Tests")
    class CreateDoctorTests {

        @Test
        @DisplayName("Should create doctor successfully with valid data")
        void shouldCreateDoctorSuccessfully() {
            // Arrange
            when(doctorRepository.existsByLicenseNumber(validRequest.getLicenseNumber())).thenReturn(false);
            when(doctorRepository.save(any(Doctor.class))).thenReturn(validDoctor);

            // Act
            DoctorResponse response = doctorService.createDoctor(validRequest);

            // Assert
            assertThat(response).isNotNull();
            assertThat(response.getId()).isEqualTo(1L);
            assertThat(response.getFullName()).isEqualTo("Dr. John Smith");
            assertThat(response.getLicenseNumber()).isEqualTo("LIC-12345");
            assertThat(response.getSpecialization()).isEqualTo("Cardiology");
            
            verify(doctorRepository).existsByLicenseNumber(validRequest.getLicenseNumber());
            verify(doctorRepository).save(any(Doctor.class));
        }

        @Test
        @DisplayName("Should throw DuplicateResourceException when license number already exists - SCRUM-20 AC")
        void shouldThrowExceptionWhenLicenseNumberExists() {
            // Arrange
            when(doctorRepository.existsByLicenseNumber(validRequest.getLicenseNumber())).thenReturn(true);

            // Act & Assert
            assertThatThrownBy(() -> doctorService.createDoctor(validRequest))
                    .isInstanceOf(DuplicateResourceException.class)
                    .hasMessageContaining("Doctor already exists with licenseNumber");

            verify(doctorRepository).existsByLicenseNumber(validRequest.getLicenseNumber());
            verify(doctorRepository, never()).save(any(Doctor.class));
        }

        @Test
        @DisplayName("Should capture and save doctor with correct field values")
        void shouldSaveDoctorWithCorrectValues() {
            // Arrange
            when(doctorRepository.existsByLicenseNumber(anyString())).thenReturn(false);
            when(doctorRepository.save(any(Doctor.class))).thenReturn(validDoctor);
            
            ArgumentCaptor<Doctor> doctorCaptor = ArgumentCaptor.forClass(Doctor.class);

            // Act
            doctorService.createDoctor(validRequest);

            // Assert
            verify(doctorRepository).save(doctorCaptor.capture());
            Doctor capturedDoctor = doctorCaptor.getValue();
            
            assertThat(capturedDoctor.getFullName()).isEqualTo(validRequest.getFullName());
            assertThat(capturedDoctor.getLicenseNumber()).isEqualTo(validRequest.getLicenseNumber());
            assertThat(capturedDoctor.getSpecialization()).isEqualTo(validRequest.getSpecialization());
            assertThat(capturedDoctor.getDeptId()).isEqualTo(validRequest.getDeptId());
        }
    }

    @Nested
    @DisplayName("Get Doctor By ID Tests")
    class GetDoctorByIdTests {

        @Test
        @DisplayName("Should return doctor when ID exists")
        void shouldReturnDoctorWhenIdExists() {
            // Arrange
            when(doctorRepository.findById(1L)).thenReturn(Optional.of(validDoctor));

            // Act
            DoctorResponse response = doctorService.getDoctorById(1L);

            // Assert
            assertThat(response).isNotNull();
            assertThat(response.getId()).isEqualTo(1L);
            assertThat(response.getLicenseNumber()).isEqualTo("LIC-12345");
            
            verify(doctorRepository).findById(1L);
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when ID does not exist")
        void shouldThrowExceptionWhenIdNotFound() {
            // Arrange
            when(doctorRepository.findById(999L)).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> doctorService.getDoctorById(999L))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Doctor not found with id: '999'");

            verify(doctorRepository).findById(999L);
        }
    }

    @Nested
    @DisplayName("Get All Doctors Tests")
    class GetAllDoctorsTests {

        @Test
        @DisplayName("Should return all doctors")
        void shouldReturnAllDoctors() {
            // Arrange
            Doctor doctor2 = Doctor.builder()
                    .id(2L)
                    .fullName("Dr. Jane Doe")
                    .licenseNumber("LIC-67890")
                    .specialization("Neurology")
                    .deptId(2L)
                    .createdAt(LocalDate.now())
                    .updatedAt(LocalDate.now())
                    .build();

            when(doctorRepository.findAll()).thenReturn(Arrays.asList(validDoctor, doctor2));

            // Act
            List<DoctorResponse> responses = doctorService.getAllDoctors();

            // Assert
            assertThat(responses).hasSize(2);
            assertThat(responses.get(0).getLicenseNumber()).isEqualTo("LIC-12345");
            assertThat(responses.get(1).getLicenseNumber()).isEqualTo("LIC-67890");
        }

        @Test
        @DisplayName("Should return empty list when no doctors exist")
        void shouldReturnEmptyListWhenNoDoctors() {
            // Arrange
            when(doctorRepository.findAll()).thenReturn(List.of());

            // Act
            List<DoctorResponse> responses = doctorService.getAllDoctors();

            // Assert
            assertThat(responses).isEmpty();
        }
    }

    @Nested
    @DisplayName("Update Doctor Tests")
    class UpdateDoctorTests {

        @Test
        @DisplayName("Should update doctor successfully")
        void shouldUpdateDoctorSuccessfully() {
            // Arrange
            DoctorRequest updateRequest = DoctorRequest.builder()
                    .fullName("Dr. John Smith Jr.")
                    .licenseNumber("LIC-12345")
                    .specialization("Cardiology")
                    .deptId(2L)
                    .build();

            Doctor updatedDoctor = Doctor.builder()
                    .id(1L)
                    .fullName("Dr. John Smith Jr.")
                    .licenseNumber("LIC-12345")
                    .specialization("Cardiology")
                    .deptId(2L)
                    .createdAt(LocalDate.now())
                    .updatedAt(LocalDate.now())
                    .build();

            when(doctorRepository.findById(1L)).thenReturn(Optional.of(validDoctor));
            when(doctorRepository.save(any(Doctor.class))).thenReturn(updatedDoctor);

            // Act
            DoctorResponse response = doctorService.updateDoctor(1L, updateRequest);

            // Assert
            assertThat(response.getFullName()).isEqualTo("Dr. John Smith Jr.");
            assertThat(response.getDeptId()).isEqualTo(2L);
        }

        @Test
        @DisplayName("Should throw exception when updating to existing license number")
        void shouldThrowExceptionWhenUpdatingToExistingLicense() {
            // Arrange
            DoctorRequest updateRequest = DoctorRequest.builder()
                    .fullName("Dr. John Smith")
                    .licenseNumber("LIC-EXISTING")
                    .specialization("Cardiology")
                    .deptId(1L)
                    .build();

            when(doctorRepository.findById(1L)).thenReturn(Optional.of(validDoctor));
            when(doctorRepository.existsByLicenseNumber("LIC-EXISTING")).thenReturn(true);

            // Act & Assert
            assertThatThrownBy(() -> doctorService.updateDoctor(1L, updateRequest))
                    .isInstanceOf(DuplicateResourceException.class);
        }
    }

    @Nested
    @DisplayName("Delete Doctor Tests")
    class DeleteDoctorTests {

        @Test
        @DisplayName("Should delete doctor successfully")
        void shouldDeleteDoctorSuccessfully() {
            // Arrange
            when(doctorRepository.existsById(1L)).thenReturn(true);
            doNothing().when(doctorRepository).deleteById(1L);

            // Act
            doctorService.deleteDoctor(1L);

            // Assert
            verify(doctorRepository).deleteById(1L);
        }

        @Test
        @DisplayName("Should throw exception when deleting non-existent doctor")
        void shouldThrowExceptionWhenDeletingNonExistent() {
            // Arrange
            when(doctorRepository.existsById(999L)).thenReturn(false);

            // Act & Assert
            assertThatThrownBy(() -> doctorService.deleteDoctor(999L))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }
}
