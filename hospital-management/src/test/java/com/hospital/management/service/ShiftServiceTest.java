package com.hospital.management.service;

import com.hospital.management.dto.ShiftRequest;
import com.hospital.management.dto.ShiftResponse;
import com.hospital.management.entity.Shift;
import com.hospital.management.exception.InvalidTimeSlotException;
import com.hospital.management.exception.ResourceNotFoundException;
import com.hospital.management.exception.ShiftConflictException;
import com.hospital.management.repository.ShiftRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for ShiftService - Stories SCRUM-18 & SCRUM-19
 * 
 * SCRUM-18: Shift Definition & Time-Slot Logic
 * Test Scenario: When startTime is 10:00 and endTime is 09:00, Then save operation fails with validation error
 * 
 * SCRUM-19: Shift Conflict Validator (Service Layer)
 * Test Scenario: Given a doctor is busy from 1 PM to 3 PM, When adding a shift at 2 PM, Then the system rejects it
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ShiftService Unit Tests - SCRUM-18 & SCRUM-19")
class ShiftServiceTest {

    @Mock
    private ShiftRepository shiftRepository;

    @Mock
    private DoctorService doctorService;

    @InjectMocks
    private ShiftService shiftService;

    private ShiftRequest validRequest;
    private Shift validShift;
    private LocalDateTime baseTime;

