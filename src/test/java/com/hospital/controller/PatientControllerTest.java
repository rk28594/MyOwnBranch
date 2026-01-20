package com.hospital.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hospital.model.Patient;
import com.hospital.service.PatientService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;

/**
 * API Integration tests using MockMvc
 * Simulates HTTP requests to test the REST API endpoints
 */
@WebMvcTest(PatientController.class)
@DisplayName("Patient Controller Integration Tests")
class PatientControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PatientService patientService;

    private Patient patient1;
    private Patient patient2;

    @BeforeEach
    void setUp() {
        patient1 = new Patient(
                "John",
                "Doe",
                LocalDate.of(1985, 5, 12),
                Patient.Gender.MALE,
                "Cardiology",
                false
        );
        patient1.setId(1L);

        patient2 = new Patient(
                "Sarah",
                "Smith",
                LocalDate.of(1992, 10, 24),
                Patient.Gender.FEMALE,
                "Emergency",
                true
        );
        patient2.setId(2L);
    }

    @Test
    @DisplayName("Should return 200 OK when getting all patients")
    void testGetAllPatients() throws Exception {
        List<Patient> patients = Arrays.asList(patient1, patient2);
        when(patientService.getAllPatients()).thenReturn(patients);

        mockMvc.perform(get("/api/v1/patients")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].firstName", is("John")))
                .andExpect(jsonPath("$[1].firstName", is("Sarah")));
    }

    @Test
    @DisplayName("Should return 200 OK when getting patient by ID")
    void testGetPatientById() throws Exception {
        when(patientService.getPatientById(1L)).thenReturn(Optional.of(patient1));

        mockMvc.perform(get("/api/v1/patients/1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName", is("John")))
                .andExpect(jsonPath("$.lastName", is("Doe")))
                .andExpect(jsonPath("$.department", is("Cardiology")));
    }

    @Test
    @DisplayName("Should return 404 when patient not found")
    void testGetPatientByIdNotFound() throws Exception {
        when(patientService.getPatientById(999L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/v1/patients/999")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should return 201 Created when creating new patient")
    void testCreatePatient() throws Exception {
        when(patientService.savePatient(any(Patient.class))).thenReturn(patient1);

        String patientJson = objectMapper.writeValueAsString(patient1);

        mockMvc.perform(post("/api/v1/patients")
                .contentType(MediaType.APPLICATION_JSON)
                .content(patientJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.firstName", is("John")))
                .andExpect(jsonPath("$.lastName", is("Doe")));
    }

    @Test
    @DisplayName("Should return 200 OK when updating patient")
    void testUpdatePatient() throws Exception {
        Patient updatedPatient = new Patient(
                "John",
                "Doe",
                LocalDate.of(1985, 5, 12),
                Patient.Gender.MALE,
                "Emergency",
                true
        );
        updatedPatient.setId(1L);

        when(patientService.getPatientById(1L)).thenReturn(Optional.of(patient1));
        when(patientService.savePatient(any(Patient.class))).thenReturn(updatedPatient);

        String patientJson = objectMapper.writeValueAsString(updatedPatient);

        mockMvc.perform(put("/api/v1/patients/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(patientJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.department", is("Emergency")))
                .andExpect(jsonPath("$.isCritical", is(true)));
    }

    @Test
    @DisplayName("Should return 404 when updating non-existent patient")
    void testUpdatePatientNotFound() throws Exception {
        when(patientService.getPatientById(999L)).thenReturn(Optional.empty());

        String patientJson = objectMapper.writeValueAsString(patient1);

        mockMvc.perform(put("/api/v1/patients/999")
                .contentType(MediaType.APPLICATION_JSON)
                .content(patientJson))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should return 204 No Content when deleting patient")
    void testDeletePatient() throws Exception {
        when(patientService.getPatientById(1L)).thenReturn(Optional.of(patient1));

        mockMvc.perform(delete("/api/v1/patients/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("Should return 404 when deleting non-existent patient")
    void testDeletePatientNotFound() throws Exception {
        when(patientService.getPatientById(999L)).thenReturn(Optional.empty());

        mockMvc.perform(delete("/api/v1/patients/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should return critical patients")
    void testGetCriticalPatients() throws Exception {
        List<Patient> criticalPatients = Arrays.asList(patient2);
        when(patientService.getCriticalPatients()).thenReturn(criticalPatients);

        mockMvc.perform(get("/api/v1/patients/critical")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].isCritical", is(true)))
                .andExpect(jsonPath("$[0].firstName", is("Sarah")));
    }

    @Test
    @DisplayName("Should return patients by department")
    void testGetPatientsByDepartment() throws Exception {
        List<Patient> cardiologyPatients = Arrays.asList(patient1);
        when(patientService.getPatientsByDepartment("Cardiology")).thenReturn(cardiologyPatients);

        mockMvc.perform(get("/api/v1/patients/department/Cardiology")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].department", is("Cardiology")))
                .andExpect(jsonPath("$[0].firstName", is("John")));
    }

    @Test
    @DisplayName("Should validate required fields when creating patient")
    void testCreatePatientWithInvalidData() throws Exception {
        Patient invalidPatient = new Patient();
        // Missing required fields

        String patientJson = objectMapper.writeValueAsString(invalidPatient);

        mockMvc.perform(post("/api/v1/patients")
                .contentType(MediaType.APPLICATION_JSON)
                .content(patientJson))
                .andExpect(status().isBadRequest());
    }
}
