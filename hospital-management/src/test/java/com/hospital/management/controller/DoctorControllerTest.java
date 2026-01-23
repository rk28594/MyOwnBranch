package com.hospital.management.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hospital.management.dto.DoctorRequest;
import com.hospital.management.dto.DoctorResponse;
import com.hospital.management.exception.DuplicateResourceException;
import com.hospital.management.exception.ResourceNotFoundException;
import com.hospital.management.service.DoctorService;
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
 * API/Integration tests for DoctorController - Story SCRUM-20
 * Test Scenario: When two doctors are registered with the same license, Then a 409 Conflict is returned
 */
@WebMvcTest(DoctorController.class)
@DisplayName("DoctorController API Tests - SCRUM-20")
class DoctorControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private DoctorService doctorService;

    private DoctorRequest validRequest;
    private DoctorResponse validResponse;

    @BeforeEach
    void setUp() {
        validRequest = DoctorRequest.builder()
                .fullName("Dr. John Smith")
                .licenseNumber("LIC-12345")
                .specialization("Cardiology")
                .deptId(1L)
                .build();

        validResponse = DoctorResponse.builder()
                .id(1L)
                .fullName("Dr. John Smith")
                .licenseNumber("LIC-12345")
                .specialization("Cardiology")
                .deptId(1L)
                .createdAt(LocalDate.now())
                .updatedAt(LocalDate.now())
                .build();
    }

    @Nested
    @DisplayName("POST /api/v1/doctors - Create Doctor")
    class CreateDoctorTests {

        @Test
        @DisplayName("Should create doctor and return 201 Created - Story SCRUM-20")
        void shouldCreateDoctorSuccessfully() throws Exception {
            // Arrange
            when(doctorService.createDoctor(any(DoctorRequest.class))).thenReturn(validResponse);

            // Act & Assert
            mockMvc.perform(post("/api/v1/doctors")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validRequest)))
                    .andDo(print())
                    .andExpect(status().isCreated())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.fullName").value("Dr. John Smith"))
                    .andExpect(jsonPath("$.licenseNumber").value("LIC-12345"))
                    .andExpect(jsonPath("$.specialization").value("Cardiology"))
                    .andExpect(jsonPath("$.deptId").value(1))
                    .andExpect(jsonPath("$.createdAt").exists())
                    .andExpect(jsonPath("$.updatedAt").exists());

            verify(doctorService).createDoctor(any(DoctorRequest.class));
        }

        @Test
        @DisplayName("Should return 409 Conflict when license number already exists - SCRUM-20 AC")
        void shouldReturn409WhenLicenseNumberExists() throws Exception {
            // Arrange
            when(doctorService.createDoctor(any(DoctorRequest.class)))
                    .thenThrow(new DuplicateResourceException("Doctor", "licenseNumber", validRequest.getLicenseNumber()));

            // Act & Assert
            mockMvc.perform(post("/api/v1/doctors")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validRequest)))
                    .andDo(print())
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.status").value(409))
                    .andExpect(jsonPath("$.error").value("Conflict"))
                    .andExpect(jsonPath("$.message").value("Doctor already exists with licenseNumber: 'LIC-12345'"))
                    .andExpect(jsonPath("$.timestamp").exists())
                    .andExpect(jsonPath("$.path").exists());

            verify(doctorService).createDoctor(any(DoctorRequest.class));
        }

        @Test
        @DisplayName("Should return 400 Bad Request when fullName is missing")
        void shouldReturn400WhenFullNameMissing() throws Exception {
            // Arrange
            DoctorRequest invalidRequest = DoctorRequest.builder()
                    .licenseNumber("LIC-12345")
                    .specialization("Cardiology")
                    .deptId(1L)
                    .build();

            // Act & Assert
            mockMvc.perform(post("/api/v1/doctors")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidRequest)))
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(400))
                    .andExpect(jsonPath("$.message").value("Validation failed"))
                    .andExpect(jsonPath("$.errors.fullName").exists());

            verify(doctorService, never()).createDoctor(any(DoctorRequest.class));
        }

        @Test
        @DisplayName("Should return 400 Bad Request when licenseNumber is missing")
        void shouldReturn400WhenLicenseNumberMissing() throws Exception {
            // Arrange
            DoctorRequest invalidRequest = DoctorRequest.builder()
                    .fullName("Dr. John Smith")
                    .specialization("Cardiology")
                    .deptId(1L)
                    .build();

            // Act & Assert
            mockMvc.perform(post("/api/v1/doctors")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidRequest)))
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errors.licenseNumber").exists());

            verify(doctorService, never()).createDoctor(any(DoctorRequest.class));
        }

        @Test
        @DisplayName("Should return 400 Bad Request when specialization is missing")
        void shouldReturn400WhenSpecializationMissing() throws Exception {
            // Arrange
            DoctorRequest invalidRequest = DoctorRequest.builder()
                    .fullName("Dr. John Smith")
                    .licenseNumber("LIC-12345")
                    .deptId(1L)
                    .build();

            // Act & Assert
            mockMvc.perform(post("/api/v1/doctors")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidRequest)))
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errors.specialization").exists());

            verify(doctorService, never()).createDoctor(any(DoctorRequest.class));
        }
    }

    @Nested
    @DisplayName("GET /api/v1/doctors/{id} - Get Doctor By ID")
    class GetDoctorByIdTests {

        @Test
        @DisplayName("Should return doctor when ID exists")
        void shouldReturnDoctorWhenIdExists() throws Exception {
            // Arrange
            when(doctorService.getDoctorById(1L)).thenReturn(validResponse);

            // Act & Assert
            mockMvc.perform(get("/api/v1/doctors/1"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.fullName").value("Dr. John Smith"))
                    .andExpect(jsonPath("$.licenseNumber").value("LIC-12345"));

            verify(doctorService).getDoctorById(1L);
        }

        @Test
        @DisplayName("Should return 404 when doctor not found")
        void shouldReturn404WhenDoctorNotFound() throws Exception {
            // Arrange
            when(doctorService.getDoctorById(999L))
                    .thenThrow(new ResourceNotFoundException("Doctor", "id", "999"));

            // Act & Assert
            mockMvc.perform(get("/api/v1/doctors/999"))
                    .andDo(print())
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.status").value(404))
                    .andExpect(jsonPath("$.message").value("Doctor not found with id: '999'"));

            verify(doctorService).getDoctorById(999L);
        }
    }

    @Nested
    @DisplayName("GET /api/v1/doctors - Get All Doctors")
    class GetAllDoctorsTests {

        @Test
        @DisplayName("Should return all doctors")
        void shouldReturnAllDoctors() throws Exception {
            // Arrange
            DoctorResponse doctor2 = DoctorResponse.builder()
                    .id(2L)
                    .fullName("Dr. Jane Doe")
                    .licenseNumber("LIC-67890")
                    .specialization("Neurology")
                    .deptId(2L)
                    .createdAt(LocalDate.now())
                    .updatedAt(LocalDate.now())
                    .build();

            when(doctorService.getAllDoctors()).thenReturn(Arrays.asList(validResponse, doctor2));

            // Act & Assert
            mockMvc.perform(get("/api/v1/doctors"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$", hasSize(2)))
                    .andExpect(jsonPath("$[0].licenseNumber").value("LIC-12345"))
                    .andExpect(jsonPath("$[1].licenseNumber").value("LIC-67890"));
        }

        @Test
        @DisplayName("Should return empty array when no doctors exist")
        void shouldReturnEmptyArrayWhenNoDoctors() throws Exception {
            // Arrange
            when(doctorService.getAllDoctors()).thenReturn(Collections.emptyList());

            // Act & Assert
            mockMvc.perform(get("/api/v1/doctors"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(0)));
        }
    }

    @Nested
    @DisplayName("PUT /api/v1/doctors/{id} - Update Doctor")
    class UpdateDoctorTests {

        @Test
        @DisplayName("Should update doctor successfully")
        void shouldUpdateDoctorSuccessfully() throws Exception {
            // Arrange
            DoctorRequest updateRequest = DoctorRequest.builder()
                    .fullName("Dr. John Smith Jr.")
                    .licenseNumber("LIC-12345")
                    .specialization("Oncology")
                    .deptId(2L)
                    .build();

            DoctorResponse updatedResponse = DoctorResponse.builder()
                    .id(1L)
                    .fullName("Dr. John Smith Jr.")
                    .licenseNumber("LIC-12345")
                    .specialization("Oncology")
                    .deptId(2L)
                    .createdAt(LocalDate.now())
                    .updatedAt(LocalDate.now())
                    .build();

            when(doctorService.updateDoctor(eq(1L), any(DoctorRequest.class))).thenReturn(updatedResponse);

            // Act & Assert
            mockMvc.perform(put("/api/v1/doctors/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateRequest)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.fullName").value("Dr. John Smith Jr."))
                    .andExpect(jsonPath("$.specialization").value("Oncology"));
        }

        @Test
        @DisplayName("Should return 404 when updating non-existent doctor")
        void shouldReturn404WhenUpdatingNonExistent() throws Exception {
            // Arrange
            when(doctorService.updateDoctor(eq(999L), any(DoctorRequest.class)))
                    .thenThrow(new ResourceNotFoundException("Doctor", "id", "999"));

            // Act & Assert
            mockMvc.perform(put("/api/v1/doctors/999")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validRequest)))
                    .andDo(print())
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("DELETE /api/v1/doctors/{id} - Delete Doctor")
    class DeleteDoctorTests {

        @Test
        @DisplayName("Should delete doctor and return 204")
        void shouldDeleteDoctorSuccessfully() throws Exception {
            // Arrange
            doNothing().when(doctorService).deleteDoctor(1L);

            // Act & Assert
            mockMvc.perform(delete("/api/v1/doctors/1"))
                    .andDo(print())
                    .andExpect(status().isNoContent());

            verify(doctorService).deleteDoctor(1L);
        }

        @Test
        @DisplayName("Should return 404 when deleting non-existent doctor")
        void shouldReturn404WhenDeletingNonExistent() throws Exception {
            // Arrange
            doThrow(new ResourceNotFoundException("Doctor", "id", "999"))
                    .when(doctorService).deleteDoctor(999L);

            // Act & Assert
            mockMvc.perform(delete("/api/v1/doctors/999"))
                    .andDo(print())
                    .andExpect(status().isNotFound());
        }
    }
}
