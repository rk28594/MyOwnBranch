package com.sparks.patient.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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

import com.sparks.patient.dto.DoctorRequest;
import com.sparks.patient.dto.DoctorResponse;
import com.sparks.patient.entity.Doctor;
import com.sparks.patient.exception.DoctorNotFoundException;
import com.sparks.patient.mapper.DoctorMapper;
import com.sparks.patient.repository.DoctorRepository;
import com.sparks.patient.test.UnitTest;

/**
 * Unit Tests for DoctorServiceImpl
 * SCRUM-20: Doctor Profile Management
 * Tests business logic in isolation using mocks
 */
@UnitTest
@ExtendWith(MockitoExtension.class)
@DisplayName("DoctorService Unit Tests")
class DoctorServiceImplTest {

    @Mock
    private DoctorRepository doctorRepository;

    @Mock
    private DoctorMapper doctorMapper;

    @InjectMocks
    private DoctorServiceImpl doctorService;

    private DoctorRequest doctorRequest;
    private Doctor doctor;
    private DoctorResponse doctorResponse;

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

        doctorResponse = DoctorResponse.builder()
                .id(1L)
                .fullName("Dr. John Smith")
                .licenseNumber("MED-123456")
                .specialization("Cardiology")
                .deptId(1L)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Nested
    @DisplayName("Create Doctor Tests")
    class CreateDoctorTests {

        @Test
        @DisplayName("Should create doctor successfully when license number is unique")
        void shouldCreateDoctorWhenLicenseNumberIsUnique() {
            // Arrange
            when(doctorRepository.existsByLicenseNumber("MED-123456")).thenReturn(false);
            when(doctorMapper.toEntity(doctorRequest)).thenReturn(doctor);
            when(doctorRepository.save(doctor)).thenReturn(doctor);
            when(doctorMapper.toResponse(doctor)).thenReturn(doctorResponse);

            // Act
            DoctorResponse response = doctorService.createDoctor(doctorRequest);

            // Assert
            assertThat(response).isNotNull();
            assertThat(response.getId()).isEqualTo(1L);
            assertThat(response.getFullName()).isEqualTo("Dr. John Smith");
            assertThat(response.getLicenseNumber()).isEqualTo("MED-123456");
            verify(doctorRepository).save(any(Doctor.class));
        }

        @Test
        @DisplayName("Should throw exception when license number already exists")
        void shouldThrowExceptionWhenLicenseNumberExists() {
            // Arrange
            when(doctorRepository.existsByLicenseNumber("MED-123456")).thenReturn(true);

            // Act & Assert
            assertThatThrownBy(() -> doctorService.createDoctor(doctorRequest))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("already exists");
            verify(doctorRepository, never()).save(any(Doctor.class));
        }

        @Test
        @DisplayName("Should return 409 Conflict for duplicate license number")
        void shouldReturnConflictForDuplicateLicenseNumber() {
            // Arrange
            when(doctorRepository.existsByLicenseNumber("MED-123456")).thenReturn(true);

            // Act & Assert
            assertThatThrownBy(() -> doctorService.createDoctor(doctorRequest))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("Get Doctor Tests")
    class GetDoctorTests {

        @Test
        @DisplayName("Should get doctor by ID successfully")
        void shouldGetDoctorById() {
            // Arrange
            when(doctorRepository.findById(1L)).thenReturn(Optional.of(doctor));
            when(doctorMapper.toResponse(doctor)).thenReturn(doctorResponse);

            // Act
            DoctorResponse response = doctorService.getDoctorById(1L);

            // Assert
            assertThat(response).isNotNull();
            assertThat(response.getId()).isEqualTo(1L);
            assertThat(response.getFullName()).isEqualTo("Dr. John Smith");
        }

        @Test
        @DisplayName("Should throw exception when doctor not found by ID")
        void shouldThrowExceptionWhenDoctorNotFoundById() {
            // Arrange
            when(doctorRepository.findById(99L)).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> doctorService.getDoctorById(99L))
                    .isInstanceOf(DoctorNotFoundException.class)
                    .hasMessageContaining("not found");
        }

        @Test
        @DisplayName("Should get doctor by license number successfully")
        void shouldGetDoctorByLicenseNumber() {
            // Arrange
            when(doctorRepository.findByLicenseNumber("MED-123456")).thenReturn(Optional.of(doctor));
            when(doctorMapper.toResponse(doctor)).thenReturn(doctorResponse);

            // Act
            DoctorResponse response = doctorService.getDoctorByLicenseNumber("MED-123456");

            // Assert
            assertThat(response).isNotNull();
            assertThat(response.getLicenseNumber()).isEqualTo("MED-123456");
        }

        @Test
        @DisplayName("Should throw exception when doctor not found by license number")
        void shouldThrowExceptionWhenDoctorNotFoundByLicenseNumber() {
            // Arrange
            when(doctorRepository.findByLicenseNumber("INVALID")).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> doctorService.getDoctorByLicenseNumber("INVALID"))
                    .isInstanceOf(DoctorNotFoundException.class)
                    .hasMessageContaining("not found");
        }

