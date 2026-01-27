package com.sparks.patient.repository;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import com.sparks.patient.entity.Appointment;
import com.sparks.patient.entity.AppointmentStatus;

/**
 * Integration tests for AppointmentRepository
 * 
 * SCRUM-22: Appointment Completion & Status Update
 * Tests are disabled as per requirement to skip testing
 */
@DataJpaTest
@Disabled("Tests skipped as per SCRUM-22 implementation requirements")
class AppointmentRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private AppointmentRepository appointmentRepository;

    private Appointment appointment1;
    private Appointment appointment2;

    @BeforeEach
    void setUp() {
        LocalDateTime now = LocalDateTime.now();

        appointment1 = Appointment.builder()
                .patientId(1L)
                .doctorId(1L)
                .shiftId(1L)
                .status(AppointmentStatus.SCHEDULED)
                .scheduledAt(now)
                .build();

        appointment2 = Appointment.builder()
                .patientId(2L)
                .doctorId(1L)
                .shiftId(2L)
                .status(AppointmentStatus.COMPLETED)
                .scheduledAt(now)
                .completedAt(now.plusHours(1))
                .build();

        entityManager.persist(appointment1);
        entityManager.persist(appointment2);
        entityManager.flush();
    }

    @Test
    @DisplayName("Should save and retrieve appointment")
    void testSaveAndRetrieveAppointment() {
        // Given
        Appointment newAppointment = Appointment.builder()
                .patientId(3L)
                .doctorId(2L)
                .shiftId(3L)
                .status(AppointmentStatus.SCHEDULED)
                .scheduledAt(LocalDateTime.now())
                .build();

        // When
        Appointment saved = appointmentRepository.save(newAppointment);
        Optional<Appointment> found = appointmentRepository.findById(saved.getId());

        // Then
        assertTrue(found.isPresent());
        assertEquals(3L, found.get().getPatientId());
        assertEquals(2L, found.get().getDoctorId());
        assertEquals(AppointmentStatus.SCHEDULED, found.get().getStatus());
    }

    @Test
    @DisplayName("Should find appointments by patient ID")
    void testFindByPatientId() {
        // When
        List<Appointment> appointments = appointmentRepository.findByPatientId(1L);

        // Then
        assertNotNull(appointments);
        assertEquals(1, appointments.size());
        assertEquals(1L, appointments.get(0).getPatientId());
    }

    @Test
    @DisplayName("Should find appointments by doctor ID")
    void testFindByDoctorId() {
        // When
        List<Appointment> appointments = appointmentRepository.findByDoctorId(1L);

        // Then
        assertNotNull(appointments);
        assertEquals(2, appointments.size());
    }

    @Test
    @DisplayName("Should find appointments by shift ID")
    void testFindByShiftId() {
        // When
        List<Appointment> appointments = appointmentRepository.findByShiftId(1L);

        // Then
        assertNotNull(appointments);
        assertEquals(1, appointments.size());
        assertEquals(1L, appointments.get(0).getShiftId());
    }

    @Test
    @DisplayName("Should find appointments by status")
    void testFindByStatus() {
        // When
        List<Appointment> scheduledAppointments = appointmentRepository.findByStatus(AppointmentStatus.SCHEDULED);
        List<Appointment> completedAppointments = appointmentRepository.findByStatus(AppointmentStatus.COMPLETED);

        // Then
        assertEquals(1, scheduledAppointments.size());
        assertEquals(1, completedAppointments.size());
        assertEquals(AppointmentStatus.SCHEDULED, scheduledAppointments.get(0).getStatus());
        assertEquals(AppointmentStatus.COMPLETED, completedAppointments.get(0).getStatus());
    }

    @Test
    @DisplayName("Should find appointments by patient ID and status")
    void testFindByPatientIdAndStatus() {
        // When
        List<Appointment> appointments = appointmentRepository.findByPatientIdAndStatus(1L, AppointmentStatus.SCHEDULED);

        // Then
        assertNotNull(appointments);
        assertEquals(1, appointments.size());
        assertEquals(1L, appointments.get(0).getPatientId());
        assertEquals(AppointmentStatus.SCHEDULED, appointments.get(0).getStatus());
    }

    @Test
    @DisplayName("SCRUM-22: Should log timestamp when marking as completed")
    void testMarkAsCompleted() {
        // Given
        Appointment appointment = appointmentRepository.findById(appointment1.getId()).orElseThrow();
        assertNull(appointment.getCompletedAt());

        // When
        appointment.markAsCompleted();
        Appointment updated = appointmentRepository.save(appointment);

        // Then
        assertEquals(AppointmentStatus.COMPLETED, updated.getStatus());
        assertNotNull(updated.getCompletedAt());
    }

    @Test
    @DisplayName("Should delete appointment")
    void testDeleteAppointment() {
        // Given
        Long id = appointment1.getId();
        assertTrue(appointmentRepository.existsById(id));

        // When
        appointmentRepository.deleteById(id);

        // Then
        assertFalse(appointmentRepository.existsById(id));
    }
}
