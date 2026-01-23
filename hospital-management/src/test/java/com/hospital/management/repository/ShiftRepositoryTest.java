package com.hospital.management.repository;

import com.hospital.management.entity.Shift;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

/**
 * Integration tests for ShiftRepository - Stories SCRUM-18 & SCRUM-19
 * Tests JPA repository operations with H2 database
 * 
 * SCRUM-18: Shift Definition & Time-Slot Logic
 * SCRUM-19: Shift Conflict Validator
 */
@DataJpaTest
@DisplayName("ShiftRepository Integration Tests - SCRUM-18 & SCRUM-19")
class ShiftRepositoryTest {

    @Autowired
    private ShiftRepository shiftRepository;

    @Autowired
    private TestEntityManager entityManager;

    private Shift testShift;
    private LocalDateTime baseTime;

    @BeforeEach
    void setUp() {
        baseTime = LocalDateTime.of(2026, 1, 23, 10, 0);
        
        testShift = Shift.builder()
                .doctorId(1L)
                .startTime(baseTime)
                .endTime(baseTime.plusHours(2))
                .room("Room 101")
                .build();
    }

    @Nested
    @DisplayName("Save Shift Tests - SCRUM-18")
    class SaveShiftTests {

        @Test
        @DisplayName("Should save shift successfully - Story SCRUM-18")
        void shouldSaveShiftSuccessfully() {
            // Act
            Shift savedShift = shiftRepository.save(testShift);

            // Assert
            assertThat(savedShift).isNotNull();
            assertThat(savedShift.getId()).isNotNull();
            assertThat(savedShift.getDoctorId()).isEqualTo(1L);
            assertThat(savedShift.getStartTime()).isEqualTo(baseTime);
            assertThat(savedShift.getEndTime()).isEqualTo(baseTime.plusHours(2));
            assertThat(savedShift.getRoom()).isEqualTo("Room 101");
        }

        @Test
        @DisplayName("Should auto-generate ID when saving shift")
        void shouldAutoGenerateId() {
            // Act
            Shift savedShift = shiftRepository.save(testShift);
            
            // Assert
            assertThat(savedShift.getId()).isGreaterThan(0L);
        }

        @Test
        @DisplayName("Should set audit timestamps on save")
        void shouldSetAuditTimestamps() {
            // Act
            Shift savedShift = shiftRepository.save(testShift);
            entityManager.flush();
            entityManager.clear();

            // Assert
            Shift foundShift = shiftRepository.findById(savedShift.getId()).orElseThrow();
            assertThat(foundShift.getCreatedAt()).isNotNull();
            assertThat(foundShift.getUpdatedAt()).isNotNull();
        }

        @Test
        @DisplayName("Should validate time slot is stored correctly")
        void shouldStoreTimeSlotCorrectly() {
            // Arrange - Valid time slot
            Shift shift = Shift.builder()
                    .doctorId(1L)
                    .startTime(LocalDateTime.of(2026, 1, 23, 9, 0))
                    .endTime(LocalDateTime.of(2026, 1, 23, 17, 0))
                    .room("Room 101")
                    .build();

            // Act
            Shift savedShift = shiftRepository.save(shift);
            entityManager.flush();
            entityManager.clear();

            // Assert
            Shift foundShift = shiftRepository.findById(savedShift.getId()).orElseThrow();
            assertThat(foundShift.isValidTimeSlot()).isTrue();
            assertThat(foundShift.getEndTime()).isAfter(foundShift.getStartTime());
        }
    }

    @Nested
    @DisplayName("Find Shift Tests")
    class FindShiftTests {

        @Test
        @DisplayName("Should find shift by ID")
        void shouldFindShiftById() {
            // Arrange
            Shift savedShift = shiftRepository.save(testShift);
            entityManager.flush();
            entityManager.clear();

            // Act
            Optional<Shift> foundShift = shiftRepository.findById(savedShift.getId());

            // Assert
            assertThat(foundShift).isPresent();
            assertThat(foundShift.get().getDoctorId()).isEqualTo(1L);
        }

        @Test
        @DisplayName("Should find shifts by doctor ID")
        void shouldFindShiftsByDoctorId() {
            // Arrange
            Shift shift1 = testShift;
            Shift shift2 = Shift.builder()
                    .doctorId(1L)
                    .startTime(baseTime.plusDays(1))
                    .endTime(baseTime.plusDays(1).plusHours(2))
                    .room("Room 102")
                    .build();

            shiftRepository.save(shift1);
            shiftRepository.save(shift2);
            entityManager.flush();
            entityManager.clear();

            // Act
            List<Shift> shifts = shiftRepository.findByDoctorId(1L);

            // Assert
            assertThat(shifts).hasSize(2);
            assertThat(shifts).allMatch(s -> s.getDoctorId().equals(1L));
        }

