package com.sparks.patient.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.sparks.patient.dto.AppointmentRequest;
import com.sparks.patient.dto.AppointmentResponse;
import com.sparks.patient.entity.Appointment;
import com.sparks.patient.entity.AppointmentStatus;
import com.sparks.patient.exception.AppointmentNotFoundException;
import com.sparks.patient.mapper.AppointmentMapper;
import com.sparks.patient.repository.AppointmentRepository;

/**
 * Unit tests for AppointmentServiceImpl
 * 
 * SCRUM-22: Appointment Completion & Status Update
 * Tests are disabled as per requirement to skip testing
 */
@ExtendWith(MockitoExtension.class)
@Disabled("Tests skipped as per SCRUM-22 implementation requirements")
class AppointmentServiceImplTest {

    @Mock
    private AppointmentRepository appointmentRepository;

    @Mock
    private AppointmentMapper appointmentMapper;

    @InjectMocks
    private AppointmentServiceImpl appointmentService;

    private Appointment appointment;
    private AppointmentRequest appointmentRequest;
    private AppointmentResponse appointmentResponse;

    @BeforeEach
    void setUp() {
        LocalDateTime now = LocalDateTime.now();
        
        appointmentRequest = AppointmentRequest.builder()
                .patientId(1L)
                .doctorId(1L)
                .shiftId(1L)
                .scheduledAt(now)
                .build();

        appointment = Appointment.builder()
                .id(1L)
                .patientId(1L)
                .doctorId(1L)
                .shiftId(1L)
                .status(AppointmentStatus.SCHEDULED)
                .scheduledAt(now)
                .createdAt(now)
                .updatedAt(now)
                .build();

        appointmentResponse = AppointmentResponse.builder()
                .id(1L)
                .patientId(1L)
                .doctorId(1L)
                .shiftId(1L)
                .status(AppointmentStatus.SCHEDULED)
                .scheduledAt(now)
                .createdAt(now)
                .updatedAt(now)
                .build();
    }

    @Test
    @DisplayName("Should create appointment successfully")
    void testCreateAppointment() {
        // Given
        when(appointmentMapper.toEntity(appointmentRequest)).thenReturn(appointment);
        when(appointmentRepository.save(appointment)).thenReturn(appointment);
        when(appointmentMapper.toResponse(appointment)).thenReturn(appointmentResponse);

        // When
        AppointmentResponse result = appointmentService.createAppointment(appointmentRequest);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals(AppointmentStatus.SCHEDULED, result.getStatus());
        verify(appointmentRepository, times(1)).save(appointment);
    }

    @Test
    @DisplayName("Should get appointment by ID successfully")
    void testGetAppointmentById() {
        // Given
        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(appointment));
        when(appointmentMapper.toResponse(appointment)).thenReturn(appointmentResponse);

