package com.hospital.management.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.hospital.management.dto.ShiftRequest;
import com.hospital.management.dto.ShiftResponse;
import com.hospital.management.exception.InvalidTimeSlotException;
import com.hospital.management.exception.ResourceNotFoundException;
import com.hospital.management.exception.ShiftConflictException;
import com.hospital.management.service.ShiftService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
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
 * API/Integration tests for ShiftController - Stories SCRUM-18 & SCRUM-19
 * 
 * SCRUM-18: Shift Definition & Time-Slot Logic
 * Test Scenario: When startTime is 10:00 and endTime is 09:00, Then save operation fails with validation error
 * 
 * SCRUM-19: Shift Conflict Validator (Service Layer)
 * Test Scenario: Given a doctor is busy from 1 PM to 3 PM, When adding a shift at 2 PM, Then system rejects it
 */
@WebMvcTest(ShiftController.class)
@DisplayName("ShiftController API Tests - SCRUM-18 & SCRUM-19")
class ShiftControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ShiftService shiftService;

    private ShiftRequest validRequest;
    private ShiftResponse validResponse;
    private LocalDateTime baseTime;

    @BeforeEach
    void setUp() {
        objectMapper.registerModule(new JavaTimeModule());
        
        baseTime = LocalDateTime.of(2026, 1, 23, 10, 0);
        
        validRequest = ShiftRequest.builder()
                .doctorId(1L)
                .startTime(baseTime)
                .endTime(baseTime.plusHours(2))
                .room("Room 101")
                .build();

        validResponse = ShiftResponse.builder()
                .id(1L)
                .doctorId(1L)
                .startTime(baseTime)
                .endTime(baseTime.plusHours(2))
                .room("Room 101")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Nested
    @DisplayName("POST /api/v1/shifts - Create Shift")
    class CreateShiftTests {

        @Test
        @DisplayName("Should create shift and return 201 Created - Story SCRUM-18")
        void shouldCreateShiftSuccessfully() throws Exception {
            // Arrange
            when(shiftService.createShift(any(ShiftRequest.class))).thenReturn(validResponse);

            // Act & Assert
            mockMvc.perform(post("/api/v1/shifts")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validRequest)))
                    .andDo(print())
                    .andExpect(status().isCreated())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.doctorId").value(1))
                    .andExpect(jsonPath("$.room").value("Room 101"))
                    .andExpect(jsonPath("$.createdAt").exists())
                    .andExpect(jsonPath("$.updatedAt").exists());

            verify(shiftService).createShift(any(ShiftRequest.class));
        }

        @Test
        @DisplayName("Should return 400 when endTime is before startTime - SCRUM-18 AC")
        void shouldReturn400WhenEndTimeBeforeStartTime() throws Exception {
            // Arrange - startTime 10:00, endTime 09:00
            ShiftRequest invalidRequest = ShiftRequest.builder()
                    .doctorId(1L)
                    .startTime(LocalDateTime.of(2026, 1, 23, 10, 0))
                    .endTime(LocalDateTime.of(2026, 1, 23, 9, 0))
                    .room("Room 101")
                    .build();

            when(shiftService.createShift(any(ShiftRequest.class)))
                    .thenThrow(new InvalidTimeSlotException(
                            "End time (2026-01-23T09:00) must be strictly after start time (2026-01-23T10:00)"));

            // Act & Assert
            mockMvc.perform(post("/api/v1/shifts")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidRequest)))
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(400))
                    .andExpect(jsonPath("$.message").value(containsString("End time")))
                    .andExpect(jsonPath("$.message").value(containsString("must be strictly after start time")));
        }

        @Test
        @DisplayName("Should return 409 Conflict when shift conflicts with existing - SCRUM-19 AC")
        void shouldReturn409WhenShiftConflicts() throws Exception {
            // Arrange - Doctor busy from 1 PM to 3 PM, trying to add shift at 2 PM
            ShiftRequest conflictingRequest = ShiftRequest.builder()
                    .doctorId(1L)
                    .startTime(LocalDateTime.of(2026, 1, 23, 14, 0))
                    .endTime(LocalDateTime.of(2026, 1, 23, 16, 0))
                    .room("Room 102")
                    .build();

            when(shiftService.createShift(any(ShiftRequest.class)))
                    .thenThrow(new ShiftConflictException(1L, 
                            LocalDateTime.of(2026, 1, 23, 13, 0), 
                            LocalDateTime.of(2026, 1, 23, 15, 0)));

            // Act & Assert
            mockMvc.perform(post("/api/v1/shifts")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(conflictingRequest)))
                    .andDo(print())
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.status").value(409))
                    .andExpect(jsonPath("$.error").value("Conflict"))
                    .andExpect(jsonPath("$.message").value(containsString("Shift conflict")));

            verify(shiftService).createShift(any(ShiftRequest.class));
        }

        @Test
        @DisplayName("Should return 404 when doctor not found")
        void shouldReturn404WhenDoctorNotFound() throws Exception {
            // Arrange
            when(shiftService.createShift(any(ShiftRequest.class)))
                    .thenThrow(new ResourceNotFoundException("Doctor", "id", "1"));

            // Act & Assert
            mockMvc.perform(post("/api/v1/shifts")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validRequest)))
                    .andDo(print())
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.status").value(404));
        }

        @Test
        @DisplayName("Should return 400 when doctorId is missing")
        void shouldReturn400WhenDoctorIdMissing() throws Exception {
            // Arrange
            ShiftRequest invalidRequest = ShiftRequest.builder()
                    .startTime(baseTime)
                    .endTime(baseTime.plusHours(2))
                    .room("Room 101")
                    .build();

            // Act & Assert
            mockMvc.perform(post("/api/v1/shifts")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidRequest)))
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errors.doctorId").exists());

            verify(shiftService, never()).createShift(any(ShiftRequest.class));
        }

        @Test
        @DisplayName("Should return 400 when startTime is missing")
        void shouldReturn400WhenStartTimeMissing() throws Exception {
            // Arrange
            ShiftRequest invalidRequest = ShiftRequest.builder()
                    .doctorId(1L)
                    .endTime(baseTime.plusHours(2))
                    .room("Room 101")
                    .build();

            // Act & Assert
            mockMvc.perform(post("/api/v1/shifts")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidRequest)))
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errors.startTime").exists());

            verify(shiftService, never()).createShift(any(ShiftRequest.class));
        }

        @Test
        @DisplayName("Should return 400 when endTime is missing")
        void shouldReturn400WhenEndTimeMissing() throws Exception {
            // Arrange
            ShiftRequest invalidRequest = ShiftRequest.builder()
                    .doctorId(1L)
                    .startTime(baseTime)
                    .room("Room 101")
                    .build();

            // Act & Assert
            mockMvc.perform(post("/api/v1/shifts")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidRequest)))
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errors.endTime").exists());

            verify(shiftService, never()).createShift(any(ShiftRequest.class));
        }
    }

    @Nested
    @DisplayName("GET /api/v1/shifts/{id} - Get Shift By ID")
    class GetShiftByIdTests {

        @Test
        @DisplayName("Should return shift when ID exists")
        void shouldReturnShiftWhenIdExists() throws Exception {
            // Arrange
            when(shiftService.getShiftById(1L)).thenReturn(validResponse);

            // Act & Assert
            mockMvc.perform(get("/api/v1/shifts/1"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.doctorId").value(1))
                    .andExpect(jsonPath("$.room").value("Room 101"));

            verify(shiftService).getShiftById(1L);
        }

        @Test
        @DisplayName("Should return 404 when shift not found")
        void shouldReturn404WhenShiftNotFound() throws Exception {
            // Arrange
            when(shiftService.getShiftById(999L))
                    .thenThrow(new ResourceNotFoundException("Shift", "id", "999"));

            // Act & Assert
            mockMvc.perform(get("/api/v1/shifts/999"))
                    .andDo(print())
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.status").value(404))
                    .andExpect(jsonPath("$.message").value("Shift not found with id: '999'"));

            verify(shiftService).getShiftById(999L);
        }
    }

    @Nested
    @DisplayName("GET /api/v1/shifts - Get All Shifts")
    class GetAllShiftsTests {

        @Test
        @DisplayName("Should return all shifts")
        void shouldReturnAllShifts() throws Exception {
            // Arrange
            ShiftResponse shift2 = ShiftResponse.builder()
                    .id(2L)
                    .doctorId(2L)
                    .startTime(baseTime.plusDays(1))
                    .endTime(baseTime.plusDays(1).plusHours(2))
                    .room("Room 102")
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();

            when(shiftService.getAllShifts()).thenReturn(Arrays.asList(validResponse, shift2));

            // Act & Assert
            mockMvc.perform(get("/api/v1/shifts"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$", hasSize(2)))
                    .andExpect(jsonPath("$[0].room").value("Room 101"))
                    .andExpect(jsonPath("$[1].room").value("Room 102"));
        }

        @Test
        @DisplayName("Should return empty array when no shifts exist")
        void shouldReturnEmptyArrayWhenNoShifts() throws Exception {
            // Arrange
            when(shiftService.getAllShifts()).thenReturn(Collections.emptyList());

            // Act & Assert
            mockMvc.perform(get("/api/v1/shifts"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(0)));
        }
    }

    @Nested
    @DisplayName("GET /api/v1/shifts/doctor/{doctorId} - Get Shifts By Doctor")
    class GetShiftsByDoctorTests {

        @Test
        @DisplayName("Should return shifts for doctor")
        void shouldReturnShiftsForDoctor() throws Exception {
            // Arrange
            when(shiftService.getShiftsByDoctorId(1L)).thenReturn(Arrays.asList(validResponse));

            // Act & Assert
            mockMvc.perform(get("/api/v1/shifts/doctor/1"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(1)))
                    .andExpect(jsonPath("$[0].doctorId").value(1));
        }
    }

    @Nested
    @DisplayName("PUT /api/v1/shifts/{id} - Update Shift")
    class UpdateShiftTests {

        @Test
        @DisplayName("Should update shift successfully")
        void shouldUpdateShiftSuccessfully() throws Exception {
            // Arrange
            ShiftRequest updateRequest = ShiftRequest.builder()
                    .doctorId(1L)
                    .startTime(baseTime.plusHours(4))
                    .endTime(baseTime.plusHours(6))
                    .room("Room 201")
                    .build();

            ShiftResponse updatedResponse = ShiftResponse.builder()
                    .id(1L)
                    .doctorId(1L)
                    .startTime(updateRequest.getStartTime())
                    .endTime(updateRequest.getEndTime())
                    .room("Room 201")
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();

            when(shiftService.updateShift(eq(1L), any(ShiftRequest.class))).thenReturn(updatedResponse);

            // Act & Assert
            mockMvc.perform(put("/api/v1/shifts/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateRequest)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.room").value("Room 201"));
        }

        @Test
        @DisplayName("Should return 404 when updating non-existent shift")
        void shouldReturn404WhenUpdatingNonExistent() throws Exception {
            // Arrange
            when(shiftService.updateShift(eq(999L), any(ShiftRequest.class)))
                    .thenThrow(new ResourceNotFoundException("Shift", "id", "999"));

            // Act & Assert
            mockMvc.perform(put("/api/v1/shifts/999")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validRequest)))
                    .andDo(print())
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("DELETE /api/v1/shifts/{id} - Delete Shift")
    class DeleteShiftTests {

        @Test
        @DisplayName("Should delete shift and return 204")
        void shouldDeleteShiftSuccessfully() throws Exception {
            // Arrange
            doNothing().when(shiftService).deleteShift(1L);

            // Act & Assert
            mockMvc.perform(delete("/api/v1/shifts/1"))
                    .andDo(print())
                    .andExpect(status().isNoContent());

            verify(shiftService).deleteShift(1L);
        }

        @Test
        @DisplayName("Should return 404 when deleting non-existent shift")
        void shouldReturn404WhenDeletingNonExistent() throws Exception {
            // Arrange
            doThrow(new ResourceNotFoundException("Shift", "id", "999"))
                    .when(shiftService).deleteShift(999L);

            // Act & Assert
            mockMvc.perform(delete("/api/v1/shifts/999"))
                    .andDo(print())
                    .andExpect(status().isNotFound());
        }
    }
}
