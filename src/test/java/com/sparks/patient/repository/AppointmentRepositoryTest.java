package com.sparks.patient.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import com.sparks.patient.entity.Appointment;
import com.sparks.patient.entity.Doctor;
import com.sparks.patient.entity.Patient;

import java.time.LocalDate;

/**
 * Repository tests for Appointment - SCRUM-23
 */
@DataJpaTest
@DisplayName("Appointment Repository Tests")
class AppointmentRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private AppointmentRepository appointmentRepository;

    private Patient patient;
    private Doctor doctor;

    @BeforeEach
    void setUp() {
        // Create and persist patient
        patient = Patient.builder()
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@test.com")
                .phone("+1234567890")
                .dob(LocalDate.of(1990, 1, 1))
                .build();
        entityManager.persist(patient);

        // Create and persist doctor
        doctor = Doctor.builder()
                .fullName("Dr. Smith")
                .licenseNumber("LIC123456")
                .specialization("Cardiology")
                .deptId(1L)
                .build();
        entityManager.persist(doctor);

        entityManager.flush();
    }

    @Test
    @DisplayName("Should save appointment with generated UUID")
    void testSaveAppointment() {
        // Given
        LocalDateTime appointmentTime = LocalDateTime.now().plusDays(1);
        Appointment appointment = Appointment.builder()
                .patient(patient)
                .doctor(doctor)
                .appointmentTime(appointmentTime)
                .status(Appointment.AppointmentStatus.SCHEDULED)
                .build();

        // When
        Appointment saved = appointmentRepository.save(appointment);

        // Then
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getAppointmentId()).isNotNull();
        assertThat(saved.getAppointmentId()).matches("^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$");
        assertThat(saved.getStatus()).isEqualTo(Appointment.AppointmentStatus.SCHEDULED);
        assertThat(saved.getCreatedAt()).isNotNull();
    }

    @Test
    @DisplayName("Should find appointment by UUID")
    void testFindByAppointmentId() {
        // Given
        LocalDateTime appointmentTime = LocalDateTime.now().plusDays(1);
        Appointment appointment = Appointment.builder()
                .patient(patient)
                .doctor(doctor)
                .appointmentTime(appointmentTime)
                .build();
        Appointment saved = entityManager.persist(appointment);
        entityManager.flush();

        // When
        Optional<Appointment> found = appointmentRepository.findByAppointmentId(saved.getAppointmentId());

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getAppointmentId()).isEqualTo(saved.getAppointmentId());
    }

    @Test
    @DisplayName("Should find appointments by patient ID")
    void testFindByPatientId() {
        // Given
        Appointment appointment1 = Appointment.builder()
                .patient(patient)
                .doctor(doctor)
                .appointmentTime(LocalDateTime.now().plusDays(1))
                .build();
        
        Appointment appointment2 = Appointment.builder()
                .patient(patient)
                .doctor(doctor)
                .appointmentTime(LocalDateTime.now().plusDays(2))
                .build();

        entityManager.persist(appointment1);
        entityManager.persist(appointment2);
        entityManager.flush();

        // When
        List<Appointment> appointments = appointmentRepository.findByPatientId(patient.getId());

        // Then
        assertThat(appointments).hasSize(2);
        assertThat(appointments).allMatch(a -> a.getPatient().getId().equals(patient.getId()));
    }

    @Test
    @DisplayName("Should find appointments by doctor ID")
    void testFindByDoctorId() {
        // Given
        Appointment appointment = Appointment.builder()
                .patient(patient)
                .doctor(doctor)
                .appointmentTime(LocalDateTime.now().plusDays(1))
                .build();

        entityManager.persist(appointment);
        entityManager.flush();

        // When
        List<Appointment> appointments = appointmentRepository.findByDoctorId(doctor.getId());

        // Then
        assertThat(appointments).hasSize(1);
        assertThat(appointments.get(0).getDoctor().getId()).isEqualTo(doctor.getId());
    }

    @Test
    @DisplayName("Should find appointments by patient ID and status")
    void testFindByPatientIdAndStatus() {
        // Given
        Appointment scheduled = Appointment.builder()
                .patient(patient)
                .doctor(doctor)
                .appointmentTime(LocalDateTime.now().plusDays(1))
                .status(Appointment.AppointmentStatus.SCHEDULED)
                .build();

        Appointment confirmed = Appointment.builder()
                .patient(patient)
                .doctor(doctor)
                .appointmentTime(LocalDateTime.now().plusDays(2))
                .status(Appointment.AppointmentStatus.CONFIRMED)
                .build();

        entityManager.persist(scheduled);
        entityManager.persist(confirmed);
        entityManager.flush();

        // When
        List<Appointment> scheduledAppointments = appointmentRepository
                .findByPatientIdAndStatus(patient.getId(), Appointment.AppointmentStatus.SCHEDULED);

        // Then
        assertThat(scheduledAppointments).hasSize(1);
        assertThat(scheduledAppointments.get(0).getStatus()).isEqualTo(Appointment.AppointmentStatus.SCHEDULED);
    }

    @Test
    @DisplayName("Should find appointments by doctor and time range")
    void testFindByDoctorIdAndAppointmentTimeBetween() {
        // Given
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime tomorrow = now.plusDays(1);
        LocalDateTime dayAfter = now.plusDays(2);
        LocalDateTime nextWeek = now.plusDays(7);

        Appointment appointment1 = Appointment.builder()
                .patient(patient)
                .doctor(doctor)
                .appointmentTime(tomorrow)
                .build();

        Appointment appointment2 = Appointment.builder()
                .patient(patient)
                .doctor(doctor)
                .appointmentTime(nextWeek)
                .build();

        entityManager.persist(appointment1);
        entityManager.persist(appointment2);
        entityManager.flush();

        // When
        List<Appointment> appointments = appointmentRepository
                .findByDoctorIdAndAppointmentTimeBetween(doctor.getId(), now, dayAfter);

        // Then
        assertThat(appointments).hasSize(1);
        assertThat(appointments.get(0).getAppointmentTime()).isEqualTo(tomorrow);
    }
}
