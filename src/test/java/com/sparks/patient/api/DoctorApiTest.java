package com.sparks.patient.api;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.test.annotation.DirtiesContext;

import com.sparks.patient.dto.DoctorRequest;
import com.sparks.patient.entity.Doctor;
import com.sparks.patient.repository.DoctorRepository;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;

/**
 * API Tests for Doctor Management using REST Assured
 * SCRUM-20: Doctor Profile Management
 * End-to-end testing of the Doctor Management REST API
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@DisplayName("Doctor Management API Tests (REST Assured)")
class DoctorApiTest {

    @LocalServerPort
    private int port;

    @Autowired
    private DoctorRepository doctorRepository;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
        RestAssured.basePath = "/api/v1/doctors";
        doctorRepository.deleteAll();
    }

    @Nested
    @DisplayName("SCRUM-20: Doctor Profile Management API Tests")
    class DoctorProfileManagementTests {

        @Test
        @DisplayName("POST /api/v1/doctors - Should create doctor with 201 Created")
        void shouldCreateDoctorWithValidData() {
            DoctorRequest request = DoctorRequest.builder()
                    .fullName("Dr. John Smith")
                    .licenseNumber("MED-123456")
                    .specialization("Cardiology")
                    .deptId(1L)
                    .build();

            given()
                    .contentType(ContentType.JSON)
                    .body(request)
                    .when()
                    .post()
                    .then()
                    .statusCode(HttpStatus.CREATED.value())
                    .body("id", notNullValue())
                    .body("fullName", equalTo("Dr. John Smith"))
                    .body("licenseNumber", equalTo("MED-123456"))
                    .body("specialization", equalTo("Cardiology"))
                    .body("deptId", equalTo(1));
        }

        @Test
        @DisplayName("POST /api/v1/doctors - Should return 409 Conflict for duplicate license number")
        void shouldReturnConflictForDuplicateLicenseNumber() {
            // Create first doctor
            DoctorRequest request = DoctorRequest.builder()
                    .fullName("Dr. John Smith")
                    .licenseNumber("MED-123456")
                    .specialization("Cardiology")
                    .deptId(1L)
                    .build();

            given()
                    .contentType(ContentType.JSON)
                    .body(request)
                    .when()
                    .post()
                    .then()
                    .statusCode(HttpStatus.CREATED.value());

            // Try to create second doctor with same license number
            DoctorRequest duplicateRequest = DoctorRequest.builder()
                    .fullName("Dr. Jane Doe")
                    .licenseNumber("MED-123456")
                    .specialization("Neurology")
                    .deptId(2L)
                    .build();

            given()
                    .contentType(ContentType.JSON)
                    .body(duplicateRequest)
                    .when()
                    .post()
                    .then()
                    .statusCode(HttpStatus.CONFLICT.value());
        }

        @Test
        @DisplayName("POST /api/v1/doctors - Should return 400 Bad Request for invalid data")
        void shouldReturnBadRequestForInvalidData() {
            DoctorRequest invalidRequest = DoctorRequest.builder()
                    .fullName("")  // Empty name
                    .licenseNumber("MED")  // Too short
                    .specialization("")  // Empty specialization
                    .deptId(null)  // Null department
                    .build();

            given()
                    .contentType(ContentType.JSON)
                    .body(invalidRequest)
                    .when()
                    .post()
                    .then()
                    .statusCode(HttpStatus.BAD_REQUEST.value());
        }

        @Test
        @DisplayName("GET /api/v1/doctors/{id} - Should retrieve doctor by ID")
        void shouldRetrieveDoctorById() {
            // Create a doctor
            Doctor doctor = Doctor.builder()
                    .fullName("Dr. John Smith")
                    .licenseNumber("MED-123456")
                    .specialization("Cardiology")
                    .deptId(1L)
                    .build();
            Doctor savedDoctor = doctorRepository.save(doctor);

            given()
                    .when()
                    .get("/{id}", savedDoctor.getId())
                    .then()
                    .statusCode(HttpStatus.OK.value())
                    .body("id", equalTo(savedDoctor.getId().intValue()))
                    .body("fullName", equalTo("Dr. John Smith"))
                    .body("licenseNumber", equalTo("MED-123456"));
        }

        @Test
        @DisplayName("GET /api/v1/doctors/{id} - Should return 404 Not Found for non-existent doctor")
        void shouldReturnNotFoundForNonExistentDoctor() {
            given()
                    .when()
                    .get("/{id}", 999L)
                    .then()
                    .statusCode(HttpStatus.NOT_FOUND.value());
        }

        @Test
        @DisplayName("GET /api/v1/doctors/license/{licenseNumber} - Should retrieve doctor by license number")
        void shouldRetrieveDoctorByLicenseNumber() {
            // Create a doctor
            Doctor doctor = Doctor.builder()
                    .fullName("Dr. John Smith")
                    .licenseNumber("MED-123456")
                    .specialization("Cardiology")
                    .deptId(1L)
                    .build();
            doctorRepository.save(doctor);

            given()
                    .when()
                    .get("/license/{licenseNumber}", "MED-123456")
                    .then()
                    .statusCode(HttpStatus.OK.value())
                    .body("fullName", equalTo("Dr. John Smith"))
                    .body("licenseNumber", equalTo("MED-123456"));
        }

        @Test
        @DisplayName("GET /api/v1/doctors - Should retrieve all doctors")
        void shouldRetrieveAllDoctors() {
            // Create multiple doctors
            Doctor doctor1 = Doctor.builder()
                    .fullName("Dr. John Smith")
                    .licenseNumber("MED-123456")
                    .specialization("Cardiology")
                    .deptId(1L)
                    .build();

            Doctor doctor2 = Doctor.builder()
                    .fullName("Dr. Jane Doe")
                    .licenseNumber("MED-654321")
                    .specialization("Neurology")
                    .deptId(2L)
                    .build();

            doctorRepository.save(doctor1);
            doctorRepository.save(doctor2);

            given()
                    .when()
                    .get()
                    .then()
                    .statusCode(HttpStatus.OK.value())
                    .body(".", org.hamcrest.Matchers.hasSize(2));
        }

        @Test
        @DisplayName("PUT /api/v1/doctors/{id} - Should update doctor successfully")
        void shouldUpdateDoctorSuccessfully() {
            // Create a doctor
            Doctor doctor = Doctor.builder()
                    .fullName("Dr. John Smith")
                    .licenseNumber("MED-123456")
                    .specialization("Cardiology")
                    .deptId(1L)
                    .build();
            Doctor savedDoctor = doctorRepository.save(doctor);

            // Update doctor
            DoctorRequest updateRequest = DoctorRequest.builder()
                    .fullName("Dr. John Smith Updated")
                    .licenseNumber("MED-123456")
                    .specialization("Cardiology")
                    .deptId(1L)
                    .build();

            given()
                    .contentType(ContentType.JSON)
                    .body(updateRequest)
                    .when()
                    .put("/{id}", savedDoctor.getId())
                    .then()
                    .statusCode(HttpStatus.OK.value())
                    .body("fullName", equalTo("Dr. John Smith Updated"));
        }

        @Test
        @DisplayName("PUT /api/v1/doctors/{id} - Should return 409 Conflict when updating with existing license number")
        void shouldReturnConflictWhenUpdatingWithExistingLicenseNumber() {
            // Create two doctors
            Doctor doctor1 = Doctor.builder()
                    .fullName("Dr. John Smith")
                    .licenseNumber("MED-123456")
                    .specialization("Cardiology")
                    .deptId(1L)
                    .build();

            Doctor doctor2 = Doctor.builder()
                    .fullName("Dr. Jane Doe")
                    .licenseNumber("MED-654321")
                    .specialization("Neurology")
                    .deptId(2L)
                    .build();

            Doctor savedDoctor1 = doctorRepository.save(doctor1);
            doctorRepository.save(doctor2);

            // Try to update doctor1 with doctor2's license number
            DoctorRequest updateRequest = DoctorRequest.builder()
                    .fullName("Dr. John Smith Updated")
                    .licenseNumber("MED-654321")  // Existing license number
                    .specialization("Cardiology")
                    .deptId(1L)
                    .build();

            given()
                    .contentType(ContentType.JSON)
                    .body(updateRequest)
                    .when()
                    .put("/{id}", savedDoctor1.getId())
                    .then()
                    .statusCode(HttpStatus.CONFLICT.value());
        }

        @Test
        @DisplayName("DELETE /api/v1/doctors/{id} - Should delete doctor successfully")
        void shouldDeleteDoctorSuccessfully() {
            // Create a doctor
            Doctor doctor = Doctor.builder()
                    .fullName("Dr. John Smith")
                    .licenseNumber("MED-123456")
                    .specialization("Cardiology")
                    .deptId(1L)
                    .build();
            Doctor savedDoctor = doctorRepository.save(doctor);

            given()
                    .when()
                    .delete("/{id}", savedDoctor.getId())
                    .then()
                    .statusCode(HttpStatus.NO_CONTENT.value());

            // Verify doctor is deleted
            given()
                    .when()
                    .get("/{id}", savedDoctor.getId())
                    .then()
                    .statusCode(HttpStatus.NOT_FOUND.value());
        }
    }

    @Nested
    @DisplayName("Doctor Validation Tests")
    class DoctorValidationTests {

        @Test
        @DisplayName("Should validate full name length constraints")
        void shouldValidateFullNameLengthConstraints() {
            DoctorRequest invalidRequest = DoctorRequest.builder()
                    .fullName("A")  // Too short
                    .licenseNumber("MED-123456")
                    .specialization("Cardiology")
                    .deptId(1L)
                    .build();

            given()
                    .contentType(ContentType.JSON)
                    .body(invalidRequest)
                    .when()
                    .post()
                    .then()
                    .statusCode(HttpStatus.BAD_REQUEST.value());
        }

        @Test
        @DisplayName("Should validate license number length constraints")
        void shouldValidateLicenseNumberLengthConstraints() {
            DoctorRequest invalidRequest = DoctorRequest.builder()
                    .fullName("Dr. John Smith")
                    .licenseNumber("MED")  // Too short
                    .specialization("Cardiology")
                    .deptId(1L)
                    .build();

            given()
                    .contentType(ContentType.JSON)
                    .body(invalidRequest)
                    .when()
                    .post()
                    .then()
                    .statusCode(HttpStatus.BAD_REQUEST.value());
        }
    }
}
