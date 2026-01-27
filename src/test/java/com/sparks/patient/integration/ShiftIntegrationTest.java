package com.sparks.patient.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalTime;

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
import com.sparks.patient.dto.ShiftRequest;
import com.sparks.patient.entity.Shift;
import com.sparks.patient.repository.ShiftRepository;

/**
 * Integration Tests for Shift Management API
 * Tests the full application context with real database
 * 
 * SCRUM-18: Shift Definition & Time-Slot Logic
 * Acceptance Criteria: endTime must be strictly after startTime
 * Test Scenario: When startTime is 10:00 and endTime is 09:00, save operation fails with validation error
 */
@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@DisplayName("Shift Management Integration Tests")
class ShiftIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ShiftRepository shiftRepository;

    private ShiftRequest validShiftRequest;

    @BeforeEach
    void setUp() {
        shiftRepository.deleteAll();
        
        validShiftRequest = ShiftRequest.builder()
                .doctorId(1L)
                .startTime(LocalTime.of(9, 0))
                .endTime(LocalTime.of(17, 0))
                .room("Room-101")
                .build();
    }

    @Nested
    @DisplayName("SCRUM-18: Shift Creation Tests")
    class ShiftCreationTests {

        @Test
        @DisplayName("POST /api/v1/shifts returns 201 Created - Valid time slot")
        void shouldCreateShiftAndReturn201() throws Exception {
            // When/Then
            MvcResult result = mockMvc.perform(post("/api/v1/shifts")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(validShiftRequest)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").exists())
                    .andExpect(jsonPath("$.doctorId").value(1))
                    .andExpect(jsonPath("$.startTime").value("09:00:00"))
                    .andExpect(jsonPath("$.endTime").value("17:00:00"))
                    .andExpect(jsonPath("$.room").value("Room-101"))
                    .andExpect(jsonPath("$.createdAt").exists())
                    .andReturn();

            // Verify data is persisted
            assertThat(shiftRepository.count()).isEqualTo(1);
        }

        @Test
        @DisplayName("SCRUM-18 Test Scenario: When startTime is 10:00 and endTime is 09:00, save operation fails")
        void shouldReturn400WhenEndTimeBeforeStartTime() throws Exception {
            // Given - Test Scenario from SCRUM-18: startTime 10:00, endTime 09:00
            ShiftRequest invalidRequest = ShiftRequest.builder()
                    .doctorId(1L)
                    .startTime(LocalTime.of(10, 0))  // 10:00
                    .endTime(LocalTime.of(9, 0))     // 09:00
                    .room("Room-101")
                    .build();

            // When/Then - save operation fails with validation error
            mockMvc.perform(post("/api/v1/shifts")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(invalidRequest)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("End time must be strictly after start time"));

            // Verify data is NOT persisted
            assertThat(shiftRepository.count()).isEqualTo(0);
        }

        @Test
        @DisplayName("Should return 400 when startTime equals endTime")
        void shouldReturn400WhenStartTimeEqualsEndTime() throws Exception {
            // Given
            ShiftRequest invalidRequest = ShiftRequest.builder()
                    .doctorId(1L)
                    .startTime(LocalTime.of(10, 0))
                    .endTime(LocalTime.of(10, 0))
                    .room("Room-101")
                    .build();

            // When/Then
            mockMvc.perform(post("/api/v1/shifts")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(invalidRequest)))
                    .andExpect(status().isBadRequest());

            assertThat(shiftRepository.count()).isEqualTo(0);
        }

        @Test
        @DisplayName("Should return 400 for missing required fields")
        void shouldReturn400ForMissingRequiredFields() throws Exception {
            // Given
            ShiftRequest invalidRequest = ShiftRequest.builder()
                    .doctorId(1L)
                    // Missing startTime, endTime, room
                    .build();

            // When/Then
            mockMvc.perform(post("/api/v1/shifts")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(invalidRequest)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(400))
                    .andExpect(jsonPath("$.message").value("Validation failed"))
                    .andExpect(jsonPath("$.errors.startTime").exists())
                    .andExpect(jsonPath("$.errors.endTime").exists())
                    .andExpect(jsonPath("$.errors.room").exists());
        }
    }

    @Nested
    @DisplayName("Shift Retrieval Tests")
    class ShiftRetrievalTests {

        @Test
        @DisplayName("GET /api/v1/shifts/{id} returns shift when exists")
        void shouldReturnShiftWhenExists() throws Exception {
            // Given
            Shift shift = Shift.builder()
                    .doctorId(1L)
                    .startTime(LocalTime.of(9, 0))
                    .endTime(LocalTime.of(17, 0))
                    .room("Room-101")
                    .build();
            Shift saved = shiftRepository.save(shift);

            // When/Then
            mockMvc.perform(get("/api/v1/shifts/{id}", saved.getId()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(saved.getId()))
                    .andExpect(jsonPath("$.doctorId").value(1))
                    .andExpect(jsonPath("$.room").value("Room-101"));
        }

        @Test
        @DisplayName("GET /api/v1/shifts/{id} returns 404 when not found")
        void shouldReturn404WhenShiftNotFound() throws Exception {
            // When/Then
            mockMvc.perform(get("/api/v1/shifts/{id}", 999L))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.status").value(404))
                    .andExpect(jsonPath("$.message").value("Shift not found with ID: 999"));
        }

        @Test
        @DisplayName("GET /api/v1/shifts returns all shifts")
        void shouldReturnAllShifts() throws Exception {
            // Given
            Shift shift1 = Shift.builder()
                    .doctorId(1L)
                    .startTime(LocalTime.of(9, 0))
                    .endTime(LocalTime.of(17, 0))
                    .room("Room-101")
                    .build();
            Shift shift2 = Shift.builder()
                    .doctorId(2L)
                    .startTime(LocalTime.of(14, 0))
                    .endTime(LocalTime.of(22, 0))
                    .room("Room-102")
                    .build();
            shiftRepository.save(shift1);
            shiftRepository.save(shift2);

            // When/Then
            mockMvc.perform(get("/api/v1/shifts"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(2));
        }

        @Test
        @DisplayName("GET /api/v1/shifts?doctorId={id} returns shifts by doctor")
        void shouldReturnShiftsByDoctorId() throws Exception {
            // Given
            Shift shift1 = Shift.builder()
                    .doctorId(1L)
                    .startTime(LocalTime.of(9, 0))
                    .endTime(LocalTime.of(17, 0))
                    .room("Room-101")
                    .build();
            Shift shift2 = Shift.builder()
                    .doctorId(2L)
                    .startTime(LocalTime.of(14, 0))
                    .endTime(LocalTime.of(22, 0))
                    .room("Room-102")
                    .build();
            shiftRepository.save(shift1);
            shiftRepository.save(shift2);

            // When/Then
            mockMvc.perform(get("/api/v1/shifts")
                    .param("doctorId", "1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(1))
                    .andExpect(jsonPath("$[0].doctorId").value(1));
        }
    }

    @Nested
    @DisplayName("Shift Update Tests")
    class ShiftUpdateTests {

        @Test
        @DisplayName("PUT /api/v1/shifts/{id} updates shift successfully")
        void shouldUpdateShiftSuccessfully() throws Exception {
            // Given
            Shift shift = Shift.builder()
                    .doctorId(1L)
                    .startTime(LocalTime.of(9, 0))
                    .endTime(LocalTime.of(17, 0))
                    .room("Room-101")
                    .build();
            Shift saved = shiftRepository.save(shift);

            ShiftRequest updateRequest = ShiftRequest.builder()
                    .doctorId(1L)
                    .startTime(LocalTime.of(8, 0))
                    .endTime(LocalTime.of(16, 0))
                    .room("Room-102")
                    .build();

            // When/Then
            mockMvc.perform(put("/api/v1/shifts/{id}", saved.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(updateRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.room").value("Room-102"))
                    .andExpect(jsonPath("$.startTime").value("08:00:00"));
        }

        @Test
        @DisplayName("PUT /api/v1/shifts/{id} fails with invalid time slot")
        void shouldFailUpdateWithInvalidTimeSlot() throws Exception {
            // Given
            Shift shift = Shift.builder()
                    .doctorId(1L)
                    .startTime(LocalTime.of(9, 0))
                    .endTime(LocalTime.of(17, 0))
                    .room("Room-101")
                    .build();
            Shift saved = shiftRepository.save(shift);

            ShiftRequest invalidUpdateRequest = ShiftRequest.builder()
                    .doctorId(1L)
                    .startTime(LocalTime.of(10, 0))  // 10:00
                    .endTime(LocalTime.of(9, 0))     // 09:00 - invalid
                    .room("Room-102")
                    .build();

            // When/Then
            mockMvc.perform(put("/api/v1/shifts/{id}", saved.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(invalidUpdateRequest)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("End time must be strictly after start time"));
        }

        @Test
        @DisplayName("PUT /api/v1/shifts/{id} returns 404 when not found")
        void shouldReturn404WhenUpdatingNonExistentShift() throws Exception {
            // When/Then
            mockMvc.perform(put("/api/v1/shifts/{id}", 999L)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(validShiftRequest)))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("Shift Deletion Tests")
    class ShiftDeletionTests {

        @Test
        @DisplayName("DELETE /api/v1/shifts/{id} deletes shift successfully")
        void shouldDeleteShiftSuccessfully() throws Exception {
            // Given
            Shift shift = Shift.builder()
                    .doctorId(1L)
                    .startTime(LocalTime.of(9, 0))
                    .endTime(LocalTime.of(17, 0))
                    .room("Room-101")
                    .build();
            Shift saved = shiftRepository.save(shift);

            // When/Then
            mockMvc.perform(delete("/api/v1/shifts/{id}", saved.getId()))
                    .andExpect(status().isNoContent());

            assertThat(shiftRepository.findById(saved.getId())).isEmpty();
        }

        @Test
        @DisplayName("DELETE /api/v1/shifts/{id} returns 404 when not found")
        void shouldReturn404WhenDeletingNonExistentShift() throws Exception {
            // When/Then
            mockMvc.perform(delete("/api/v1/shifts/{id}", 999L))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("SCRUM-18: Database Schema Validation Tests")
    class SchemaValidationTests {

        @Test
        @DisplayName("Shifts table is created with correct columns")
        void shouldCreateShiftsTableWithCorrectColumns() throws Exception {
            // Given/When
            Shift shift = Shift.builder()
                    .doctorId(1L)
                    .startTime(LocalTime.of(9, 0))
                    .endTime(LocalTime.of(17, 0))
                    .room("Room-101")
                    .build();
            Shift saved = shiftRepository.save(shift);

            // Then
            assertThat(saved.getId()).isNotNull();
            assertThat(saved.getDoctorId()).isNotNull();
            assertThat(saved.getStartTime()).isNotNull();
            assertThat(saved.getEndTime()).isNotNull();
            assertThat(saved.getRoom()).isNotNull();
            assertThat(saved.getCreatedAt()).isNotNull();
            assertThat(saved.getUpdatedAt()).isNotNull();
        }
    }

    @Nested
    @DisplayName("SCRUM-19: Shift Conflict Validator Integration Tests")
    class ShiftConflictValidatorIntegrationTests {

        @Test
        @DisplayName("POST /api/v1/shifts returns 409 CONFLICT when creating shift that overlaps existing shift")
        void shouldReturn409WhenCreatingConflictingShift() throws Exception {
            // Given - Doctor 1 has an existing shift from 1 PM to 3 PM
            Shift existingShift = Shift.builder()
                    .doctorId(1L)
                    .startTime(LocalTime.of(13, 0))  // 1 PM
                    .endTime(LocalTime.of(15, 0))    // 3 PM
                    .room("Room-101")
                    .build();
            shiftRepository.save(existingShift);

            // New shift request from 2 PM to 4 PM - conflicts with existing
            ShiftRequest conflictingRequest = ShiftRequest.builder()
                    .doctorId(1L)
                    .startTime(LocalTime.of(14, 0))  // 2 PM
                    .endTime(LocalTime.of(16, 0))    // 4 PM
                    .room("Room-102")
                    .build();

            // When/Then
            mockMvc.perform(post("/api/v1/shifts")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(conflictingRequest)))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.status").value(409))
                    .andExpect(jsonPath("$.message").value("Doctor 1 already has a conflicting shift between 13:00 and 15:00"));

            // Verify conflicting shift was NOT created
            assertThat(shiftRepository.count()).isEqualTo(1);
        }

        @Test
        @DisplayName("POST /api/v1/shifts returns 201 CREATED when creating non-conflicting shift")
        void shouldReturn201WhenCreatingNonConflictingShift() throws Exception {
            // Given - Doctor 1 has shift from 9 AM to 12 PM
            Shift existingShift = Shift.builder()
                    .doctorId(1L)
                    .startTime(LocalTime.of(9, 0))
                    .endTime(LocalTime.of(12, 0))
                    .room("Room-101")
                    .build();
            shiftRepository.save(existingShift);

            // New shift from 2 PM to 5 PM - no conflict
            ShiftRequest nonConflictingRequest = ShiftRequest.builder()
                    .doctorId(1L)
                    .startTime(LocalTime.of(14, 0))  // 2 PM
                    .endTime(LocalTime.of(17, 0))    // 5 PM
                    .room("Room-102")
                    .build();

            // When/Then
            mockMvc.perform(post("/api/v1/shifts")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(nonConflictingRequest)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").exists())
                    .andExpect(jsonPath("$.doctorId").value(1))
                    .andExpect(jsonPath("$.startTime").value("14:00:00"))
                    .andExpect(jsonPath("$.endTime").value("17:00:00"));

            // Verify both shifts exist
            assertThat(shiftRepository.count()).isEqualTo(2);
        }

        @Test
        @DisplayName("PUT /api/v1/shifts/{id} returns 409 CONFLICT when updating shift to overlap another shift")
        void shouldReturn409WhenUpdatingShiftToConflict() throws Exception {
            // Given - Doctor 1 has two shifts: 9-12 and 14-17
            Shift shift1 = Shift.builder()
                    .doctorId(1L)
                    .startTime(LocalTime.of(9, 0))
                    .endTime(LocalTime.of(12, 0))
                    .room("Room-101")
                    .build();
            Shift saved1 = shiftRepository.save(shift1);

            Shift shift2 = Shift.builder()
                    .doctorId(1L)
                    .startTime(LocalTime.of(14, 0))
                    .endTime(LocalTime.of(17, 0))
                    .room("Room-102")
                    .build();
            shiftRepository.save(shift2);

            // Try to update shift1 to overlap with shift2 (13:00-16:00)
            ShiftRequest conflictingUpdateRequest = ShiftRequest.builder()
                    .doctorId(1L)
                    .startTime(LocalTime.of(13, 0))
                    .endTime(LocalTime.of(16, 0))
                    .room("Room-101")
                    .build();

            // When/Then
            mockMvc.perform(put("/api/v1/shifts/{id}", saved1.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(conflictingUpdateRequest)))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.status").value(409))
                    .andExpect(jsonPath("$.message").value("Doctor 1 already has a conflicting shift between 14:00 and 17:00"));
        }

        @Test
        @DisplayName("PUT /api/v1/shifts/{id} returns 200 OK when updating shift without conflicts")
        void shouldReturn200WhenUpdatingShiftWithoutConflict() throws Exception {
            // Given - Doctor 1 has two shifts: 9-12 and 14-17
            Shift shift1 = Shift.builder()
                    .doctorId(1L)
                    .startTime(LocalTime.of(9, 0))
                    .endTime(LocalTime.of(12, 0))
                    .room("Room-101")
                    .build();
            Shift saved1 = shiftRepository.save(shift1);

            Shift shift2 = Shift.builder()
                    .doctorId(1L)
                    .startTime(LocalTime.of(14, 0))
                    .endTime(LocalTime.of(17, 0))
                    .room("Room-102")
                    .build();
            shiftRepository.save(shift2);

            // Update shift1 to 8-11 - no conflict with shift2
            ShiftRequest nonConflictingUpdateRequest = ShiftRequest.builder()
                    .doctorId(1L)
                    .startTime(LocalTime.of(8, 0))
                    .endTime(LocalTime.of(11, 0))
                    .room("Room-101")
                    .build();

            // When/Then
            mockMvc.perform(put("/api/v1/shifts/{id}", saved1.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(nonConflictingUpdateRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(saved1.getId()))
                    .andExpect(jsonPath("$.startTime").value("08:00:00"))
                    .andExpect(jsonPath("$.endTime").value("11:00:00"));
        }

        @Test
        @DisplayName("POST /api/v1/shifts returns 201 CREATED when different doctors have overlapping time slots")
        void shouldReturn201WhenDifferentDoctorsHaveOverlappingShifts() throws Exception {
            // Given - Doctor 1 has shift from 1 PM to 3 PM
            Shift doctor1Shift = Shift.builder()
                    .doctorId(1L)
                    .startTime(LocalTime.of(13, 0))
                    .endTime(LocalTime.of(15, 0))
                    .room("Room-101")
                    .build();
            shiftRepository.save(doctor1Shift);

            // Doctor 2 wants shift with same time slot - should be allowed
            ShiftRequest doctor2Request = ShiftRequest.builder()
                    .doctorId(2L)
                    .startTime(LocalTime.of(13, 0))  // Same time
                    .endTime(LocalTime.of(15, 0))    // Same time
                    .room("Room-102")
                    .build();

            // When/Then
            mockMvc.perform(post("/api/v1/shifts")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(doctor2Request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.doctorId").value(2));

            // Verify both shifts exist
            assertThat(shiftRepository.count()).isEqualTo(2);
        }

        @Test
        @DisplayName("POST /api/v1/shifts returns 201 CREATED when shifts are adjacent (no overlap)")
        void shouldReturn201WhenShiftsAreAdjacent() throws Exception {
            // Given - Doctor 1 has shift from 9 AM to 12 PM
            Shift existingShift = Shift.builder()
                    .doctorId(1L)
                    .startTime(LocalTime.of(9, 0))
                    .endTime(LocalTime.of(12, 0))
                    .room("Room-101")
                    .build();
            shiftRepository.save(existingShift);

            // New shift starts exactly when existing ends - should be allowed
            ShiftRequest adjacentRequest = ShiftRequest.builder()
                    .doctorId(1L)
                    .startTime(LocalTime.of(12, 0))  // Starts when previous ends
                    .endTime(LocalTime.of(15, 0))
                    .room("Room-102")
                    .build();

            // When/Then
            mockMvc.perform(post("/api/v1/shifts")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(adjacentRequest)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.startTime").value("12:00:00"));

            // Verify both shifts exist
            assertThat(shiftRepository.count()).isEqualTo(2);
        }

        @Test
        @DisplayName("POST /api/v1/shifts returns 409 CONFLICT when new shift completely encompasses existing shift")
        void shouldReturn409WhenNewShiftEncompassesExisting() throws Exception {
            // Given - Doctor 1 has shift from 10 AM to 2 PM
            Shift existingShift = Shift.builder()
                    .doctorId(1L)
                    .startTime(LocalTime.of(10, 0))
                    .endTime(LocalTime.of(14, 0))
                    .room("Room-101")
                    .build();
            shiftRepository.save(existingShift);

            // New shift from 9 AM to 5 PM completely encompasses existing
            ShiftRequest encompassingRequest = ShiftRequest.builder()
                    .doctorId(1L)
                    .startTime(LocalTime.of(9, 0))
                    .endTime(LocalTime.of(17, 0))
                    .room("Room-102")
                    .build();

            // When/Then
            mockMvc.perform(post("/api/v1/shifts")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(encompassingRequest)))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.status").value(409));

            // Verify only original shift exists
            assertThat(shiftRepository.count()).isEqualTo(1);
        }
    }
}