    @BeforeEach
    void setUp() {
        baseTime = LocalDateTime.of(2026, 1, 23, 10, 0);
        
        validRequest = ShiftRequest.builder()
                .doctorId(1L)
                .startTime(baseTime)
                .endTime(baseTime.plusHours(2))
                .room("Room 101")
                .build();

        validShift = Shift.builder()
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
    @DisplayName("Create Shift Tests")
    class CreateShiftTests {

        @Test
        @DisplayName("Should create shift successfully with valid data")
        void shouldCreateShiftSuccessfully() {
            // Arrange
            when(doctorService.existsById(1L)).thenReturn(true);
            when(shiftRepository.findConflictingShifts(any(), any(), any())).thenReturn(List.of());
            when(shiftRepository.save(any(Shift.class))).thenReturn(validShift);

            // Act
            ShiftResponse response = shiftService.createShift(validRequest);

            // Assert
            assertThat(response).isNotNull();
            assertThat(response.getId()).isEqualTo(1L);
            assertThat(response.getDoctorId()).isEqualTo(1L);
            assertThat(response.getStartTime()).isEqualTo(baseTime);
            assertThat(response.getEndTime()).isEqualTo(baseTime.plusHours(2));
            
            verify(shiftRepository).save(any(Shift.class));
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when doctor does not exist")
        void shouldThrowExceptionWhenDoctorNotFound() {
            // Arrange
            when(doctorService.existsById(1L)).thenReturn(false);

            // Act & Assert
            assertThatThrownBy(() -> shiftService.createShift(validRequest))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Doctor not found");

            verify(shiftRepository, never()).save(any(Shift.class));
        }

        @Test
        @DisplayName("Should throw InvalidTimeSlotException when endTime is before startTime - SCRUM-18 AC")
        void shouldThrowExceptionWhenEndTimeBeforeStartTime() {
            // Arrange - startTime is 10:00, endTime is 09:00
            ShiftRequest invalidRequest = ShiftRequest.builder()
                    .doctorId(1L)
                    .startTime(LocalDateTime.of(2026, 1, 23, 10, 0))
                    .endTime(LocalDateTime.of(2026, 1, 23, 9, 0))
                    .room("Room 101")
                    .build();

            when(doctorService.existsById(1L)).thenReturn(true);

            // Act & Assert
            assertThatThrownBy(() -> shiftService.createShift(invalidRequest))
                    .isInstanceOf(InvalidTimeSlotException.class)
                    .hasMessageContaining("End time")
                    .hasMessageContaining("must be strictly after start time");

            verify(shiftRepository, never()).save(any(Shift.class));
        }

        @Test
        @DisplayName("Should throw InvalidTimeSlotException when endTime equals startTime - SCRUM-18")
        void shouldThrowExceptionWhenEndTimeEqualsStartTime() {
            // Arrange
            LocalDateTime sameTime = LocalDateTime.of(2026, 1, 23, 10, 0);
            ShiftRequest invalidRequest = ShiftRequest.builder()
                    .doctorId(1L)
                    .startTime(sameTime)
                    .endTime(sameTime)
                    .room("Room 101")
                    .build();

            when(doctorService.existsById(1L)).thenReturn(true);

            // Act & Assert
            assertThatThrownBy(() -> shiftService.createShift(invalidRequest))
                    .isInstanceOf(InvalidTimeSlotException.class);

            verify(shiftRepository, never()).save(any(Shift.class));
        }
    }

    @Nested
    @DisplayName("Shift Conflict Validation Tests - SCRUM-19")
    class ShiftConflictTests {

        @Test
        @DisplayName("Should throw ShiftConflictException when shift overlaps with existing - SCRUM-19 AC")
        void shouldThrowExceptionWhenShiftConflicts() {
            // Arrange - Doctor busy from 1 PM to 3 PM
            LocalDateTime existingStart = LocalDateTime.of(2026, 1, 23, 13, 0);
            LocalDateTime existingEnd = LocalDateTime.of(2026, 1, 23, 15, 0);
            
            Shift existingShift = Shift.builder()
                    .id(1L)
                    .doctorId(1L)
                    .startTime(existingStart)
                    .endTime(existingEnd)
                    .build();

            // New shift at 2 PM (overlaps)
            ShiftRequest conflictingRequest = ShiftRequest.builder()
                    .doctorId(1L)
                    .startTime(LocalDateTime.of(2026, 1, 23, 14, 0))
                    .endTime(LocalDateTime.of(2026, 1, 23, 16, 0))
                    .room("Room 102")
                    .build();

            when(doctorService.existsById(1L)).thenReturn(true);
            when(shiftRepository.findConflictingShifts(eq(1L), any(), any()))
                    .thenReturn(List.of(existingShift));

            // Act & Assert
            assertThatThrownBy(() -> shiftService.createShift(conflictingRequest))
                    .isInstanceOf(ShiftConflictException.class)
                    .hasMessageContaining("Shift conflict")
                    .hasMessageContaining("Doctor 1");

            verify(shiftRepository, never()).save(any(Shift.class));
        }

        @Test
        @DisplayName("Should allow non-overlapping shift for same doctor")
        void shouldAllowNonOverlappingShift() {
            // Arrange - Existing shift 1 PM to 3 PM, new shift 4 PM to 6 PM
            ShiftRequest nonConflictingRequest = ShiftRequest.builder()
                    .doctorId(1L)
                    .startTime(LocalDateTime.of(2026, 1, 23, 16, 0))
                    .endTime(LocalDateTime.of(2026, 1, 23, 18, 0))
                    .room("Room 102")
                    .build();

            Shift savedShift = Shift.builder()
                    .id(2L)
                    .doctorId(1L)
                    .startTime(nonConflictingRequest.getStartTime())
                    .endTime(nonConflictingRequest.getEndTime())
                    .room("Room 102")
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();

            when(doctorService.existsById(1L)).thenReturn(true);
            when(shiftRepository.findConflictingShifts(eq(1L), any(), any())).thenReturn(List.of());
            when(shiftRepository.save(any(Shift.class))).thenReturn(savedShift);

            // Act
            ShiftResponse response = shiftService.createShift(nonConflictingRequest);

            // Assert
            assertThat(response).isNotNull();
            assertThat(response.getId()).isEqualTo(2L);
            verify(shiftRepository).save(any(Shift.class));
        }

        @Test
        @DisplayName("Should detect conflict when new shift starts during existing shift")
        void shouldDetectConflictWhenStartsDuringExisting() {
            // Arrange - Existing 1-3 PM, new 2-4 PM
            Shift existingShift = Shift.builder()
                    .id(1L)
                    .doctorId(1L)
                    .startTime(LocalDateTime.of(2026, 1, 23, 13, 0))
                    .endTime(LocalDateTime.of(2026, 1, 23, 15, 0))
                    .build();

            ShiftRequest request = ShiftRequest.builder()
                    .doctorId(1L)
                    .startTime(LocalDateTime.of(2026, 1, 23, 14, 0))
                    .endTime(LocalDateTime.of(2026, 1, 23, 16, 0))
                    .room("Room 101")
                    .build();

            when(doctorService.existsById(1L)).thenReturn(true);
            when(shiftRepository.findConflictingShifts(eq(1L), any(), any()))
                    .thenReturn(List.of(existingShift));

            // Act & Assert
            assertThatThrownBy(() -> shiftService.createShift(request))
                    .isInstanceOf(ShiftConflictException.class);
        }

        @Test
        @DisplayName("Should detect conflict when new shift ends during existing shift")
        void shouldDetectConflictWhenEndsDuringExisting() {
            // Arrange - Existing 2-4 PM, new 1-3 PM
            Shift existingShift = Shift.builder()
                    .id(1L)
                    .doctorId(1L)
                    .startTime(LocalDateTime.of(2026, 1, 23, 14, 0))
                    .endTime(LocalDateTime.of(2026, 1, 23, 16, 0))
                    .build();

            ShiftRequest request = ShiftRequest.builder()
                    .doctorId(1L)
                    .startTime(LocalDateTime.of(2026, 1, 23, 13, 0))
                    .endTime(LocalDateTime.of(2026, 1, 23, 15, 0))
                    .room("Room 101")
                    .build();

            when(doctorService.existsById(1L)).thenReturn(true);
            when(shiftRepository.findConflictingShifts(eq(1L), any(), any()))
                    .thenReturn(List.of(existingShift));

            // Act & Assert
            assertThatThrownBy(() -> shiftService.createShift(request))
                    .isInstanceOf(ShiftConflictException.class);
        }

        @Test
        @DisplayName("Should detect conflict when new shift completely contains existing shift")
        void shouldDetectConflictWhenNewContainsExisting() {
            // Arrange - Existing 2-3 PM, new 1-4 PM
            Shift existingShift = Shift.builder()
                    .id(1L)
                    .doctorId(1L)
                    .startTime(LocalDateTime.of(2026, 1, 23, 14, 0))
                    .endTime(LocalDateTime.of(2026, 1, 23, 15, 0))
                    .build();

            ShiftRequest request = ShiftRequest.builder()
                    .doctorId(1L)
                    .startTime(LocalDateTime.of(2026, 1, 23, 13, 0))
                    .endTime(LocalDateTime.of(2026, 1, 23, 16, 0))
                    .room("Room 101")
                    .build();

            when(doctorService.existsById(1L)).thenReturn(true);
            when(shiftRepository.findConflictingShifts(eq(1L), any(), any()))
                    .thenReturn(List.of(existingShift));

            // Act & Assert
            assertThatThrownBy(() -> shiftService.createShift(request))
                    .isInstanceOf(ShiftConflictException.class);
        }
    }

    @Nested
    @DisplayName("Get Shift Tests")
    class GetShiftTests {

        @Test
        @DisplayName("Should return shift when ID exists")
        void shouldReturnShiftWhenIdExists() {
            // Arrange
            when(shiftRepository.findById(1L)).thenReturn(Optional.of(validShift));

            // Act
            ShiftResponse response = shiftService.getShiftById(1L);

            // Assert
            assertThat(response).isNotNull();
            assertThat(response.getId()).isEqualTo(1L);
            assertThat(response.getDoctorId()).isEqualTo(1L);
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when ID does not exist")
        void shouldThrowExceptionWhenIdNotFound() {
            // Arrange
            when(shiftRepository.findById(999L)).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> shiftService.getShiftById(999L))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Shift not found with id: '999'");
        }

        @Test
        @DisplayName("Should return shifts for a specific doctor")
        void shouldReturnShiftsForDoctor() {
            // Arrange
            Shift shift2 = Shift.builder()
                    .id(2L)
                    .doctorId(1L)
                    .startTime(baseTime.plusDays(1))
                    .endTime(baseTime.plusDays(1).plusHours(2))
                    .room("Room 102")
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();

            when(shiftRepository.findByDoctorId(1L)).thenReturn(Arrays.asList(validShift, shift2));

            // Act
            List<ShiftResponse> responses = shiftService.getShiftsByDoctorId(1L);

            // Assert
            assertThat(responses).hasSize(2);
            assertThat(responses).allMatch(r -> r.getDoctorId().equals(1L));
        }
    }

    @Nested
    @DisplayName("Update Shift Tests")
    class UpdateShiftTests {

        @Test
        @DisplayName("Should update shift successfully")
        void shouldUpdateShiftSuccessfully() {
            // Arrange
            ShiftRequest updateRequest = ShiftRequest.builder()
                    .doctorId(1L)
                    .startTime(baseTime.plusHours(4))
                    .endTime(baseTime.plusHours(6))
                    .room("Room 201")
                    .build();

            Shift updatedShift = Shift.builder()
                    .id(1L)
                    .doctorId(1L)
                    .startTime(updateRequest.getStartTime())
                    .endTime(updateRequest.getEndTime())
                    .room("Room 201")
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();

            when(shiftRepository.findById(1L)).thenReturn(Optional.of(validShift));
            when(doctorService.existsById(1L)).thenReturn(true);
            when(shiftRepository.findConflictingShiftsExcluding(eq(1L), any(), any(), eq(1L)))
                    .thenReturn(List.of());
            when(shiftRepository.save(any(Shift.class))).thenReturn(updatedShift);

            // Act
            ShiftResponse response = shiftService.updateShift(1L, updateRequest);

            // Assert
            assertThat(response.getRoom()).isEqualTo("Room 201");
            assertThat(response.getStartTime()).isEqualTo(updateRequest.getStartTime());
        }
    }

    @Nested
    @DisplayName("Delete Shift Tests")
    class DeleteShiftTests {

        @Test
        @DisplayName("Should delete shift successfully")
        void shouldDeleteShiftSuccessfully() {
            // Arrange
            when(shiftRepository.existsById(1L)).thenReturn(true);
            doNothing().when(shiftRepository).deleteById(1L);

            // Act
            shiftService.deleteShift(1L);

            // Assert
            verify(shiftRepository).deleteById(1L);
        }

        @Test
        @DisplayName("Should throw exception when deleting non-existent shift")
        void shouldThrowExceptionWhenDeletingNonExistent() {
            // Arrange
            when(shiftRepository.existsById(999L)).thenReturn(false);

            // Act & Assert
            assertThatThrownBy(() -> shiftService.deleteShift(999L))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }
}
