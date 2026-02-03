package com.sparks.patient.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.sparks.patient.dto.AppointmentRequest;
import com.sparks.patient.dto.AppointmentResponse;
import com.sparks.patient.entity.Appointment;
import com.sparks.patient.entity.Doctor;
import com.sparks.patient.entity.Patient;
import com.sparks.patient.exception.AppointmentNotFoundException;
import com.sparks.patient.exception.DoctorNotFoundException;
import com.sparks.patient.exception.PatientNotFoundException;
import com.sparks.patient.mapper.AppointmentMapper;
import com.sparks.patient.repository.AppointmentRepository;
import com.sparks.patient.repository.DoctorRepository;
import com.sparks.patient.repository.PatientRepository;

/**
 * Service tests for Appointment - SCRUM-23
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Appointment Service Tests")
class AppointmentServiceImplTest {

    @Mock
    private AppointmentRepository appointmentRepository;

    @Mock
    private PatientRepository patientRepository;

    @Mock
    private DoctorRepository doctorRepository;

    @Mock
    private AppointmentMapper appointmentMapper;

    @InjectMocks
    private AppointmentServiceImpl appointmentService;

    private Patient patient;
    private Doctor doctor;
    private AppointmentRequest request;
    private Appointment appointment;
    private AppointmentResponse response;

    @BeforeEach
    void setUp() {
        patient = Patient.builder()
                .id(1L)
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@test.com")
                .phone("+1234567890")
                .dob(LocalDate.of(1990, 1, 1))
                .build();

        doctor = Doctor.builder()
                .id(2L)
                .fullName("Dr. Smith")
                .licenseNumber("LIC123456")
                .specialization("Cardiology")
                .deptId(1L)
                .build();

        LocalDateTime appointmentTime = LocalDateTime.now().plusDays(1);
        
        request = AppointmentRequest.builder()
                .patientId(1L)
                .doctorId(2L)
                .appointmentTime(appointmentTime)
                .build();

        appointment = Appointment.builder()
                .id(10L)
                .appointmentId("550e8400-e29b-41d4-a716-446655440000")
                .patient(patient)
                .doctor(doctor)
                .appointmentTime(appointmentTime)
                .status(Appointment.AppointmentStatus.SCHEDULED)
                .createdAt(LocalDateTime.now())
                .build();

        response = AppointmentResponse.builder()
                .id(10L)
                .appointmentId("550e8400-e29b-41d4-a716-446655440000")
                .patientId(1L)
                .patientName("John Doe")
                .doctorId(2L)
                .doctorName("Dr. Smith")
                .appointmentTime(appointmentTime)
                .status("SCHEDULED")
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("Should create appointment successfully and generate UUID")
    void testCreateAppointment_Success() {
        // Given
        when(patientRepository.findById(1L)).thenReturn(Optional.of(patient));
        when(doctorRepository.findById(2L)).thenReturn(Optional.of(doctor));
        when(appointmentMapper.toEntity(request, patient, doctor)).thenReturn(appointment);
        when(appointmentRepository.save(any(Appointment.class))).thenReturn(appointment);
        when(appointmentMapper.toResponse(appointment)).thenReturn(response);

        // When
        AppointmentResponse result = appointmentService.createAppointment(request);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getAppointmentId()).isNotNull();
        assertThat(result.getStatus()).isEqualTo("SCHEDULED");
        assertThat(result.getPatientId()).isEqualTo(1L);
        assertThat(result.getDoctorId()).isEqualTo(2L);

        verify(patientRepository).findById(1L);
        verify(doctorRepository).findById(2L);
        verify(appointmentRepository).save(any(Appointment.class));
    }

    @Test
    @DisplayName("Should throw PatientNotFoundException when patient not found")
    void testCreateAppointment_PatientNotFound() {
        // Given
        when(patientRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> appointmentService.createAppointment(request))
                .isInstanceOf(PatientNotFoundException.class)
                .hasMessageContaining("Patient not found with id: 1");

        verify(patientRepository).findById(1L);
    }

    @Test
    @DisplayName("Should throw DoctorNotFoundException when doctor not found")
    void testCreateAppointment_DoctorNotFound() {
        // Given
        when(patientRepository.findById(1L)).thenReturn(Optional.of(patient));
        when(doctorRepository.findById(2L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> appointmentService.createAppointment(request))
                .isInstanceOf(DoctorNotFoundException.class)
                .hasMessageContaining("Doctor not found with id: 2");

        verify(patientRepository).findById(1L);
        verify(doctorRepository).findById(2L);
    }

    @Test
    @DisplayName("Should get appointment by UUID")
    void testGetAppointmentById_Success() {
        // Given
        String appointmentId = "550e8400-e29b-41d4-a716-446655440000";
        when(appointmentRepository.findByAppointmentId(appointmentId)).thenReturn(Optional.of(appointment));
        when(appointmentMapper.toResponse(appointment)).thenReturn(response);

        // When
        AppointmentResponse result = appointmentService.getAppointmentById(appointmentId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getAppointmentId()).isEqualTo(appointmentId);

        verify(appointmentRepository).findByAppointmentId(appointmentId);
    }

    @Test
    @DisplayName("Should throw AppointmentNotFoundException when appointment not found")
    void testGetAppointmentById_NotFound() {
        // Given
        String appointmentId = "invalid-uuid";
        when(appointmentRepository.findByAppointmentId(appointmentId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> appointmentService.getAppointmentById(appointmentId))
                .isInstanceOf(AppointmentNotFoundException.class)
                .hasMessageContaining("Appointment not found with id: invalid-uuid");

        verify(appointmentRepository).findByAppointmentId(appointmentId);
    }

    @Test
    @DisplayName("Should get appointments by patient ID")
    void testGetAppointmentsByPatientId_Success() {
        // Given
        List<Appointment> appointments = Arrays.asList(appointment);
        when(patientRepository.existsById(1L)).thenReturn(true);
        when(appointmentRepository.findByPatientId(1L)).thenReturn(appointments);
        when(appointmentMapper.toResponse(appointment)).thenReturn(response);

        // When
        List<AppointmentResponse> results = appointmentService.getAppointmentsByPatientId(1L);

        // Then
        assertThat(results).hasSize(1);
        assertThat(results.get(0).getPatientId()).isEqualTo(1L);

        verify(patientRepository).existsById(1L);
        verify(appointmentRepository).findByPatientId(1L);
    }

    @Test
    @DisplayName("Should throw PatientNotFoundException when getting appointments for non-existent patient")
    void testGetAppointmentsByPatientId_PatientNotFound() {
        // Given
        when(patientRepository.existsById(1L)).thenReturn(false);

        // When & Then
        assertThatThrownBy(() -> appointmentService.getAppointmentsByPatientId(1L))
                .isInstanceOf(PatientNotFoundException.class)
                .hasMessageContaining("Patient not found with id: 1");

        verify(patientRepository).existsById(1L);
    }

    @Test
    @DisplayName("Should get appointments by doctor ID")
    void testGetAppointmentsByDoctorId_Success() {
        // Given
        List<Appointment> appointments = Arrays.asList(appointment);
        when(doctorRepository.existsById(2L)).thenReturn(true);
        when(appointmentRepository.findByDoctorId(2L)).thenReturn(appointments);
        when(appointmentMapper.toResponse(appointment)).thenReturn(response);

        // When
        List<AppointmentResponse> results = appointmentService.getAppointmentsByDoctorId(2L);

        // Then
        assertThat(results).hasSize(1);
        assertThat(results.get(0).getDoctorId()).isEqualTo(2L);

        verify(doctorRepository).existsById(2L);
        verify(appointmentRepository).findByDoctorId(2L);
    }

    @Test
    @DisplayName("Should throw DoctorNotFoundException when getting appointments for non-existent doctor")
    void testGetAppointmentsByDoctorId_DoctorNotFound() {
        // Given
        when(doctorRepository.existsById(2L)).thenReturn(false);

        // When & Then
        assertThatThrownBy(() -> appointmentService.getAppointmentsByDoctorId(2L))
                .isInstanceOf(DoctorNotFoundException.class)
                .hasMessageContaining("Doctor not found with id: 2");

        verify(doctorRepository).existsById(2L);
    }

    @Test
    @DisplayName("Should get all appointments")
    void testGetAllAppointments() {
        // Given
        List<Appointment> appointments = Arrays.asList(appointment);
        when(appointmentRepository.findAll()).thenReturn(appointments);
        when(appointmentMapper.toResponse(appointment)).thenReturn(response);

        // When
        List<AppointmentResponse> results = appointmentService.getAllAppointments();

        // Then
        assertThat(results).hasSize(1);
        verify(appointmentRepository).findAll();
    }
}