        @Test
        @DisplayName("Should return empty list when doctor has no shifts")
        void shouldReturnEmptyWhenDoctorHasNoShifts() {
            // Act
            List<Shift> shifts = shiftRepository.findByDoctorId(999L);

            // Assert
            assertThat(shifts).isEmpty();
        }
    }

    @Nested
    @DisplayName("Conflict Detection Tests - SCRUM-19")
    class ConflictDetectionTests {

        @Test
        @DisplayName("Should detect conflict when new shift starts during existing - SCRUM-19")
        void shouldDetectConflictWhenStartsDuringExisting() {
            // Arrange - Existing shift 1 PM to 3 PM
            Shift existingShift = Shift.builder()
                    .doctorId(1L)
                    .startTime(LocalDateTime.of(2026, 1, 23, 13, 0))
                    .endTime(LocalDateTime.of(2026, 1, 23, 15, 0))
                    .room("Room 101")
                    .build();
            shiftRepository.save(existingShift);
            entityManager.flush();
            entityManager.clear();

            // Act - Check for conflict with 2 PM to 4 PM
            List<Shift> conflicts = shiftRepository.findConflictingShifts(
                    1L,
                    LocalDateTime.of(2026, 1, 23, 14, 0),
                    LocalDateTime.of(2026, 1, 23, 16, 0));

            // Assert
            assertThat(conflicts).hasSize(1);
        }

        @Test
        @DisplayName("Should detect conflict when new shift ends during existing - SCRUM-19")
        void shouldDetectConflictWhenEndsDuringExisting() {
            // Arrange - Existing shift 2 PM to 4 PM
            Shift existingShift = Shift.builder()
                    .doctorId(1L)
                    .startTime(LocalDateTime.of(2026, 1, 23, 14, 0))
                    .endTime(LocalDateTime.of(2026, 1, 23, 16, 0))
                    .room("Room 101")
                    .build();
            shiftRepository.save(existingShift);
            entityManager.flush();
            entityManager.clear();

            // Act - Check for conflict with 1 PM to 3 PM
            List<Shift> conflicts = shiftRepository.findConflictingShifts(
                    1L,
                    LocalDateTime.of(2026, 1, 23, 13, 0),
                    LocalDateTime.of(2026, 1, 23, 15, 0));

            // Assert
            assertThat(conflicts).hasSize(1);
        }

        @Test
        @DisplayName("Should detect conflict when new shift contains existing - SCRUM-19")
        void shouldDetectConflictWhenNewContainsExisting() {
            // Arrange - Existing shift 2 PM to 3 PM
            Shift existingShift = Shift.builder()
                    .doctorId(1L)
                    .startTime(LocalDateTime.of(2026, 1, 23, 14, 0))
                    .endTime(LocalDateTime.of(2026, 1, 23, 15, 0))
                    .room("Room 101")
                    .build();
            shiftRepository.save(existingShift);
            entityManager.flush();
            entityManager.clear();

            // Act - Check for conflict with 1 PM to 4 PM (contains existing)
            List<Shift> conflicts = shiftRepository.findConflictingShifts(
                    1L,
                    LocalDateTime.of(2026, 1, 23, 13, 0),
                    LocalDateTime.of(2026, 1, 23, 16, 0));

            // Assert
            assertThat(conflicts).hasSize(1);
        }

        @Test
        @DisplayName("Should detect conflict when new shift is contained by existing - SCRUM-19")
        void shouldDetectConflictWhenExistingContainsNew() {
            // Arrange - Existing shift 1 PM to 4 PM
            Shift existingShift = Shift.builder()
                    .doctorId(1L)
                    .startTime(LocalDateTime.of(2026, 1, 23, 13, 0))
                    .endTime(LocalDateTime.of(2026, 1, 23, 16, 0))
                    .room("Room 101")
                    .build();
            shiftRepository.save(existingShift);
            entityManager.flush();
            entityManager.clear();

            // Act - Check for conflict with 2 PM to 3 PM (within existing)
            List<Shift> conflicts = shiftRepository.findConflictingShifts(
                    1L,
                    LocalDateTime.of(2026, 1, 23, 14, 0),
                    LocalDateTime.of(2026, 1, 23, 15, 0));

            // Assert
            assertThat(conflicts).hasSize(1);
        }

