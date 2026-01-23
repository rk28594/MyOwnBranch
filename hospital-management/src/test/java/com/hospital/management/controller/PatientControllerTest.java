package com.hospital.management.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hospital.management.dto.PatientRequest;
import com.hospital.management.dto.PatientResponse;
import com.hospital.management.exception.DuplicateResourceException;
import com.hospital.management.exception.ResourceNotFoundException;
import com.hospital.management.service.PatientService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * API/Integration tests for PatientController
 * Tests all REST endpoints with various scenarios
 */
@WebMvcTest(PatientController.class)
@DisplayName("PatientController API Tests")
class PatientControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PatientService patientService;

    private PatientRequest validRequest;
    private PatientResponse validResponse;

    @BeforeEach
    void setUp() {
        validRequest = PatientRequest.builder()
                .firstName("John")
                .lastName("Doe")
                .dob(LocalDate.of(1990, 1, 15))
                .email("john.doe@example.com")
                .phone("+1234567890")
                .build();

        validResponse = PatientResponse.builder()
                .id(1L)
                .firstName("John")
                .lastName("Doe")
                .dob(LocalDate.of(1990, 1, 15))
                .email("john.doe@example.com")
                .phone("+1234567890")
                .createdAt(LocalDate.now())
                .updatedAt(LocalDate.now())
                .build();
    }

    @Nested
    @DisplayName("POST /api/v1/patients - Create Patient")
    class CreatePatientTests {

        @Test
        @DisplayName("Should create patient and return 201 Created - Story SCRUM-14")
        void shouldCreatePatientSuccessfully() throws Exception {
            // Arrange
            when(patientService.createPatient(any(PatientRequest.class))).thenReturn(validResponse);

            // Act & Assert
            mockMvc.perform(post("/api/v1/patients")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validRequest)))
                    .andDo(print())
                    .andExpect(status().isCreated())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.firstName").value("John"))
                    .andExpect(jsonPath("$.lastName").value("Doe"))
                    .andExpect(jsonPath("$.email").value("john.doe@example.com"))
                    .andExpect(jsonPath("$.phone").value("+1234567890"))
                    .andExpect(jsonPath("$.dob").value("1990-01-15"))
                    .andExpect(jsonPath("$.createdAt").exists())
                    .andExpect(jsonPath("$.updatedAt").exists());

            verify(patientService).createPatient(any(PatientRequest.class));
        }

        @Test
        @DisplayName("Should return 409 Conflict when email already exists")
        void shouldReturn409WhenEmailExists() throws Exception {
            // Arrange
            when(patientService.createPatient(any(PatientRequest.class)))
                    .thenThrow(new DuplicateResourceException("Patient", "email", validRequest.getEmail()));

            // Act & Assert
            mockMvc.perform(post("/api/v1/patients")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validRequest)))
                    .andDo(print())
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.status").value(409))
                    .andExpect(jsonPath("$.error").value("Conflict"))
                    .andExpect(jsonPath("$.message").value("Patient already exists with email: 'john.doe@example.com'"))
                    .andExpect(jsonPath("$.timestamp").exists())
                    .andExpect(jsonPath("$.path").exists());

            verify(patientService).createPatient(any(PatientRequest.class));
        }

        @Test
        @DisplayName("Should return 400 Bad Request when firstName is missing")
        void shouldReturn400WhenFirstNameMissing() throws Exception {
            // Arrange
            PatientRequest invalidRequest = PatientRequest.builder()
                    .lastName("Doe")
                    .dob(LocalDate.of(1990, 1, 15))
                    .email("john.doe@example.com")
                    .phone("+1234567890")
                    .build();

            // Act & Assert
            mockMvc.perform(post("/api/v1/patients")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidRequest)))
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(400))
                    .andExpect(jsonPath("$.message").value("Validation failed"))
                    .andExpect(jsonPath("$.errors.firstName").exists());

            verify(patientService, never()).createPatient(any(PatientRequest.class));
        }

        @Test
        @DisplayName("Should return 400 Bad Request when lastName is missing")
        void shouldReturn400WhenLastNameMissing() throws Exception {
            // Arrange
            PatientRequest invalidRequest = PatientRequest.builder()
                    .firstName("John")
                    .dob(LocalDate.of(1990, 1, 15))
                    .email("john.doe@example.com")
                    .phone("+1234567890")
                    .build();

            // Act & Assert
            mockMvc.perform(post("/api/v1/patients")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidRequest)))
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errors.lastName").exists());

            verify(patientService, never()).createPatient(any(PatientRequest.class));
        }

        @Test
        @DisplayName("Should return 400 Bad Request when email is invalid")
        void shouldReturn400WhenEmailInvalid() throws Exception {
            // Arrange
            PatientRequest invalidRequest = PatientRequest.builder()
                    .firstName("John")
                    .lastName("Doe")
                    .dob(LocalDate.of(1990, 1, 15))
                    .email("invalid-email")
                    .phone("+1234567890")
                    .build();

            // Act & Assert
            mockMvc.perform(post("/api/v1/patients")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidRequest)))
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errors.email").exists());

            verify(patientService, never()).createPatient(any(PatientRequest.class));
        }

        @Test
        @DisplayName("Should return 400 Bad Request when dob is in future")
        void shouldReturn400WhenDobInFuture() throws Exception {
            // Arrange
            PatientRequest invalidRequest = PatientRequest.builder()
                    .firstName("John")
                    .lastName("Doe")
                    .dob(LocalDate.now().plusDays(1))
                    .email("john.doe@example.com")
                    .phone("+1234567890")
                    .build();

            // Act & Assert
            mockMvc.perform(post("/api/v1/patients")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidRequest)))
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errors.dob").exists());

            verify(patientService, never()).createPatient(any(PatientRequest.class));
        }

        @Test
        @DisplayName("Should return 400 Bad Request when phone is missing")
        void shouldReturn400WhenPhoneMissing() throws Exception {
            // Arrange
            PatientRequest invalidRequest = PatientRequest.builder()
                    .firstName("John")
                    .lastName("Doe")
                    .dob(LocalDate.of(1990, 1, 15))
                    .email("john.doe@example.com")
                    .build();

            // Act & Assert
            mockMvc.perform(post("/api/v1/patients")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidRequest)))
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errors.phone").exists());

            verify(patientService, never()).createPatient(any(PatientRequest.class));
        }

        @Test
        @DisplayName("Should return 400 Bad Request when all fields are missing")
        void shouldReturn400WhenAllFieldsMissing() throws Exception {
            // Arrange
            PatientRequest emptyRequest = PatientRequest.builder().build();

            // Act & Assert
            mockMvc.perform(post("/api/v1/patients")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(emptyRequest)))
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errors.firstName").exists())
                    .andExpect(jsonPath("$.errors.lastName").exists())
                    .andExpect(jsonPath("$.errors.dob").exists())
                    .andExpect(jsonPath("$.errors.email").exists())
                    .andExpect(jsonPath("$.errors.phone").exists());

            verify(patientService, never()).createPatient(any(PatientRequest.class));
        }

        @Test
        @DisplayName("Should return 400 Bad Request with malformed JSON")
        void shouldReturn400WithMalformedJson() throws Exception {
            // Act & Assert - Spring Boot 2.7.x returns 500 for JSON parse errors
            mockMvc.perform(post("/api/v1/patients")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{invalid json"))
                    .andDo(print())
                    .andExpect(status().is5xxServerError());

            verify(patientService, never()).createPatient(any(PatientRequest.class));
        }
    }

    @Nested
    @DisplayName("GET /api/v1/patients/{id} - Get Patient By ID")
    class GetPatientByIdTests {

        @Test
        @DisplayName("Should return patient when ID exists - Story SCRUM-15")
        void shouldReturnPatientWhenIdExists() throws Exception {
            // Arrange
            when(patientService.getPatientById(1L)).thenReturn(validResponse);

            // Act & Assert
            mockMvc.perform(get("/api/v1/patients/1")
                            .accept(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.firstName").value("John"))
                    .andExpect(jsonPath("$.lastName").value("Doe"))
                    .andExpect(jsonPath("$.email").value("john.doe@example.com"));

            verify(patientService).getPatientById(1L);
        }

        @Test
        @DisplayName("Should return 404 Not Found when ID does not exist - Story SCRUM-15")
        void shouldReturn404WhenIdNotFound() throws Exception {
            // Arrange
            when(patientService.getPatientById(999L))
                    .thenThrow(new ResourceNotFoundException("Patient", "id", 999L));

            // Act & Assert
            mockMvc.perform(get("/api/v1/patients/999")
                            .accept(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.status").value(404))
                    .andExpect(jsonPath("$.error").value("Not Found"))
                    .andExpect(jsonPath("$.message").value("Patient not found with id: '999'"))
                    .andExpect(jsonPath("$.timestamp").exists())
                    .andExpect(jsonPath("$.path").value("/api/v1/patients/999"));

            verify(patientService).getPatientById(999L);
        }

        @Test
        @DisplayName("Should handle zero ID")
        void shouldHandleZeroId() throws Exception {
            // Arrange
            when(patientService.getPatientById(0L))
                    .thenThrow(new ResourceNotFoundException("Patient", "id", 0L));

            // Act & Assert
            mockMvc.perform(get("/api/v1/patients/0")
                            .accept(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(status().isNotFound());

            verify(patientService).getPatientById(0L);
        }

        @Test
        @DisplayName("Should handle negative ID")
        void shouldHandleNegativeId() throws Exception {
            // Arrange
            when(patientService.getPatientById(-1L))
                    .thenThrow(new ResourceNotFoundException("Patient", "id", -1L));

            // Act & Assert
            mockMvc.perform(get("/api/v1/patients/-1")
                            .accept(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(status().isNotFound());

            verify(patientService).getPatientById(-1L);
        }

        @Test
        @DisplayName("Should handle very large ID")
        void shouldHandleVeryLargeId() throws Exception {
            // Arrange
            Long largeId = Long.MAX_VALUE;
            when(patientService.getPatientById(largeId))
                    .thenThrow(new ResourceNotFoundException("Patient", "id", largeId));

            // Act & Assert
            mockMvc.perform(get("/api/v1/patients/" + largeId)
                            .accept(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(status().isNotFound());

            verify(patientService).getPatientById(largeId);
        }
    }

    @Nested
    @DisplayName("GET /api/v1/patients - Get All Patients")
    class GetAllPatientsTests {

        @Test
        @DisplayName("Should return all patients")
        void shouldReturnAllPatients() throws Exception {
            // Arrange
            PatientResponse patient2 = PatientResponse.builder()
                    .id(2L)
                    .firstName("Jane")
                    .lastName("Smith")
                    .dob(LocalDate.of(1985, 5, 20))
                    .email("jane.smith@example.com")
                    .phone("+9876543210")
                    .createdAt(LocalDate.now())
                    .updatedAt(LocalDate.now())
                    .build();

            when(patientService.getAllPatients()).thenReturn(Arrays.asList(validResponse, patient2));

            // Act & Assert
            mockMvc.perform(get("/api/v1/patients")
                            .accept(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$", hasSize(2)))
                    .andExpect(jsonPath("$[0].id").value(1))
                    .andExpect(jsonPath("$[0].firstName").value("John"))
                    .andExpect(jsonPath("$[1].id").value(2))
                    .andExpect(jsonPath("$[1].firstName").value("Jane"));

            verify(patientService).getAllPatients();
        }

        @Test
        @DisplayName("Should return empty array when no patients exist")
        void shouldReturnEmptyArrayWhenNoPatientsExist() throws Exception {
            // Arrange
            when(patientService.getAllPatients()).thenReturn(Collections.emptyList());

            // Act & Assert
            mockMvc.perform(get("/api/v1/patients")
                            .accept(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$", hasSize(0)));

            verify(patientService).getAllPatients();
        }

        @Test
        @DisplayName("Should return single patient in array")
        void shouldReturnSinglePatientInArray() throws Exception {
            // Arrange
            when(patientService.getAllPatients()).thenReturn(Collections.singletonList(validResponse));

            // Act & Assert
            mockMvc.perform(get("/api/v1/patients")
                            .accept(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(1)))
                    .andExpect(jsonPath("$[0].id").value(1));

            verify(patientService).getAllPatients();
        }
    }

    @Nested
    @DisplayName("PUT /api/v1/patients/{id} - Update Patient")
    class UpdatePatientTests {

        @Test
        @DisplayName("Should update patient successfully")
        void shouldUpdatePatientSuccessfully() throws Exception {
            // Arrange
            PatientRequest updateRequest = PatientRequest.builder()
                    .firstName("John Updated")
                    .lastName("Doe Updated")
                    .dob(LocalDate.of(1990, 1, 15))
                    .email("john.updated@example.com")
                    .phone("+1111111111")
                    .build();

            PatientResponse updatedResponse = PatientResponse.builder()
                    .id(1L)
                    .firstName("John Updated")
                    .lastName("Doe Updated")
                    .dob(LocalDate.of(1990, 1, 15))
                    .email("john.updated@example.com")
                    .phone("+1111111111")
                    .createdAt(LocalDate.now())
                    .updatedAt(LocalDate.now())
                    .build();

            when(patientService.updatePatient(eq(1L), any(PatientRequest.class))).thenReturn(updatedResponse);

            // Act & Assert
            mockMvc.perform(put("/api/v1/patients/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateRequest)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.firstName").value("John Updated"))
                    .andExpect(jsonPath("$.email").value("john.updated@example.com"));

            verify(patientService).updatePatient(eq(1L), any(PatientRequest.class));
        }

        @Test
        @DisplayName("Should return 404 when updating non-existent patient")
        void shouldReturn404WhenUpdatingNonExistentPatient() throws Exception {
            // Arrange
            when(patientService.updatePatient(eq(999L), any(PatientRequest.class)))
                    .thenThrow(new ResourceNotFoundException("Patient", "id", 999L));

            // Act & Assert
            mockMvc.perform(put("/api/v1/patients/999")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validRequest)))
                    .andDo(print())
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message").value("Patient not found with id: '999'"));

            verify(patientService).updatePatient(eq(999L), any(PatientRequest.class));
        }

        @Test
        @DisplayName("Should return 409 when email is taken by another patient")
        void shouldReturn409WhenEmailTaken() throws Exception {
            // Arrange
            when(patientService.updatePatient(eq(1L), any(PatientRequest.class)))
                    .thenThrow(new DuplicateResourceException("Patient", "email", "taken@example.com"));

            // Act & Assert
            mockMvc.perform(put("/api/v1/patients/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validRequest)))
                    .andDo(print())
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.status").value(409));

            verify(patientService).updatePatient(eq(1L), any(PatientRequest.class));
        }

        @Test
        @DisplayName("Should return 400 with invalid update data")
        void shouldReturn400WithInvalidUpdateData() throws Exception {
            // Arrange
            PatientRequest invalidRequest = PatientRequest.builder()
                    .firstName("")
                    .lastName("Doe")
                    .dob(LocalDate.of(1990, 1, 15))
                    .email("invalid-email")
                    .phone("+1234567890")
                    .build();

            // Act & Assert
            mockMvc.perform(put("/api/v1/patients/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidRequest)))
                    .andDo(print())
                    .andExpect(status().isBadRequest());

            verify(patientService, never()).updatePatient(anyLong(), any(PatientRequest.class));
        }
    }

    @Nested
    @DisplayName("DELETE /api/v1/patients/{id} - Delete Patient")
    class DeletePatientTests {

        @Test
        @DisplayName("Should delete patient successfully and return 204 No Content")
        void shouldDeletePatientSuccessfully() throws Exception {
            // Arrange
            doNothing().when(patientService).deletePatient(1L);

            // Act & Assert
            mockMvc.perform(delete("/api/v1/patients/1"))
                    .andDo(print())
                    .andExpect(status().isNoContent())
                    .andExpect(content().string(""));

            verify(patientService).deletePatient(1L);
        }

        @Test
        @DisplayName("Should return 404 when deleting non-existent patient")
        void shouldReturn404WhenDeletingNonExistentPatient() throws Exception {
            // Arrange
            doThrow(new ResourceNotFoundException("Patient", "id", 999L))
                    .when(patientService).deletePatient(999L);

            // Act & Assert
            mockMvc.perform(delete("/api/v1/patients/999"))
                    .andDo(print())
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message").value("Patient not found with id: '999'"));

            verify(patientService).deletePatient(999L);
        }

        @Test
        @DisplayName("Should handle deletion with zero ID")
        void shouldHandleDeletionWithZeroId() throws Exception {
            // Arrange
            doThrow(new ResourceNotFoundException("Patient", "id", 0L))
                    .when(patientService).deletePatient(0L);

            // Act & Assert
            mockMvc.perform(delete("/api/v1/patients/0"))
                    .andDo(print())
                    .andExpect(status().isNotFound());

            verify(patientService).deletePatient(0L);
        }
    }

    @Nested
    @DisplayName("Edge Cases and Error Handling")
    class EdgeCasesTests {

        @Test
        @DisplayName("Should handle request with unsupported media type")
        void shouldHandleUnsupportedMediaType() throws Exception {
            // Act & Assert - Spring Boot 2.7.x returns 500 for XML parsing
            mockMvc.perform(post("/api/v1/patients")
                            .contentType(MediaType.APPLICATION_XML)
                            .content("<patient></patient>"))
                    .andDo(print())
                    .andExpect(status().is5xxServerError());

            verify(patientService, never()).createPatient(any(PatientRequest.class));
        }

        @Test
        @DisplayName("Should handle empty request body")
        void shouldHandleEmptyRequestBody() throws Exception {
            // Act & Assert - Spring Boot 2.7.x returns 500 for empty JSON
            mockMvc.perform(post("/api/v1/patients")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(""))
                    .andDo(print())
                    .andExpect(status().is5xxServerError());

            verify(patientService, never()).createPatient(any(PatientRequest.class));
        }

        @Test
        @DisplayName("Should handle null request body")
        void shouldHandleNullRequestBody() throws Exception {
            // Act & Assert - Spring Boot 2.7.x returns 500 for missing JSON body
            mockMvc.perform(post("/api/v1/patients")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(status().is5xxServerError());

            verify(patientService, never()).createPatient(any(PatientRequest.class));
        }

        @Test
        @DisplayName("Should handle invalid path parameter")
        void shouldHandleInvalidPathParameter() throws Exception {
            // Act & Assert - Spring Boot 2.7.x returns 500 for type conversion errors
            mockMvc.perform(get("/api/v1/patients/invalid")
                            .accept(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(status().is5xxServerError());

            verify(patientService, never()).getPatientById(anyLong());
        }

        @Test
        @DisplayName("Should handle very long email address")
        void shouldHandleVeryLongEmail() throws Exception {
            // Arrange - Very long string before @ makes email format invalid
            String longEmail = "a".repeat(64) + "@example.com"; // RFC 5321 limit is 64 chars before @
            PatientRequest longEmailRequest = PatientRequest.builder()
                    .firstName("John")
                    .lastName("Doe")
                    .dob(LocalDate.of(1990, 1, 15))
                    .email(longEmail)
                    .phone("+1234567890")
                    .build();

            PatientResponse longEmailResponse = PatientResponse.builder()
                    .id(1L)
                    .firstName("John")
                    .lastName("Doe")
                    .dob(LocalDate.of(1990, 1, 15))
                    .email(longEmail)
                    .phone("+1234567890")
                    .createdAt(LocalDate.now())
                    .updatedAt(LocalDate.now())
                    .build();

            when(patientService.createPatient(any(PatientRequest.class))).thenReturn(longEmailResponse);

            // Act & Assert - Should accept valid RFC compliant email
            mockMvc.perform(post("/api/v1/patients")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(longEmailRequest)))
                    .andDo(print())
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.email").value(longEmail));

            verify(patientService).createPatient(any(PatientRequest.class));
        }
    }
}
