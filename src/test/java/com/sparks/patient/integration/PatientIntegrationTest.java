package com.sparks.patient.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sparks.patient.dto.PatientRequest;
import com.sparks.patient.entity.Patient;
import com.sparks.patient.repository.PatientRepository;

/**
 * Integration Tests for Patient Management API
 * Tests the full application context with real database
 * 
 * SCRUM-14: Patient Onboarding API - POST /api/v1/patients returns 201 Created
 * SCRUM-15: Patient Search & Profile Retrieval - GET /api/v1/patients/{id}
 * SCRUM-16: Patient Schema & Entity Mapping - Table created in H2
 */
@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@DisplayName("Patient Management Integration Tests")
class PatientIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PatientRepository patientRepository;

    private PatientRequest validPatientRequest;

    @BeforeEach
    void setUp() {
        patientRepository.deleteAll();
        
        validPatientRequest = PatientRequest.builder()
                .firstName("John")
                .lastName("Doe")
                .dob(LocalDate.of(1990, 5, 15))
                .email("john.doe@example.com")
                .phone("+1234567890")
                .build();
    }

    @Nested
    @DisplayName("SCRUM-14: Patient Onboarding API Tests")
    class PatientOnboardingTests {

        @Test
        @DisplayName("POST /api/v1/patients returns 201 Created - Persists data")
        void shouldCreatePatientAndReturn201() throws Exception {
            // When/Then
            MvcResult result = mockMvc.perform(post("/api/v1/patients")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(validPatientRequest)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").exists())
                    .andExpect(jsonPath("$.firstName").value("John"))
                    .andExpect(jsonPath("$.lastName").value("Doe"))
                    .andExpect(jsonPath("$.email").value("john.doe@example.com"))
                    .andExpect(jsonPath("$.phone").value("+1234567890"))
                    .andExpect(jsonPath("$.createdAt").exists())
                    .andReturn();

            // Verify data is persisted (Test Scenario: record is searchable in database)
            assertThat(patientRepository.count()).isEqualTo(1);
            assertThat(patientRepository.findByEmail("john.doe@example.com")).isPresent();
        }

        @Test
        @DisplayName("Should return 400 Bad Request for invalid JSON")
        void shouldReturn400ForInvalidRequest() throws Exception {
            // Given
            PatientRequest invalidRequest = PatientRequest.builder()
                    .firstName("") // Invalid: blank
                    .lastName("Doe")
                    .dob(LocalDate.of(1990, 5, 15))
                    .email("invalid-email") // Invalid email format
                    .phone("+1234567890")
                    .build();

            // When/Then
            mockMvc.perform(post("/api/v1/patients")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(invalidRequest)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(400))
                    .andExpect(jsonPath("$.errors").exists());
        }

        @Test
        @DisplayName("Should return 409 Conflict for duplicate email")
        void shouldReturn409ForDuplicateEmail() throws Exception {
            // Given - Create first patient
            mockMvc.perform(post("/api/v1/patients")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(validPatientRequest)))
                    .andExpect(status().isCreated());

            // When/Then - Try to create another with same email
            mockMvc.perform(post("/api/v1/patients")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(validPatientRequest)))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.status").value(409))
                    .andExpect(jsonPath("$.message").value("A patient with email 'john.doe@example.com' already exists"));
        }
    }

    @Nested
    @DisplayName("SCRUM-15: Patient Search & Profile Retrieval Tests")
    class PatientRetrievalTests {

        @Test
        @DisplayName("GET /api/v1/patients/{id} returns the profile")
        void shouldReturnPatientProfile() throws Exception {
            // Given - Create a patient first
            Patient patient = Patient.builder()
                    .firstName("John")
                    .lastName("Doe")
                    .dob(LocalDate.of(1990, 5, 15))
                    .email("john.doe@example.com")
                    .phone("+1234567890")
                    .build();
            Patient savedPatient = patientRepository.save(patient);

            // When/Then
            mockMvc.perform(get("/api/v1/patients/{id}", savedPatient.getId()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(savedPatient.getId()))
                    .andExpect(jsonPath("$.firstName").value("John"))
                    .andExpect(jsonPath("$.lastName").value("Doe"))
                    .andExpect(jsonPath("$.email").value("john.doe@example.com"))
                    .andExpect(jsonPath("$.dob").value("1990-05-15"));
        }

        @Test
        @DisplayName("Returns 404 for invalid IDs - Test Scenario: JSON error body returned")
        void shouldReturn404ForNonExistentId() throws Exception {
            // When/Then - Searching for a non-existent ID returns JSON error body
            mockMvc.perform(get("/api/v1/patients/{id}", 999L))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.status").value(404))
                    .andExpect(jsonPath("$.message").value("Patient not found with id: 999"))
                    .andExpect(jsonPath("$.path").value("/api/v1/patients/999"))
                    .andExpect(jsonPath("$.timestamp").exists());
        }

        @Test
        @DisplayName("GET /api/v1/patients returns all patients")
        void shouldReturnAllPatients() throws Exception {
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
            mockMvc.perform(get("/api/v1/patients"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(2))
                    .andExpect(jsonPath("$[0].firstName").value("John"))
                    .andExpect(jsonPath("$[1].firstName").value("Jane"));
        }
    }

    @Nested
    @DisplayName("Update Patient Tests")
    class UpdatePatientTests {

        @Test
        @DisplayName("PUT /api/v1/patients/{id} updates patient successfully")
        void shouldUpdatePatient() throws Exception {
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
            mockMvc.perform(put("/api/v1/patients/{id}", savedPatient.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(updateRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.firstName").value("John Updated"))
                    .andExpect(jsonPath("$.lastName").value("Doe Updated"))
                    .andExpect(jsonPath("$.email").value("john.updated@example.com"))
                    .andExpect(jsonPath("$.phone").value("+9999999999"));
        }
    }

    @Nested
    @DisplayName("Delete Patient Tests")
    class DeletePatientTests {

        @Test
        @DisplayName("DELETE /api/v1/patients/{id} deletes patient successfully")
        void shouldDeletePatient() throws Exception {
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
            mockMvc.perform(delete("/api/v1/patients/{id}", savedPatient.getId()))
                    .andExpect(status().isNoContent());

            // Verify deletion
            assertThat(patientRepository.findById(savedPatient.getId())).isEmpty();
        }

        @Test
        @DisplayName("DELETE returns 404 for non-existent patient")
        void shouldReturn404WhenDeletingNonExistentPatient() throws Exception {
            mockMvc.perform(delete("/api/v1/patients/{id}", 999L))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("SCRUM-16: H2 Database Integration Tests")
    class DatabaseIntegrationTests {

        @Test
        @DisplayName("Table is created in H2 with correct constraints - email unique")
        void shouldEnforceEmailUniquenessAtDatabaseLevel() throws Exception {
            // Given - Create first patient
            Patient patient1 = Patient.builder()
                    .firstName("John")
                    .lastName("Doe")
                    .dob(LocalDate.of(1990, 5, 15))
                    .email("unique@example.com")
                    .phone("+1234567890")
                    .build();
            patientRepository.save(patient1);

            // Verify email uniqueness is checked via service
            PatientRequest duplicateRequest = PatientRequest.builder()
                    .firstName("Jane")
                    .lastName("Smith")
                    .dob(LocalDate.of(1985, 3, 20))
                    .email("unique@example.com") // Same email
                    .phone("+9876543210")
                    .build();

            // When/Then
            mockMvc.perform(post("/api/v1/patients")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(duplicateRequest)))
                    .andExpect(status().isConflict());
        }
    }
}