        @Test
        @DisplayName("Should get all doctors successfully")
        void shouldGetAllDoctors() {
            // Arrange
            Doctor doctor2 = Doctor.builder()
                    .id(2L)
                    .fullName("Dr. Jane Doe")
                    .licenseNumber("MED-654321")
                    .specialization("Neurology")
                    .deptId(2L)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();

            DoctorResponse doctorResponse2 = DoctorResponse.builder()
                    .id(2L)
                    .fullName("Dr. Jane Doe")
                    .licenseNumber("MED-654321")
                    .specialization("Neurology")
                    .deptId(2L)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();

            when(doctorRepository.findAll()).thenReturn(Arrays.asList(doctor, doctor2));
            when(doctorMapper.toResponse(doctor)).thenReturn(doctorResponse);
            when(doctorMapper.toResponse(doctor2)).thenReturn(doctorResponse2);

            // Act
            List<DoctorResponse> responses = doctorService.getAllDoctors();

            // Assert
            assertThat(responses).hasSize(2);
            assertThat(responses.get(0).getFullName()).isEqualTo("Dr. John Smith");
            assertThat(responses.get(1).getFullName()).isEqualTo("Dr. Jane Doe");
        }
    }

    @Nested
    @DisplayName("Update Doctor Tests")
    class UpdateDoctorTests {

        @Test
        @DisplayName("Should update doctor successfully when license number is not changed")
        void shouldUpdateDoctorWhenLicenseNumberNotChanged() {
            // Arrange
            when(doctorRepository.findById(1L)).thenReturn(Optional.of(doctor));
            when(doctorRepository.save(doctor)).thenReturn(doctor);
            when(doctorMapper.toResponse(doctor)).thenReturn(doctorResponse);

            // Act
            DoctorResponse response = doctorService.updateDoctor(1L, doctorRequest);

            // Assert
            assertThat(response).isNotNull();
            assertThat(response.getId()).isEqualTo(1L);
            verify(doctorRepository).save(any(Doctor.class));
        }

        @Test
        @DisplayName("Should update doctor successfully when license number is changed to new unique number")
        void shouldUpdateDoctorWhenLicenseNumberChanged() {
            // Arrange
            DoctorRequest updateRequest = DoctorRequest.builder()
                    .fullName("Dr. John Smith Updated")
                    .licenseNumber("MED-789012")
                    .specialization("Cardiology")
                    .deptId(1L)
                    .build();

            when(doctorRepository.findById(1L)).thenReturn(Optional.of(doctor));
            when(doctorRepository.existsByLicenseNumber("MED-789012")).thenReturn(false);
            when(doctorRepository.save(doctor)).thenReturn(doctor);
            when(doctorMapper.toResponse(doctor)).thenReturn(doctorResponse);

            // Act
            DoctorResponse response = doctorService.updateDoctor(1L, updateRequest);

            // Assert
            assertThat(response).isNotNull();
            verify(doctorRepository).save(any(Doctor.class));
        }

        @Test
        @DisplayName("Should throw exception when updating with existing license number")
        void shouldThrowExceptionWhenUpdatingWithExistingLicenseNumber() {
            // Arrange
            DoctorRequest updateRequest = DoctorRequest.builder()
                    .fullName("Dr. John Smith")
                    .licenseNumber("MED-EXISTING")
                    .specialization("Cardiology")
                    .deptId(1L)
                    .build();

            when(doctorRepository.findById(1L)).thenReturn(Optional.of(doctor));
            when(doctorRepository.existsByLicenseNumber("MED-EXISTING")).thenReturn(true);

            // Act & Assert
            assertThatThrownBy(() -> doctorService.updateDoctor(1L, updateRequest))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("already exists");
            verify(doctorRepository, never()).save(any(Doctor.class));
        }

        @Test
        @DisplayName("Should throw exception when doctor not found for update")
        void shouldThrowExceptionWhenDoctorNotFoundForUpdate() {
            // Arrange
            when(doctorRepository.findById(99L)).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> doctorService.updateDoctor(99L, doctorRequest))
                    .isInstanceOf(DoctorNotFoundException.class)
                    .hasMessageContaining("not found");
        }
    }

    @Nested
    @DisplayName("Delete Doctor Tests")
    class DeleteDoctorTests {

        @Test
        @DisplayName("Should delete doctor successfully")
        void shouldDeleteDoctorSuccessfully() {
            // Arrange
            when(doctorRepository.findById(1L)).thenReturn(Optional.of(doctor));

            // Act
            doctorService.deleteDoctor(1L);

            // Assert
            verify(doctorRepository).delete(doctor);
        }

        @Test
        @DisplayName("Should throw exception when doctor not found for deletion")
        void shouldThrowExceptionWhenDoctorNotFoundForDeletion() {
            // Arrange
            when(doctorRepository.findById(99L)).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> doctorService.deleteDoctor(99L))
                    .isInstanceOf(DoctorNotFoundException.class)
                    .hasMessageContaining("not found");
            verify(doctorRepository, never()).delete(any(Doctor.class));
        }
    }
}
