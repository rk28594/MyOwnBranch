package com.sparks.patient.integration;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.matchesPattern;
import static org.hamcrest.Matchers.notNullValue;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import com.sparks.patient.dto.AppointmentRequest;
import com.sparks.patient.entity.Doctor;
import com.sparks.patient.entity.Patient;
import com.sparks.patient.repository.DoctorRepository;
import com.sparks.patient.repository.PatientRepository;

import io.restassured.RestAssured;

/**
 * Integration tests for Appointment - SCRUM-23
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DisplayName("Appointment Integration Tests")
class AppointmentIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private DoctorRepository doctorRepository;

    private Patient patient;
    private Doctor doctor;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
        RestAssured.basePath = "/api/appointments";

        // Clean up and create test data
        patientRepository.deleteAll();
        doctorRepository.deleteAll();

        patient = patientRepository.save(Patient.builder()
                .firstName("John")
                .lastName("Doe")
                .email("john.doe.integration@test.com")
                .phone("+1234567890")
                .dob(LocalDate.of(1990, 1, 1))
                .build());

        doctor = doctorRepository.save(Doctor.builder()
                .fullName("Dr. Smith")
                .licenseNumber("LIC-INTEGRATION-123")
                .specialization("Cardiology")
                .deptId(1L)
                .build());
    }

    @Test
    @DisplayName("Should create appointment and generate UUID")
    void testCreateAppointment_Integration() {
        // Given
        LocalDateTime appointmentTime = LocalDateTime.now().plusDays(1);
        AppointmentRequest request = AppointmentRequest.builder()
                .patientId(patient.getId())
                .doctorId(doctor.getId())
                .appointmentTime(appointmentTime)
                .build();

        // When & Then
        String appointmentId = given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(request)
        .when()
                .post()
        .then()
                .statusCode(HttpStatus.CREATED.value())
                .body("appointmentId", notNullValue())
                .body("appointmentId", matchesPattern("^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$"))
                .body("status", equalTo("SCHEDULED"))
                .body("patientId", equalTo(patient.getId().intValue()))
                .body("patientName", equalTo("John Doe"))
                .body("doctorId", equalTo(doctor.getId().intValue()))
                .body("doctorName", equalTo("Dr. Smith"))
                .extract()
                .path("appointmentId");

        // Verify we can retrieve the appointment
        given()
        .when()
                .get("/" + appointmentId)
        .then()
                .statusCode(HttpStatus.OK.value())
                .body("appointmentId", equalTo(appointmentId));
    }

    @Test
    @DisplayName("Should return 404 when patient not found")
    void testCreateAppointment_PatientNotFound() {
        // Given
        AppointmentRequest request = AppointmentRequest.builder()
                .patientId(999L)
                .doctorId(doctor.getId())
                .appointmentTime(LocalDateTime.now().plusDays(1))
                .build();

        // When & Then
        given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(request)
        .when()
                .post()
        .then()
                .statusCode(HttpStatus.NOT_FOUND.value())
                .body("message", equalTo("Patient not found with id: 999"));
    }

    @Test
    @DisplayName("Should return 404 when doctor not found")
    void testCreateAppointment_DoctorNotFound() {
        // Given
        AppointmentRequest request = AppointmentRequest.builder()
                .patientId(patient.getId())
                .doctorId(999L)
                .appointmentTime(LocalDateTime.now().plusDays(1))
                .build();

        // When & Then
        given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(request)
        .when()
                .post()
        .then()
                .statusCode(HttpStatus.NOT_FOUND.value())
                .body("message", equalTo("Doctor not found with id: 999"));
    }

    @Test
    @DisplayName("Should get appointments by patient ID")
    void testGetAppointmentsByPatientId() {
        // Given - Create an appointment first
        AppointmentRequest request = AppointmentRequest.builder()
                .patientId(patient.getId())
                .doctorId(doctor.getId())
                .appointmentTime(LocalDateTime.now().plusDays(1))
                .build();

        given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(request)
                .post();

        // When & Then
        given()
                .queryParam("patientId", patient.getId())
        .when()
                .get()
        .then()
                .statusCode(HttpStatus.OK.value())
                .body("$", hasSize(1))
                .body("[0].patientId", equalTo(patient.getId().intValue()));
    }

    @Test
    @DisplayName("Should get appointments by doctor ID")
    void testGetAppointmentsByDoctorId() {
        // Given - Create an appointment first
        AppointmentRequest request = AppointmentRequest.builder()
                .patientId(patient.getId())
                .doctorId(doctor.getId())
                .appointmentTime(LocalDateTime.now().plusDays(1))
                .build();

        given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(request)
                .post();

        // When & Then
        given()
                .queryParam("doctorId", doctor.getId())
        .when()
                .get()
        .then()
                .statusCode(HttpStatus.OK.value())
                .body("$", hasSize(1))
                .body("[0].doctorId", equalTo(doctor.getId().intValue()));
    }

    @Test
    @DisplayName("Should return 400 for invalid request")
    void testCreateAppointment_InvalidRequest() {
        // Given
        AppointmentRequest invalidRequest = AppointmentRequest.builder()
                .patientId(null)  // Missing required field
                .doctorId(doctor.getId())
                .appointmentTime(LocalDateTime.now())
                .build();

        // When & Then
        given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(invalidRequest)
        .when()
                .post()
        .then()
                .statusCode(HttpStatus.BAD_REQUEST.value());
    }
}
