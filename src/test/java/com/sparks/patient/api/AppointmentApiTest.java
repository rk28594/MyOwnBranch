package com.sparks.patient.api;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.matchesPattern;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sparks.patient.controller.AppointmentController;
import com.sparks.patient.dto.AppointmentRequest;
import com.sparks.patient.dto.AppointmentResponse;
import com.sparks.patient.exception.AppointmentNotFoundException;
import com.sparks.patient.exception.DoctorNotFoundException;
import com.sparks.patient.exception.PatientNotFoundException;
import com.sparks.patient.service.AppointmentService;

/**
 * API tests for Appointment Controller - SCRUM-23
 */
@WebMvcTest(AppointmentController.class)
@DisplayName("Appointment API Tests")
class AppointmentApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AppointmentService appointmentService;

    private AppointmentRequest request;
    private AppointmentResponse response;

    @BeforeEach
    void setUp() {
        LocalDateTime appointmentTime = LocalDateTime.of(2026, 2, 10, 10, 0);

        request = AppointmentRequest.builder()
                .patientId(1L)
                .doctorId(2L)
                .appointmentTime(appointmentTime)
                .build();

        response = AppointmentResponse.builder()
                .id(10L)
                .appointmentId("550e8400-e29b-41d4-a716-446655440000")
                .patientId(1L)
                .patientName("John Doe")
                .doctorId(2L)
                .doctorName("Dr. Smith")
                .appointmentTime(appointmentTime)
                .status("SCHEDULED")
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("POST /api/appointments - Should create appointment with UUID")
    void testCreateAppointment_Success() throws Exception {
        // Given
        when(appointmentService.createAppointment(any(AppointmentRequest.class))).thenReturn(response);

        // When & Then
        mockMvc.perform(post("/api/appointments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.appointmentId").value("550e8400-e29b-41d4-a716-446655440000"))
                .andExpect(jsonPath("$.appointmentId").value(matchesPattern("^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$")))
                .andExpect(jsonPath("$.status").value("SCHEDULED"))
                .andExpect(jsonPath("$.patientId").value(1))
                .andExpect(jsonPath("$.doctorId").value(2));
    }

    @Test
    @DisplayName("POST /api/appointments - Should return 400 for invalid request")
    void testCreateAppointment_InvalidRequest() throws Exception {
        // Given
        AppointmentRequest invalidRequest = AppointmentRequest.builder()
                .patientId(null)  // Missing required field
                .doctorId(2L)
                .appointmentTime(LocalDateTime.now())
                .build();

        // When & Then
        mockMvc.perform(post("/api/appointments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/appointments - Should return 404 when patient not found")
    void testCreateAppointment_PatientNotFound() throws Exception {
        // Given
        when(appointmentService.createAppointment(any(AppointmentRequest.class)))
                .thenThrow(new PatientNotFoundException("Patient not found with id: 1"));

        // When & Then
        mockMvc.perform(post("/api/appointments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Patient not found with id: 1"));
    }

    @Test
    @DisplayName("POST /api/appointments - Should return 404 when doctor not found")
    void testCreateAppointment_DoctorNotFound() throws Exception {
        // Given
        when(appointmentService.createAppointment(any(AppointmentRequest.class)))
                .thenThrow(new DoctorNotFoundException("Doctor not found with id: 2"));

        // When & Then
        mockMvc.perform(post("/api/appointments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Doctor not found with id: 2"));
    }

    @Test
    @DisplayName("GET /api/appointments/{appointmentId} - Should return appointment")
    void testGetAppointment_Success() throws Exception {
        // Given
        String appointmentId = "550e8400-e29b-41d4-a716-446655440000";
        when(appointmentService.getAppointmentById(appointmentId)).thenReturn(response);

        // When & Then
        mockMvc.perform(get("/api/appointments/{appointmentId}", appointmentId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.appointmentId").value(appointmentId))
                .andExpect(jsonPath("$.status").value("SCHEDULED"));
    }

    @Test
    @DisplayName("GET /api/appointments/{appointmentId} - Should return 404 when not found")
    void testGetAppointment_NotFound() throws Exception {
        // Given
        String appointmentId = "invalid-uuid";
        when(appointmentService.getAppointmentById(appointmentId))
                .thenThrow(new AppointmentNotFoundException("Appointment not found with id: " + appointmentId));

        // When & Then
        mockMvc.perform(get("/api/appointments/{appointmentId}", appointmentId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Appointment not found with id: " + appointmentId));
    }

    @Test
    @DisplayName("GET /api/appointments - Should return all appointments")
    void testGetAllAppointments() throws Exception {
        // Given
        List<AppointmentResponse> responses = Arrays.asList(response);
        when(appointmentService.getAllAppointments()).thenReturn(responses);

        // When & Then
        mockMvc.perform(get("/api/appointments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].appointmentId").value("550e8400-e29b-41d4-a716-446655440000"));
    }

    @Test
    @DisplayName("GET /api/appointments?patientId=1 - Should return patient appointments")
    void testGetAppointmentsByPatientId() throws Exception {
        // Given
        List<AppointmentResponse> responses = Arrays.asList(response);
        when(appointmentService.getAppointmentsByPatientId(anyLong())).thenReturn(responses);

        // When & Then
        mockMvc.perform(get("/api/appointments").param("patientId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].patientId").value(1));
    }

    @Test
    @DisplayName("GET /api/appointments?doctorId=2 - Should return doctor appointments")
    void testGetAppointmentsByDoctorId() throws Exception {
        // Given
        List<AppointmentResponse> responses = Arrays.asList(response);
        when(appointmentService.getAppointmentsByDoctorId(anyLong())).thenReturn(responses);

        // When & Then
        mockMvc.perform(get("/api/appointments").param("doctorId", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].doctorId").value(2));
    }
}