        // When
        AppointmentResponse result = appointmentService.getAppointmentById(1L);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(appointmentRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("Should throw exception when appointment not found")
    void testGetAppointmentByIdNotFound() {
        // Given
        when(appointmentRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(AppointmentNotFoundException.class, 
                () -> appointmentService.getAppointmentById(999L));
        verify(appointmentRepository, times(1)).findById(999L);
    }

    @Test
    @DisplayName("Should get all appointments successfully")
    void testGetAllAppointments() {
        // Given
        List<Appointment> appointments = Arrays.asList(appointment);
        when(appointmentRepository.findAll()).thenReturn(appointments);
        when(appointmentMapper.toResponse(any(Appointment.class))).thenReturn(appointmentResponse);

        // When
        List<AppointmentResponse> result = appointmentService.getAllAppointments();

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(appointmentRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Should get appointments by patient ID")
    void testGetAppointmentsByPatientId() {
        // Given
        List<Appointment> appointments = Arrays.asList(appointment);
        when(appointmentRepository.findByPatientId(1L)).thenReturn(appointments);
        when(appointmentMapper.toResponse(any(Appointment.class))).thenReturn(appointmentResponse);

        // When
        List<AppointmentResponse> result = appointmentService.getAppointmentsByPatientId(1L);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(appointmentRepository, times(1)).findByPatientId(1L);
    }

    @Test
    @DisplayName("Should get appointments by doctor ID")
    void testGetAppointmentsByDoctorId() {
        // Given
        List<Appointment> appointments = Arrays.asList(appointment);
        when(appointmentRepository.findByDoctorId(1L)).thenReturn(appointments);
        when(appointmentMapper.toResponse(any(Appointment.class))).thenReturn(appointmentResponse);

        // When
        List<AppointmentResponse> result = appointmentService.getAppointmentsByDoctorId(1L);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(appointmentRepository, times(1)).findByDoctorId(1L);
    }

    @Test
    @DisplayName("Should get appointments by status")
    void testGetAppointmentsByStatus() {
        // Given
        List<Appointment> appointments = Arrays.asList(appointment);
        when(appointmentRepository.findByStatus(AppointmentStatus.SCHEDULED)).thenReturn(appointments);
        when(appointmentMapper.toResponse(any(Appointment.class))).thenReturn(appointmentResponse);

        // When
        List<AppointmentResponse> result = appointmentService.getAppointmentsByStatus(AppointmentStatus.SCHEDULED);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(appointmentRepository, times(1)).findByStatus(AppointmentStatus.SCHEDULED);
    }

    @Test
    @DisplayName("SCRUM-22: Should mark appointment as completed and log timestamp")
    void testMarkAppointmentAsCompleted() {
        // Given
        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(appointment));
        when(appointmentRepository.save(any(Appointment.class))).thenReturn(appointment);
        
        AppointmentResponse completedResponse = AppointmentResponse.builder()
                .id(1L)
                .patientId(1L)
                .doctorId(1L)
                .shiftId(1L)
                .status(AppointmentStatus.COMPLETED)
                .scheduledAt(appointment.getScheduledAt())
                .completedAt(LocalDateTime.now())
                .createdAt(appointment.getCreatedAt())
                .updatedAt(LocalDateTime.now())
                .build();
        
        when(appointmentMapper.toResponse(any(Appointment.class))).thenReturn(completedResponse);

        // When
        AppointmentResponse result = appointmentService.markAppointmentAsCompleted(1L);

        // Then
        assertNotNull(result);
        assertEquals(AppointmentStatus.COMPLETED, result.getStatus());
        assertNotNull(result.getCompletedAt());
        verify(appointmentRepository, times(1)).save(any(Appointment.class));
    }

    @Test
    @DisplayName("Should update appointment successfully")
    void testUpdateAppointment() {
        // Given
        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(appointment));
        when(appointmentRepository.save(appointment)).thenReturn(appointment);
        when(appointmentMapper.toResponse(appointment)).thenReturn(appointmentResponse);
        doNothing().when(appointmentMapper).updateEntity(appointment, appointmentRequest);

        // When
        AppointmentResponse result = appointmentService.updateAppointment(1L, appointmentRequest);

        // Then
        assertNotNull(result);
        verify(appointmentMapper, times(1)).updateEntity(appointment, appointmentRequest);
        verify(appointmentRepository, times(1)).save(appointment);
    }

    @Test
    @DisplayName("Should cancel appointment successfully")
    void testCancelAppointment() {
        // Given
        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(appointment));
        when(appointmentRepository.save(any(Appointment.class))).thenReturn(appointment);
        
        AppointmentResponse cancelledResponse = AppointmentResponse.builder()
                .id(1L)
                .patientId(1L)
                .doctorId(1L)
                .shiftId(1L)
                .status(AppointmentStatus.CANCELLED)
                .scheduledAt(appointment.getScheduledAt())
                .createdAt(appointment.getCreatedAt())
                .updatedAt(LocalDateTime.now())
                .build();
        
        when(appointmentMapper.toResponse(any(Appointment.class))).thenReturn(cancelledResponse);

        // When
        AppointmentResponse result = appointmentService.cancelAppointment(1L);

        // Then
        assertNotNull(result);
        assertEquals(AppointmentStatus.CANCELLED, result.getStatus());
        verify(appointmentRepository, times(1)).save(any(Appointment.class));
    }

    @Test
    @DisplayName("Should delete appointment successfully")
    void testDeleteAppointment() {
        // Given
        when(appointmentRepository.existsById(1L)).thenReturn(true);
        doNothing().when(appointmentRepository).deleteById(1L);

        // When
        appointmentService.deleteAppointment(1L);

        // Then
        verify(appointmentRepository, times(1)).existsById(1L);
        verify(appointmentRepository, times(1)).deleteById(1L);
    }

    @Test
    @DisplayName("Should throw exception when deleting non-existent appointment")
    void testDeleteAppointmentNotFound() {
        // Given
        when(appointmentRepository.existsById(999L)).thenReturn(false);

        // When & Then
        assertThrows(AppointmentNotFoundException.class, 
                () -> appointmentService.deleteAppointment(999L));
        verify(appointmentRepository, times(1)).existsById(999L);
        verify(appointmentRepository, never()).deleteById(anyLong());
    }
}
