package com.sparks.patient.api;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;

import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.test.annotation.DirtiesContext;

import com.sparks.patient.dto.PatientRequest;
import com.sparks.patient.entity.Patient;
import com.sparks.patient.repository.PatientRepository;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;

/**
 * API Tests using REST Assured
 * End-to-end testing of the Patient Management REST API
 * 
 * SCRUM-14: Patient Onboarding API
 * SCRUM-15: Patient Search & Profile Retrieval
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@DisplayName("Patient Management API Tests (REST Assured)")
class PatientApiTest {

    @LocalServerPort
    private int port;

    @Autowired
    private PatientRepository patientRepository;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
        RestAssured.basePath = "/api/v1/patients";
        patientRepository.deleteAll();
    }

    @Nested
    @DisplayName("SCRUM-14: Patient Onboarding API Tests")
    class PatientOnboardingApiTests {

        @Test
        @DisplayName("POST /api/v1/patients - Should create patient with 201 Created")
        void shouldCreatePatientWithValidData() {
            PatientRequest request = PatientRequest.builder()
                    .firstName("John")
                    .lastName("Doe")
                    .dob(LocalDate.of(1990, 5, 15))
                    .email("john.doe@example.com")
                    .phone("+1234567890")
                    .build();

            given()
                .contentType(ContentType.JSON)
                .body(request)
            .when()
                .post()
            .then()
                .statusCode(HttpStatus.CREATED.value())
                .body("id", greaterThan(0))
                .body("firstName", equalTo("John"))
                .body("lastName", equalTo("Doe"))
                .body("email", equalTo("john.doe@example.com"))
                .body("phone", equalTo("+1234567890"))
                .body("dob", equalTo("1990-05-15"))
                .body("createdAt", notNullValue())
                .body("updatedAt", notNullValue());
        }

        @Test
        @DisplayName("POST /api/v1/patients - Should return 400 for missing required fields")
        void shouldReturn400ForMissingRequiredFields() {
            PatientRequest request = PatientRequest.builder()
                    .firstName("John")
                    // Missing lastName, dob, email, phone
                    .build();

            given()
                .contentType(ContentType.JSON)
                .body(request)
            .when()
                .post()
            .then()
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .body("status", equalTo(400))
                .body("message", equalTo("Validation failed"))
                .body("errors", notNullValue());
        }

        @Test
        @DisplayName("POST /api/v1/patients - Should return 400 for invalid email format")
        void shouldReturn400ForInvalidEmailFormat() {
            PatientRequest request = PatientRequest.builder()
                    .firstName("John")
                    .lastName("Doe")
                    .dob(LocalDate.of(1990, 5, 15))
                    .email("not-a-valid-email")
                    .phone("+1234567890")
                    .build();

            given()
                .contentType(ContentType.JSON)
                .body(request)
            .when()
                .post()
            .then()
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .body("errors.email", notNullValue());
        }

        @Test
        @DisplayName("POST /api/v1/patients - Should return 400 for future date of birth")
        void shouldReturn400ForFutureDateOfBirth() {
            PatientRequest request = PatientRequest.builder()
                    .firstName("John")
                    .lastName("Doe")
                    .dob(LocalDate.now().plusDays(1)) // Future date
                    .email("john.doe@example.com")
                    .phone("+1234567890")
                    .build();

            given()
                .contentType(ContentType.JSON)
                .body(request)
            .when()
                .post()
            .then()
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .body("errors.dob", notNullValue());
        }

        @Test
        @DisplayName("POST /api/v1/patients - Should return 409 for duplicate email")
        void shouldReturn409ForDuplicateEmail() {
            // Create first patient
            Patient existingPatient = Patient.builder()
                    .firstName("Existing")
                    .lastName("Patient")
                    .dob(LocalDate.of(1985, 3, 20))
                    .email("existing@example.com")
                    .phone("+1111111111")
                    .build();
            patientRepository.save(existingPatient);

            // Try to create another with same email
            PatientRequest duplicateRequest = PatientRequest.builder()
                    .firstName("New")
                    .lastName("Patient")
                    .dob(LocalDate.of(1990, 5, 15))
                    .email("existing@example.com") // Duplicate email
                    .phone("+2222222222")
                    .build();

            given()
                .contentType(ContentType.JSON)
                .body(duplicateRequest)
            .when()
                .post()
            .then()
                .statusCode(HttpStatus.CONFLICT.value())
                .body("status", equalTo(409))
                .body("message", equalTo("A patient with email 'existing@example.com' already exists"));
        }
    }

    @Nested
    @DisplayName("SCRUM-15: Patient Search & Profile Retrieval Tests")
    class PatientRetrievalApiTests {

        @Test
        @DisplayName("GET /api/v1/patients/{id} - Should return patient profile")
        void shouldReturnPatientProfile() {
            // Given
            Patient patient = Patient.builder()
                    .firstName("John")
                    .lastName("Doe")
                    .dob(LocalDate.of(1990, 5, 15))
                    .email("john.doe@example.com")
                    .phone("+1234567890")
                    .build();
            Patient savedPatient = patientRepository.save(patient);

            // When/Then
            given()
            .when()
                .get("/{id}", savedPatient.getId())
            .then()
                .statusCode(HttpStatus.OK.value())
                .body("id", equalTo(savedPatient.getId().intValue()))
                .body("firstName", equalTo("John"))
                .body("lastName", equalTo("Doe"))
                .body("email", equalTo("john.doe@example.com"))
                .body("dob", equalTo("1990-05-15"));
        }

        @Test
        @DisplayName("GET /api/v1/patients/{id} - Should return 404 for non-existent ID")
        void shouldReturn404ForNonExistentId() {
            given()
            .when()
                .get("/{id}", 99999)
            .then()
                .statusCode(HttpStatus.NOT_FOUND.value())
                .body("status", equalTo(404))
                .body("message", equalTo("Patient not found with id: 99999"))
                .body("path", equalTo("/api/v1/patients/99999"))
                .body("timestamp", notNullValue());
        }

        @Test
        @DisplayName("GET /api/v1/patients - Should return all patients")
        void shouldReturnAllPatients() {
            // Given
            Patient patient1 = Patient.builder()
                    .firstName("John")
                    .lastName("Doe")
                    .dob(LocalDate.of(1990, 5, 15))
                    .email("john.doe@example.com")
                    .phone("+1234567890")
                    .build();
            
            Patient patient2 = Patient.builder()
                    .firstName("Jane")
                    .lastName("Smith")
                    .dob(LocalDate.of(1985, 3, 20))
                    .email("jane.smith@example.com")
                    .phone("+9876543210")
                    .build();

            patientRepository.save(patient1);
            patientRepository.save(patient2);

            // When/Then
            given()
            .when()
                .get()
            .then()
                .statusCode(HttpStatus.OK.value())
                .body("$", hasSize(2))
                .body("[0].firstName", equalTo("John"))
                .body("[1].firstName", equalTo("Jane"));
        }

        @Test
        @DisplayName("GET /api/v1/patients - Should return empty list when no patients")
        void shouldReturnEmptyListWhenNoPatients() {
            given()
            .when()
                .get()
            .then()
                .statusCode(HttpStatus.OK.value())
                .body("$", hasSize(0));
        }
    }

    @Nested
    @DisplayName("Update Patient API Tests")
    class UpdatePatientApiTests {

        @Test
        @DisplayName("PUT /api/v1/patients/{id} - Should update patient successfully")
        void shouldUpdatePatient() {
            // Given
            Patient patient = Patient.builder()
                    .firstName("John")
                    .lastName("Doe")
                    .dob(LocalDate.of(1990, 5, 15))
                    .email("john.doe@example.com")
                    .phone("+1234567890")
                    .build();
            Patient savedPatient = patientRepository.save(patient);

            PatientRequest updateRequest = PatientRequest.builder()
                    .firstName("John Updated")
                    .lastName("Doe Updated")
                    .dob(LocalDate.of(1990, 5, 15))
                    .email("john.updated@example.com")
                    .phone("+9999999999")
                    .build();

            // When/Then
            given()
                .contentType(ContentType.JSON)
                .body(updateRequest)
            .when()
                .put("/{id}", savedPatient.getId())
            .then()
                .statusCode(HttpStatus.OK.value())
                .body("firstName", equalTo("John Updated"))
                .body("lastName", equalTo("Doe Updated"))
                .body("email", equalTo("john.updated@example.com"))
                .body("phone", equalTo("+9999999999"));
        }

        @Test
        @DisplayName("PUT /api/v1/patients/{id} - Should return 404 for non-existent ID")
        void shouldReturn404WhenUpdatingNonExistentPatient() {
            PatientRequest updateRequest = PatientRequest.builder()
                    .firstName("John")
                    .lastName("Doe")
                    .dob(LocalDate.of(1990, 5, 15))
                    .email("john.doe@example.com")
                    .phone("+1234567890")
                    .build();

            given()
                .contentType(ContentType.JSON)
                .body(updateRequest)
            .when()
                .put("/{id}", 99999)
            .then()
                .statusCode(HttpStatus.NOT_FOUND.value());
        }
    }

    @Nested
    @DisplayName("Delete Patient API Tests")
    class DeletePatientApiTests {

        @Test
        @DisplayName("DELETE /api/v1/patients/{id} - Should delete patient successfully")
        void shouldDeletePatient() {
            // Given
            Patient patient = Patient.builder()
                    .firstName("John")
                    .lastName("Doe")
                    .dob(LocalDate.of(1990, 5, 15))
                    .email("john.doe@example.com")
                    .phone("+1234567890")
                    .build();
            Patient savedPatient = patientRepository.save(patient);

            // When/Then
            given()
            .when()
                .delete("/{id}", savedPatient.getId())
            .then()
                .statusCode(HttpStatus.NO_CONTENT.value());

            // Verify patient is deleted
            given()
            .when()
                .get("/{id}", savedPatient.getId())
            .then()
                .statusCode(HttpStatus.NOT_FOUND.value());
        }

        @Test
        @DisplayName("DELETE /api/v1/patients/{id} - Should return 404 for non-existent ID")
        void shouldReturn404WhenDeletingNonExistentPatient() {
            given()
            .when()
                .delete("/{id}", 99999)
            .then()
                .statusCode(HttpStatus.NOT_FOUND.value());
        }
    }
}
