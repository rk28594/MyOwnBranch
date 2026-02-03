package com.sparks.patient.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.sparks.patient.dto.AppointmentRequest;
import com.sparks.patient.dto.AppointmentResponse;
import com.sparks.patient.entity.Appointment;
import com.sparks.patient.entity.Doctor;
import com.sparks.patient.entity.Patient;

/**
 * Mapper tests for Appointment - SCRUM-23
 */
@DisplayName("Appointment Mapper Tests")
class AppointmentMapperTest {

    private AppointmentMapper appointmentMapper;
    private Patient patient;
    private Doctor doctor;

    @BeforeEach
    void setUp() {
        appointmentMapper = new AppointmentMapper();

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
    }

    @Test
    @DisplayName("Should map request to entity with SCHEDULED status")
    void testToEntity() {
        // Given
        LocalDateTime appointmentTime = LocalDateTime.now().plusDays(1);
        AppointmentRequest request = AppointmentRequest.builder()
                .patientId(patient.getId())
                .doctorId(doctor.getId())
                .appointmentTime(appointmentTime)
                .build();

        // When
        Appointment appointment = appointmentMapper.toEntity(request, patient, doctor);

        // Then
        assertThat(appointment).isNotNull();
        assertThat(appointment.getPatient()).isEqualTo(patient);
        assertThat(appointment.getDoctor()).isEqualTo(doctor);
        assertThat(appointment.getAppointmentTime()).isEqualTo(appointmentTime);
        assertThat(appointment.getStatus()).isEqualTo(Appointment.AppointmentStatus.SCHEDULED);
    }

    @Test
    @DisplayName("Should map entity to response with patient and doctor names")
    void testToResponse() {
        // Given
        LocalDateTime appointmentTime = LocalDateTime.now().plusDays(1);
        LocalDateTime createdAt = LocalDateTime.now();
        
        Appointment appointment = Appointment.builder()
                .id(10L)
                .appointmentId("550e8400-e29b-41d4-a716-446655440000")
                .patient(patient)
                .doctor(doctor)
                .appointmentTime(appointmentTime)
                .status(Appointment.AppointmentStatus.SCHEDULED)
                .createdAt(createdAt)
                .build();

        // When
        AppointmentResponse response = appointmentMapper.toResponse(appointment);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(10L);
        assertThat(response.getAppointmentId()).isEqualTo("550e8400-e29b-41d4-a716-446655440000");
        assertThat(response.getPatientId()).isEqualTo(patient.getId());
        assertThat(response.getPatientName()).isEqualTo("John Doe");
        assertThat(response.getDoctorId()).isEqualTo(doctor.getId());
        assertThat(response.getDoctorName()).isEqualTo("Dr. Smith");
        assertThat(response.getAppointmentTime()).isEqualTo(appointmentTime);
        assertThat(response.getStatus()).isEqualTo("SCHEDULED");
        assertThat(response.getCreatedAt()).isEqualTo(createdAt);
    }
}
