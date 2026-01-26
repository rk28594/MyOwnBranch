package com.sparks.patient.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.time.LocalTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.sparks.patient.dto.ShiftRequest;
import com.sparks.patient.dto.ShiftResponse;
import com.sparks.patient.entity.Shift;

/**
 * Unit Tests for ShiftMapper
 * 
 * SCRUM-18: Shift Definition & Time-Slot Logic
 */
@DisplayName("ShiftMapper Unit Tests")
class ShiftMapperTest {

    private ShiftMapper shiftMapper;

    @BeforeEach
    void setUp() {
        shiftMapper = new ShiftMapper();
    }

    @Nested
    @DisplayName("toEntity Tests")
    class ToEntityTests {

        @Test
        @DisplayName("Should convert ShiftRequest to Shift entity")
        void shouldConvertRequestToEntity() {
            // Given
            ShiftRequest request = ShiftRequest.builder()
                    .doctorId(1L)
                    .startTime(LocalTime.of(9, 0))
                    .endTime(LocalTime.of(17, 0))
                    .room("Room-101")
                    .build();

            // When
            Shift result = shiftMapper.toEntity(request);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isNull();
            assertThat(result.getDoctorId()).isEqualTo(1L);
            assertThat(result.getStartTime()).isEqualTo(LocalTime.of(9, 0));
            assertThat(result.getEndTime()).isEqualTo(LocalTime.of(17, 0));
            assertThat(result.getRoom()).isEqualTo("Room-101");
        }

        @Test
        @DisplayName("Should return null when request is null")
        void shouldReturnNullWhenRequestIsNull() {
            // When
            Shift result = shiftMapper.toEntity(null);

            // Then
            assertThat(result).isNull();
        }
    }

    @Nested
    @DisplayName("toResponse Tests")
    class ToResponseTests {

        @Test
        @DisplayName("Should convert Shift entity to ShiftResponse")
        void shouldConvertEntityToResponse() {
            // Given
            LocalDateTime now = LocalDateTime.now();
            Shift shift = Shift.builder()
                    .id(1L)
                    .doctorId(1L)
                    .startTime(LocalTime.of(9, 0))
                    .endTime(LocalTime.of(17, 0))
                    .room("Room-101")
                    .createdAt(now)
                    .updatedAt(now)
                    .build();

            // When
            ShiftResponse result = shiftMapper.toResponse(shift);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(1L);
            assertThat(result.getDoctorId()).isEqualTo(1L);
            assertThat(result.getStartTime()).isEqualTo(LocalTime.of(9, 0));
            assertThat(result.getEndTime()).isEqualTo(LocalTime.of(17, 0));
            assertThat(result.getRoom()).isEqualTo("Room-101");
            assertThat(result.getCreatedAt()).isEqualTo(now);
            assertThat(result.getUpdatedAt()).isEqualTo(now);
        }

        @Test
        @DisplayName("Should return null when entity is null")
        void shouldReturnNullWhenEntityIsNull() {
            // When
            ShiftResponse result = shiftMapper.toResponse(null);

            // Then
            assertThat(result).isNull();
        }
    }

    @Nested
    @DisplayName("updateEntity Tests")
    class UpdateEntityTests {

        @Test
        @DisplayName("Should update Shift entity with request data")
        void shouldUpdateEntityWithRequestData() {
            // Given
            Shift shift = Shift.builder()
                    .id(1L)
                    .doctorId(1L)
                    .startTime(LocalTime.of(9, 0))
                    .endTime(LocalTime.of(17, 0))
                    .room("Room-101")
                    .build();

            ShiftRequest updateRequest = ShiftRequest.builder()
                    .doctorId(2L)
                    .startTime(LocalTime.of(14, 0))
                    .endTime(LocalTime.of(22, 0))
                    .room("Room-102")
                    .build();

            // When
            shiftMapper.updateEntity(shift, updateRequest);

            // Then
            assertThat(shift.getId()).isEqualTo(1L); // ID should not change
            assertThat(shift.getDoctorId()).isEqualTo(2L);
            assertThat(shift.getStartTime()).isEqualTo(LocalTime.of(14, 0));
            assertThat(shift.getEndTime()).isEqualTo(LocalTime.of(22, 0));
            assertThat(shift.getRoom()).isEqualTo("Room-102");
        }

        @Test
        @DisplayName("Should not update when entity is null")
        void shouldNotUpdateWhenEntityIsNull() {
            // Given
            ShiftRequest request = ShiftRequest.builder()
                    .doctorId(1L)
                    .startTime(LocalTime.of(9, 0))
                    .endTime(LocalTime.of(17, 0))
                    .room("Room-101")
                    .build();

            // When/Then - Should not throw exception
            shiftMapper.updateEntity(null, request);
        }

        @Test
        @DisplayName("Should not update when request is null")
        void shouldNotUpdateWhenRequestIsNull() {
            // Given
            Shift shift = Shift.builder()
                    .id(1L)
                    .doctorId(1L)
                    .startTime(LocalTime.of(9, 0))
                    .endTime(LocalTime.of(17, 0))
                    .room("Room-101")
                    .build();

            // When
            shiftMapper.updateEntity(shift, null);

            // Then - Original values should remain
            assertThat(shift.getDoctorId()).isEqualTo(1L);
            assertThat(shift.getRoom()).isEqualTo("Room-101");
        }
    }
}
