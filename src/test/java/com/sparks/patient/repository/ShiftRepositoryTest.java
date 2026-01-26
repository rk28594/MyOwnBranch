package com.sparks.patient.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import com.sparks.patient.entity.Shift;

/**
 * Repository Tests for ShiftRepository
 * 
 * SCRUM-18: Shift Definition & Time-Slot Logic
 */
@DataJpaTest
@DisplayName("ShiftRepository Unit Tests")
class ShiftRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private ShiftRepository shiftRepository;

    private Shift shift1;
    private Shift shift2;

    @BeforeEach
    void setUp() {
        shiftRepository.deleteAll();
        
        shift1 = Shift.builder()
                .doctorId(1L)
                .startTime(LocalTime.of(9, 0))
                .endTime(LocalTime.of(17, 0))
                .room("Room-101")
                .build();

        shift2 = Shift.builder()
                .doctorId(1L)
                .startTime(LocalTime.of(18, 0))
                .endTime(LocalTime.of(22, 0))
                .room("Room-102")
                .build();
    }

    @Nested
    @DisplayName("SCRUM-18: Shift Entity Persistence Tests")
    class ShiftEntityPersistenceTests {

        @Test
        @DisplayName("Should save shift with valid time slot")
        void shouldSaveShiftWithValidTimeSlot() {
            // Given/When
            Shift saved = shiftRepository.save(shift1);

            // Then
            assertThat(saved.getId()).isNotNull();
            assertThat(saved.getDoctorId()).isEqualTo(1L);
            assertThat(saved.getStartTime()).isEqualTo(LocalTime.of(9, 0));
            assertThat(saved.getEndTime()).isEqualTo(LocalTime.of(17, 0));
            assertThat(saved.getRoom()).isEqualTo("Room-101");
            assertThat(saved.getCreatedAt()).isNotNull();
            assertThat(saved.getUpdatedAt()).isNotNull();
        }

        @Test
        @DisplayName("Should find shift by ID")
        void shouldFindShiftById() {
            // Given
            Shift saved = entityManager.persistFlushFind(shift1);

            // When
            Optional<Shift> found = shiftRepository.findById(saved.getId());

            // Then
            assertThat(found).isPresent();
            assertThat(found.get().getRoom()).isEqualTo("Room-101");
        }

        @Test
        @DisplayName("Should return empty when shift not found")
        void shouldReturnEmptyWhenShiftNotFound() {
            // When
            Optional<Shift> found = shiftRepository.findById(999L);

            // Then
            assertThat(found).isEmpty();
        }
    }

    @Nested
    @DisplayName("Find By Doctor ID Tests")
    class FindByDoctorIdTests {

        @Test
        @DisplayName("Should find all shifts by doctor ID")
        void shouldFindAllShiftsByDoctorId() {
            // Given
            entityManager.persist(shift1);
            entityManager.persist(shift2);
            entityManager.flush();

            // When
            List<Shift> shifts = shiftRepository.findByDoctorId(1L);

            // Then
            assertThat(shifts).hasSize(2);
            assertThat(shifts).extracting(Shift::getDoctorId).containsOnly(1L);
        }

        @Test
        @DisplayName("Should return empty list when no shifts for doctor")
        void shouldReturnEmptyListWhenNoShiftsForDoctor() {
            // Given
            entityManager.persist(shift1);
            entityManager.flush();

            // When
            List<Shift> shifts = shiftRepository.findByDoctorId(999L);

            // Then
            assertThat(shifts).isEmpty();
        }
    }

    @Nested
    @DisplayName("Find By Room Tests")
    class FindByRoomTests {

        @Test
        @DisplayName("Should find all shifts by room")
        void shouldFindAllShiftsByRoom() {
            // Given
            entityManager.persist(shift1);
            entityManager.persist(shift2);
            entityManager.flush();

            // When
            List<Shift> shifts = shiftRepository.findByRoom("Room-101");

            // Then
            assertThat(shifts).hasSize(1);
            assertThat(shifts.get(0).getRoom()).isEqualTo("Room-101");
        }
    }

    @Nested
    @DisplayName("Exists By Doctor ID Tests")
    class ExistsByDoctorIdTests {

        @Test
        @DisplayName("Should return true when shifts exist for doctor")
        void shouldReturnTrueWhenShiftsExistForDoctor() {
            // Given
            entityManager.persist(shift1);
            entityManager.flush();

            // When
            boolean exists = shiftRepository.existsByDoctorId(1L);

            // Then
            assertThat(exists).isTrue();
        }

        @Test
        @DisplayName("Should return false when no shifts exist for doctor")
        void shouldReturnFalseWhenNoShiftsExistForDoctor() {
            // When
            boolean exists = shiftRepository.existsByDoctorId(999L);

            // Then
            assertThat(exists).isFalse();
        }
    }

    @Nested
    @DisplayName("Delete Tests")
    class DeleteTests {

        @Test
        @DisplayName("Should delete shift by ID")
        void shouldDeleteShiftById() {
            // Given
            Shift saved = entityManager.persistFlushFind(shift1);
            Long id = saved.getId();

            // When
            shiftRepository.deleteById(id);

            // Then
            Optional<Shift> found = shiftRepository.findById(id);
            assertThat(found).isEmpty();
        }
    }
}
