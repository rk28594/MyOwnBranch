package com.sparks.patient.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.sparks.patient.dto.ShiftRequest;
import com.sparks.patient.dto.ShiftResponse;
import com.sparks.patient.entity.Shift;
import com.sparks.patient.exception.InvalidTimeSlotException;
import com.sparks.patient.exception.ShiftNotFoundException;
import com.sparks.patient.mapper.ShiftMapper;
import com.sparks.patient.repository.ShiftRepository;

/**
 * Unit Tests for ShiftServiceImpl
 * Tests business logic in isolation using mocks
 * 
 * SCRUM-18: Shift Definition & Time-Slot Logic
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ShiftService Unit Tests")
class ShiftServiceImplTest {

    @Mock
    private ShiftRepository shiftRepository;

    @Mock
    private ShiftMapper shiftMapper;

    @InjectMocks
    private ShiftServiceImpl shiftService;

    private ShiftRequest validShiftRequest;
    private ShiftRequest invalidShiftRequest;
    private Shift validShift;
    private Shift invalidShift;
    private ShiftResponse shiftResponse;

    @BeforeEach
    void setUp() {
        // Valid shift: startTime (09:00) is before endTime (17:00)
        validShiftRequest = ShiftRequest.builder()
                .doctorId(1L)
                .startTime(LocalTime.of(9, 0))
                .endTime(LocalTime.of(17, 0))
                .room("Room-101")
                .build();

        // Invalid shift: startTime (10:00) is after endTime (09:00) - violates SCRUM-18 acceptance criteria
        invalidShiftRequest = ShiftRequest.builder()
                .doctorId(1L)
                .startTime(LocalTime.of(10, 0))
                .endTime(LocalTime.of(9, 0))
                .room("Room-101")
                .build();

        validShift = Shift.builder()
                .id(1L)
                .doctorId(1L)
                .startTime(LocalTime.of(9, 0))
                .endTime(LocalTime.of(17, 0))
                .room("Room-101")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        invalidShift = Shift.builder()
                .id(2L)
                .doctorId(1L)
                .startTime(LocalTime.of(10, 0))
                .endTime(LocalTime.of(9, 0))
                .room("Room-101")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        shiftResponse = ShiftResponse.builder()
                .id(1L)
                .doctorId(1L)
                .startTime(LocalTime.of(9, 0))
                .endTime(LocalTime.of(17, 0))
                .room("Room-101")
                .createdAt(validShift.getCreatedAt())
                .updatedAt(validShift.getUpdatedAt())
                .build();
    }

    @Nested
    @DisplayName("SCRUM-18: Create Shift Tests")
    class CreateShiftTests {

        @Test
        @DisplayName("Should create shift successfully when valid time slot provided")
        void shouldCreateShiftSuccessfully() {
            // Given
            when(shiftMapper.toEntity(validShiftRequest)).thenReturn(validShift);
            when(shiftRepository.save(validShift)).thenReturn(validShift);
            when(shiftMapper.toResponse(validShift)).thenReturn(shiftResponse);

            // When
            ShiftResponse result = shiftService.createShift(validShiftRequest);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(1L);
            assertThat(result.getDoctorId()).isEqualTo(1L);
            assertThat(result.getStartTime()).isEqualTo(LocalTime.of(9, 0));
            assertThat(result.getEndTime()).isEqualTo(LocalTime.of(17, 0));
            assertThat(result.getRoom()).isEqualTo("Room-101");

            verify(shiftMapper).toEntity(validShiftRequest);
            verify(shiftRepository).save(validShift);
            verify(shiftMapper).toResponse(validShift);
        }

        @Test
        @DisplayName("SCRUM-18 Test Scenario: When startTime is 10:00 and endTime is 09:00, save operation fails with validation error")
        void shouldFailWhenEndTimeIsBeforeStartTime() {
            // Given - Test Scenario from SCRUM-18: startTime 10:00, endTime 09:00
            when(shiftMapper.toEntity(invalidShiftRequest)).thenReturn(invalidShift);

            // When/Then - save operation should fail with validation error
            assertThatThrownBy(() -> shiftService.createShift(invalidShiftRequest))
                    .isInstanceOf(InvalidTimeSlotException.class)
                    .hasMessage("End time must be strictly after start time");

            verify(shiftMapper).toEntity(invalidShiftRequest);
            verify(shiftRepository, never()).save(any(Shift.class));
        }

        @Test
        @DisplayName("Should fail when startTime equals endTime")
        void shouldFailWhenStartTimeEqualsEndTime() {
            // Given
            ShiftRequest equalTimeRequest = ShiftRequest.builder()
                    .doctorId(1L)
                    .startTime(LocalTime.of(10, 0))
                    .endTime(LocalTime.of(10, 0))
                    .room("Room-101")
                    .build();

            Shift equalTimeShift = Shift.builder()
                    .doctorId(1L)
                    .startTime(LocalTime.of(10, 0))
                    .endTime(LocalTime.of(10, 0))
                    .room("Room-101")
                    .build();

            when(shiftMapper.toEntity(equalTimeRequest)).thenReturn(equalTimeShift);

            // When/Then
            assertThatThrownBy(() -> shiftService.createShift(equalTimeRequest))
                    .isInstanceOf(InvalidTimeSlotException.class);

            verify(shiftRepository, never()).save(any(Shift.class));
        }
    }

    @Nested
    @DisplayName("Get Shift Tests")
    class GetShiftTests {

        @Test
        @DisplayName("Should return shift when ID exists")
        void shouldReturnShiftWhenIdExists() {
            // Given
            when(shiftRepository.findById(1L)).thenReturn(Optional.of(validShift));
            when(shiftMapper.toResponse(validShift)).thenReturn(shiftResponse);

            // When
            ShiftResponse result = shiftService.getShiftById(1L);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(1L);
            verify(shiftRepository).findById(1L);
        }

        @Test
        @DisplayName("Should throw ShiftNotFoundException when ID does not exist")
        void shouldThrowExceptionWhenIdNotFound() {
            // Given
            when(shiftRepository.findById(999L)).thenReturn(Optional.empty());

            // When/Then
            assertThatThrownBy(() -> shiftService.getShiftById(999L))
                    .isInstanceOf(ShiftNotFoundException.class)
                    .hasMessage("Shift not found with ID: 999");
        }

        @Test
        @DisplayName("Should return all shifts")
        void shouldReturnAllShifts() {
            // Given
            Shift shift2 = Shift.builder()
                    .id(2L)
                    .doctorId(2L)
                    .startTime(LocalTime.of(14, 0))
                    .endTime(LocalTime.of(22, 0))
                    .room("Room-102")
                    .build();

            ShiftResponse response2 = ShiftResponse.builder()
                    .id(2L)
                    .doctorId(2L)
                    .startTime(LocalTime.of(14, 0))
                    .endTime(LocalTime.of(22, 0))
                    .room("Room-102")
                    .build();

            when(shiftRepository.findAll()).thenReturn(Arrays.asList(validShift, shift2));
            when(shiftMapper.toResponse(validShift)).thenReturn(shiftResponse);
            when(shiftMapper.toResponse(shift2)).thenReturn(response2);

            // When
            List<ShiftResponse> results = shiftService.getAllShifts();

            // Then
            assertThat(results).hasSize(2);
        }

        @Test
        @DisplayName("Should return shifts by doctor ID")
        void shouldReturnShiftsByDoctorId() {
            // Given
            when(shiftRepository.findByDoctorId(1L)).thenReturn(Arrays.asList(validShift));
            when(shiftMapper.toResponse(validShift)).thenReturn(shiftResponse);

            // When
            List<ShiftResponse> results = shiftService.getShiftsByDoctorId(1L);

            // Then
            assertThat(results).hasSize(1);
            assertThat(results.get(0).getDoctorId()).isEqualTo(1L);
        }
    }

    @Nested
    @DisplayName("Update Shift Tests")
    class UpdateShiftTests {

        @Test
        @DisplayName("Should update shift successfully when valid time slot provided")
        void shouldUpdateShiftSuccessfully() {
            // Given
            ShiftRequest updateRequest = ShiftRequest.builder()
                    .doctorId(1L)
                    .startTime(LocalTime.of(8, 0))
                    .endTime(LocalTime.of(16, 0))
                    .room("Room-102")
                    .build();

            Shift updatedShift = Shift.builder()
                    .id(1L)
                    .doctorId(1L)
                    .startTime(LocalTime.of(8, 0))
                    .endTime(LocalTime.of(16, 0))
                    .room("Room-102")
                    .createdAt(validShift.getCreatedAt())
                    .updatedAt(LocalDateTime.now())
                    .build();

            ShiftResponse updatedResponse = ShiftResponse.builder()
                    .id(1L)
                    .doctorId(1L)
                    .startTime(LocalTime.of(8, 0))
                    .endTime(LocalTime.of(16, 0))
                    .room("Room-102")
                    .build();

            when(shiftRepository.findById(1L)).thenReturn(Optional.of(validShift));
            when(shiftRepository.save(validShift)).thenReturn(updatedShift);
            when(shiftMapper.toResponse(updatedShift)).thenReturn(updatedResponse);

            // When
            ShiftResponse result = shiftService.updateShift(1L, updateRequest);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getRoom()).isEqualTo("Room-102");

            verify(shiftMapper).updateEntity(validShift, updateRequest);
        }

        @Test
        @DisplayName("Should fail update when endTime is before startTime")
        void shouldFailUpdateWhenInvalidTimeSlot() {
            // Given
            when(shiftRepository.findById(1L)).thenReturn(Optional.of(validShift));
            
            // Simulate mapper updating entity with invalid times
            Shift invalidUpdatedShift = Shift.builder()
                    .id(1L)
                    .doctorId(1L)
                    .startTime(LocalTime.of(10, 0))
                    .endTime(LocalTime.of(9, 0))
                    .room("Room-101")
                    .build();

            // After updateEntity is called, the shift has invalid times
            when(shiftRepository.findById(1L)).thenReturn(Optional.of(invalidUpdatedShift));

            // When/Then
            assertThatThrownBy(() -> shiftService.updateShift(1L, invalidShiftRequest))
                    .isInstanceOf(InvalidTimeSlotException.class);

            verify(shiftRepository, never()).save(any(Shift.class));
        }

        @Test
        @DisplayName("Should throw ShiftNotFoundException when updating non-existent shift")
        void shouldThrowExceptionWhenUpdatingNonExistentShift() {
            // Given
            when(shiftRepository.findById(999L)).thenReturn(Optional.empty());

            // When/Then
            assertThatThrownBy(() -> shiftService.updateShift(999L, validShiftRequest))
                    .isInstanceOf(ShiftNotFoundException.class);

            verify(shiftRepository, never()).save(any(Shift.class));
        }
    }

    @Nested
    @DisplayName("Delete Shift Tests")
    class DeleteShiftTests {

        @Test
        @DisplayName("Should delete shift successfully")
        void shouldDeleteShiftSuccessfully() {
            // Given
            when(shiftRepository.existsById(1L)).thenReturn(true);

            // When
            shiftService.deleteShift(1L);

            // Then
            verify(shiftRepository).deleteById(1L);
        }

        @Test
        @DisplayName("Should throw ShiftNotFoundException when deleting non-existent shift")
        void shouldThrowExceptionWhenDeletingNonExistentShift() {
            // Given
            when(shiftRepository.existsById(999L)).thenReturn(false);

            // When/Then
            assertThatThrownBy(() -> shiftService.deleteShift(999L))
                    .isInstanceOf(ShiftNotFoundException.class);

            verify(shiftRepository, never()).deleteById(any());
        }
    }
}
