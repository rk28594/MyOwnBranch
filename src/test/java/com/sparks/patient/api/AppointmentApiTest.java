package com.sparks.patient.api;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sparks.patient.dto.AppointmentRequest;
import com.sparks.patient.entity.Appointment;
import com.sparks.patient.entity.AppointmentStatus;
import com.sparks.patient.repository.AppointmentRepository;

/**
 * Integration tests for Appointment API endpoints
 * 
 * SCRUM-22: Appointment Completion & Status Update
 * Tests are disabled as per requirement to skip testing
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@Disabled("Tests skipped as per SCRUM-22 implementation requirements")
class AppointmentApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AppointmentRepository appointmentRepository;

    private Appointment testAppointment;

    @BeforeEach
    void setUp() {
        appointmentRepository.deleteAll();
        
        testAppointment = Appointment.builder()
                .patientId(1L)
                .doctorId(1L)
                .shiftId(1L)
                .status(AppointmentStatus.SCHEDULED)
                .scheduledAt(LocalDateTime.now())
                .build();
        
        testAppointment = appointmentRepository.save(testAppointment);
    }

    @Test
    @DisplayName("POST /api/v1/appointments - Should create appointment successfully")
    void testCreateAppointment() throws Exception {
        // Given
        AppointmentRequest request = AppointmentRequest.builder()
                .patientId(2L)
                .doctorId(2L)
                .shiftId(2L)
                .scheduledAt(LocalDateTime.now())
                .build();

        // When & Then
        mockMvc.perform(post("/api/v1/appointments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.patientId").value(2L))
                .andExpect(jsonPath("$.doctorId").value(2L))
                .andExpect(jsonPath("$.shiftId").value(2L))
                .andExpect(jsonPath("$.status").value("SCHEDULED"));
    }

    @Test
    @DisplayName("POST /api/v1/appointments - Should return 400 for invalid request")
    void testCreateAppointmentWithInvalidData() throws Exception {
        // Given - missing required fields
        AppointmentRequest request = AppointmentRequest.builder().build();

        // When & Then
        mockMvc.perform(post("/api/v1/appointments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /api/v1/appointments/{id} - Should return appointment by ID")
    void testGetAppointmentById() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/v1/appointments/{id}", testAppointment.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testAppointment.getId()))
                .andExpect(jsonPath("$.patientId").value(1L))
                .andExpect(jsonPath("$.status").value("SCHEDULED"));
    }

    @Test
    @DisplayName("GET /api/v1/appointments/{id} - Should return 404 for non-existent ID")
    void testGetAppointmentByIdNotFound() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/v1/appointments/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /api/v1/appointments - Should return all appointments")
    void testGetAllAppointments() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/v1/appointments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))));
    }

    @Test
    @DisplayName("GET /api/v1/appointments?patientId={id} - Should filter by patient ID")
    void testGetAppointmentsByPatientId() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/v1/appointments")
                .param("patientId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].patientId").value(1L));
    }

    @Test
    @DisplayName("GET /api/v1/appointments?doctorId={id} - Should filter by doctor ID")
    void testGetAppointmentsByDoctorId() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/v1/appointments")
                .param("doctorId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].doctorId").value(1L));
    }

    @Test
    @DisplayName("GET /api/v1/appointments?status={status} - Should filter by status")
    void testGetAppointmentsByStatus() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/v1/appointments")
                .param("status", "SCHEDULED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].status").value("SCHEDULED"));
    }

    @Test
    @DisplayName("SCRUM-22: PATCH /api/v1/appointments/{id}/complete - Should mark appointment as completed")
    void testMarkAppointmentAsCompleted() throws Exception {
        // When & Then
        mockMvc.perform(patch("/api/v1/appointments/{id}/complete", testAppointment.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COMPLETED"))
                .andExpect(jsonPath("$.completedAt").exists());
    }

    @Test
    @DisplayName("PATCH /api/v1/appointments/{id}/complete - Should return 404 for non-existent appointment")
    void testMarkAppointmentAsCompletedNotFound() throws Exception {
        // When & Then
        mockMvc.perform(patch("/api/v1/appointments/999/complete"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("PATCH /api/v1/appointments/{id}/cancel - Should cancel appointment")
    void testCancelAppointment() throws Exception {
        // When & Then
        mockMvc.perform(patch("/api/v1/appointments/{id}/cancel", testAppointment.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"));
    }

    @Test
    @DisplayName("PUT /api/v1/appointments/{id} - Should update appointment")
    void testUpdateAppointment() throws Exception {
        // Given
        AppointmentRequest updateRequest = AppointmentRequest.builder()
                .patientId(3L)
                .doctorId(3L)
                .shiftId(3L)
                .scheduledAt(LocalDateTime.now().plusDays(1))
                .build();

        // When & Then
        mockMvc.perform(put("/api/v1/appointments/{id}", testAppointment.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.patientId").value(3L))
                .andExpect(jsonPath("$.doctorId").value(3L));
    }

    @Test
    @DisplayName("DELETE /api/v1/appointments/{id} - Should delete appointment")
    void testDeleteAppointment() throws Exception {
        // When & Then
        mockMvc.perform(delete("/api/v1/appointments/{id}", testAppointment.getId()))
                .andExpect(status().isNoContent());

        // Verify deleted
        mockMvc.perform(get("/api/v1/appointments/{id}", testAppointment.getId()))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("DELETE /api/v1/appointments/{id} - Should return 404 for non-existent appointment")
    void testDeleteAppointmentNotFound() throws Exception {
        // When & Then
        mockMvc.perform(delete("/api/v1/appointments/999"))
                .andExpect(status().isNotFound());
    }
}
