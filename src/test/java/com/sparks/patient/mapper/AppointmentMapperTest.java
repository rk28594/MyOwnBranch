package com.sparks.patient.mapper;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.sparks.patient.dto.AppointmentRequest;
import com.sparks.patient.dto.AppointmentResponse;
import com.sparks.patient.entity.Appointment;
import com.sparks.patient.entity.AppointmentStatus;

/**
 * Unit tests for AppointmentMapper
 * 
 * SCRUM-22: Appointment Completion & Status Update
 * Tests are disabled as per requirement to skip testing
 */
@Disabled("Tests skipped as per SCRUM-22 implementation requirements")
class AppointmentMapperTest {

    private AppointmentMapper appointmentMapper;

    @BeforeEach
    void setUp() {
        appointmentMapper = new AppointmentMapper();
    }

    @Test
    @DisplayName("Should convert AppointmentRequest to Appointment entity")
    void testToEntity() {
        // Given
        LocalDateTime scheduledAt = LocalDateTime.now();
        AppointmentRequest request = AppointmentRequest.builder()
                .patientId(1L)
                .doctorId(2L)
                .shiftId(3L)
                .scheduledAt(scheduledAt)
                .build();

        // When
        Appointment result = appointmentMapper.toEntity(request);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.getPatientId());
        assertEquals(2L, result.getDoctorId());
        assertEquals(3L, result.getShiftId());
        assertEquals(scheduledAt, result.getScheduledAt());
        assertEquals(AppointmentStatus.SCHEDULED, result.getStatus());
    }

    @Test
    @DisplayName("Should return null when AppointmentRequest is null")
    void testToEntityWithNull() {
        // When
        Appointment result = appointmentMapper.toEntity(null);

        // Then
        assertNull(result);
    }

    @Test
    @DisplayName("Should convert Appointment entity to AppointmentResponse")
    void testToResponse() {
        // Given
        LocalDateTime now = LocalDateTime.now();
        Appointment appointment = Appointment.builder()
                .id(1L)
                .patientId(1L)
                .doctorId(2L)
                .shiftId(3L)
                .status(AppointmentStatus.COMPLETED)
                .scheduledAt(now)
                .completedAt(now.plusHours(1))
                .createdAt(now)
                .updatedAt(now)
                .build();

        // When
        AppointmentResponse result = appointmentMapper.toResponse(appointment);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals(1L, result.getPatientId());
        assertEquals(2L, result.getDoctorId());
        assertEquals(3L, result.getShiftId());
        assertEquals(AppointmentStatus.COMPLETED, result.getStatus());
        assertEquals(now, result.getScheduledAt());
        assertNotNull(result.getCompletedAt());
        assertEquals(now, result.getCreatedAt());
        assertEquals(now, result.getUpdatedAt());
    }

    @Test
    @DisplayName("Should return null when Appointment is null")
    void testToResponseWithNull() {
        // When
        AppointmentResponse result = appointmentMapper.toResponse(null);

        // Then
        assertNull(result);
    }

    @Test
    @DisplayName("Should update Appointment entity from AppointmentRequest")
    void testUpdateEntity() {
        // Given
        LocalDateTime originalTime = LocalDateTime.now();
        Appointment appointment = Appointment.builder()
                .id(1L)
                .patientId(1L)
                .doctorId(2L)
                .shiftId(3L)
                .status(AppointmentStatus.SCHEDULED)
                .scheduledAt(originalTime)
                .build();

        LocalDateTime newTime = originalTime.plusDays(1);
        AppointmentRequest request = AppointmentRequest.builder()
                .patientId(10L)
                .doctorId(20L)
                .shiftId(30L)
                .scheduledAt(newTime)
                .build();

        // When
        appointmentMapper.updateEntity(appointment, request);

        // Then
        assertEquals(10L, appointment.getPatientId());
        assertEquals(20L, appointment.getDoctorId());
        assertEquals(30L, appointment.getShiftId());
        assertEquals(newTime, appointment.getScheduledAt());
        assertEquals(1L, appointment.getId()); // ID should not change
    }

    @Test
    @DisplayName("Should not update when Appointment is null")
    void testUpdateEntityWithNullAppointment() {
        // Given
        AppointmentRequest request = AppointmentRequest.builder()
                .patientId(1L)
                .doctorId(2L)
                .shiftId(3L)
                .build();

        // When & Then - should not throw exception
        assertDoesNotThrow(() -> appointmentMapper.updateEntity(null, request));
    }

    @Test
    @DisplayName("Should not update when AppointmentRequest is null")
    void testUpdateEntityWithNullRequest() {
        // Given
        Appointment appointment = Appointment.builder()
                .id(1L)
                .patientId(1L)
                .doctorId(2L)
                .shiftId(3L)
                .build();

        // When & Then - should not throw exception
        assertDoesNotThrow(() -> appointmentMapper.updateEntity(appointment, null));
    }
}