        @Test
        @DisplayName("Should not detect conflict for non-overlapping shifts - SCRUM-19")
        void shouldNotDetectConflictForNonOverlapping() {
            // Arrange - Existing shift 1 PM to 3 PM
            Shift existingShift = Shift.builder()
                    .doctorId(1L)
                    .startTime(LocalDateTime.of(2026, 1, 23, 13, 0))
                    .endTime(LocalDateTime.of(2026, 1, 23, 15, 0))
                    .room("Room 101")
                    .build();
            shiftRepository.save(existingShift);
            entityManager.flush();
            entityManager.clear();

            // Act - Check for conflict with 4 PM to 6 PM (no overlap)
            List<Shift> conflicts = shiftRepository.findConflictingShifts(
                    1L,
                    LocalDateTime.of(2026, 1, 23, 16, 0),
                    LocalDateTime.of(2026, 1, 23, 18, 0));

            // Assert
            assertThat(conflicts).isEmpty();
        }

        @Test
        @DisplayName("Should not detect conflict for different doctors - SCRUM-19")
        void shouldNotDetectConflictForDifferentDoctors() {
            // Arrange - Existing shift for doctor 1
            Shift existingShift = Shift.builder()
                    .doctorId(1L)
                    .startTime(LocalDateTime.of(2026, 1, 23, 13, 0))
                    .endTime(LocalDateTime.of(2026, 1, 23, 15, 0))
                    .room("Room 101")
                    .build();
            shiftRepository.save(existingShift);
            entityManager.flush();
            entityManager.clear();

            // Act - Check for conflict for doctor 2 at same time
            List<Shift> conflicts = shiftRepository.findConflictingShifts(
                    2L, // Different doctor
                    LocalDateTime.of(2026, 1, 23, 13, 0),
                    LocalDateTime.of(2026, 1, 23, 15, 0));

            // Assert
            assertThat(conflicts).isEmpty();
        }

        @Test
        @DisplayName("Should exclude specific shift when checking conflicts")
        void shouldExcludeSpecificShiftWhenCheckingConflicts() {
            // Arrange
            Shift existingShift = Shift.builder()
                    .doctorId(1L)
                    .startTime(LocalDateTime.of(2026, 1, 23, 13, 0))
                    .endTime(LocalDateTime.of(2026, 1, 23, 15, 0))
                    .room("Room 101")
                    .build();
            Shift savedShift = shiftRepository.save(existingShift);
            entityManager.flush();
            entityManager.clear();

            // Act - Check for conflict excluding the same shift (for updates)
            List<Shift> conflicts = shiftRepository.findConflictingShiftsExcluding(
                    1L,
                    LocalDateTime.of(2026, 1, 23, 13, 0),
                    LocalDateTime.of(2026, 1, 23, 15, 0),
                    savedShift.getId());

            // Assert
            assertThat(conflicts).isEmpty();
        }
    }

    @Nested
    @DisplayName("Update Shift Tests")
    class UpdateShiftTests {

        @Test
        @DisplayName("Should update shift time slot")
        void shouldUpdateShiftTimeSlot() {
            // Arrange
            Shift savedShift = shiftRepository.save(testShift);
            entityManager.flush();
            entityManager.clear();

            // Act
            Shift shiftToUpdate = shiftRepository.findById(savedShift.getId()).orElseThrow();
            shiftToUpdate.setStartTime(baseTime.plusHours(4));
            shiftToUpdate.setEndTime(baseTime.plusHours(6));
            shiftRepository.save(shiftToUpdate);
            entityManager.flush();
            entityManager.clear();

            // Assert
            Shift updatedShift = shiftRepository.findById(savedShift.getId()).orElseThrow();
            assertThat(updatedShift.getStartTime()).isEqualTo(baseTime.plusHours(4));
            assertThat(updatedShift.getEndTime()).isEqualTo(baseTime.plusHours(6));
        }
    }

    @Nested
    @DisplayName("Delete Shift Tests")
    class DeleteShiftTests {

        @Test
        @DisplayName("Should delete shift by ID")
        void shouldDeleteShiftById() {
            // Arrange
            Shift savedShift = shiftRepository.save(testShift);
            entityManager.flush();
            Long shiftId = savedShift.getId();

            // Act
            shiftRepository.deleteById(shiftId);
            entityManager.flush();
            entityManager.clear();

            // Assert
            Optional<Shift> deletedShift = shiftRepository.findById(shiftId);
            assertThat(deletedShift).isEmpty();
        }
    }
}
