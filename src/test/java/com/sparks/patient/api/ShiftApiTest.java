package com.sparks.patient.api;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;

import java.time.LocalTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.test.annotation.DirtiesContext;

import com.sparks.patient.dto.ShiftRequest;
import com.sparks.patient.entity.Shift;
import com.sparks.patient.repository.ShiftRepository;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;

/**
 * API Tests using REST Assured
 * End-to-end testing of the Shift Management REST API
 * 
 * SCRUM-18: Shift Definition & Time-Slot Logic
 * Acceptance Criteria: endTime must be strictly after startTime
 * Test Scenario: When startTime is 10:00 and endTime is 09:00, save operation fails with validation error
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@DisplayName("Shift Management API Tests (REST Assured)")
class ShiftApiTest {

    @LocalServerPort
    private int port;

    @Autowired
    private ShiftRepository shiftRepository;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
        RestAssured.basePath = "/api/v1/shifts";
        shiftRepository.deleteAll();
    }

    @Nested
    @DisplayName("SCRUM-18: Shift Creation API Tests")
    class ShiftCreationApiTests {

        @Test
        @DisplayName("POST /api/v1/shifts - Should create shift with 201 Created (valid time slot)")
        void shouldCreateShiftWithValidTimeSlot() {
            ShiftRequest request = ShiftRequest.builder()
                    .doctorId(1L)
                    .startTime(LocalTime.of(9, 0))
                    .endTime(LocalTime.of(17, 0))
                    .room("Room-101")
                    .build();

            given()
                .contentType(ContentType.JSON)
                .body(request)
            .when()
                .post()
            .then()
                .statusCode(HttpStatus.CREATED.value())
                .body("id", greaterThan(0))
                .body("doctorId", equalTo(1))
                .body("startTime", equalTo("09:00:00"))
                .body("endTime", equalTo("17:00:00"))
                .body("room", equalTo("Room-101"))
                .body("createdAt", notNullValue())
                .body("updatedAt", notNullValue());
        }

        @Test
        @DisplayName("SCRUM-18 Test Scenario: When startTime is 10:00 and endTime is 09:00, save operation fails")
        void shouldReturn400WhenEndTimeBeforeStartTime() {
            // Test Scenario from SCRUM-18: startTime 10:00, endTime 09:00
            ShiftRequest request = ShiftRequest.builder()
                    .doctorId(1L)
                    .startTime(LocalTime.of(10, 0))  // 10:00
                    .endTime(LocalTime.of(9, 0))     // 09:00 - INVALID
                    .room("Room-101")
                    .build();

            given()
                .contentType(ContentType.JSON)
                .body(request)
            .when()
                .post()
            .then()
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .body("status", equalTo(400))
                .body("message", equalTo("End time must be strictly after start time"));
        }

        @Test
        @DisplayName("POST /api/v1/shifts - Should return 400 when startTime equals endTime")
        void shouldReturn400WhenStartTimeEqualsEndTime() {
            ShiftRequest request = ShiftRequest.builder()
                    .doctorId(1L)
                    .startTime(LocalTime.of(10, 0))
                    .endTime(LocalTime.of(10, 0))  // Same as startTime
                    .room("Room-101")
                    .build();

            given()
                .contentType(ContentType.JSON)
                .body(request)
            .when()
                .post()
            .then()
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .body("status", equalTo(400));
        }

        @Test
        @DisplayName("POST /api/v1/shifts - Should return 400 for missing required fields")
        void shouldReturn400ForMissingRequiredFields() {
            ShiftRequest request = ShiftRequest.builder()
                    .doctorId(1L)
                    // Missing startTime, endTime, room
                    .build();

            given()
                .contentType(ContentType.JSON)
                .body(request)
            .when()
                .post()
            .then()
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .body("status", equalTo(400))
                .body("message", equalTo("Validation failed"))
                .body("errors.startTime", notNullValue())
                .body("errors.endTime", notNullValue())
                .body("errors.room", notNullValue());
        }

        @Test
        @DisplayName("POST /api/v1/shifts - Should return 400 when doctorId is null")
        void shouldReturn400WhenDoctorIdIsNull() {
            ShiftRequest request = ShiftRequest.builder()
                    .startTime(LocalTime.of(9, 0))
                    .endTime(LocalTime.of(17, 0))
                    .room("Room-101")
                    // Missing doctorId
                    .build();

            given()
                .contentType(ContentType.JSON)
                .body(request)
            .when()
                .post()
            .then()
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .body("errors.doctorId", notNullValue());
        }
    }

    @Nested
    @DisplayName("Shift Retrieval API Tests")
    class ShiftRetrievalApiTests {

        @Test
        @DisplayName("GET /api/v1/shifts/{id} - Should return shift when exists")
        void shouldReturnShiftWhenExists() {
            // Given
            Shift shift = createAndSaveShift(1L, LocalTime.of(9, 0), LocalTime.of(17, 0), "Room-101");

            given()
            .when()
                .get("/{id}", shift.getId())
            .then()
                .statusCode(HttpStatus.OK.value())
                .body("id", equalTo(shift.getId().intValue()))
                .body("doctorId", equalTo(1))
                .body("room", equalTo("Room-101"));
        }

        @Test
        @DisplayName("GET /api/v1/shifts/{id} - Should return 404 when not found")
        void shouldReturn404WhenShiftNotFound() {
            given()
            .when()
                .get("/{id}", 999)
            .then()
                .statusCode(HttpStatus.NOT_FOUND.value())
                .body("status", equalTo(404))
                .body("message", equalTo("Shift not found with ID: 999"));
        }

        @Test
        @DisplayName("GET /api/v1/shifts - Should return all shifts")
        void shouldReturnAllShifts() {
            // Given
            createAndSaveShift(1L, LocalTime.of(9, 0), LocalTime.of(17, 0), "Room-101");
            createAndSaveShift(2L, LocalTime.of(14, 0), LocalTime.of(22, 0), "Room-102");

            given()
            .when()
                .get()
            .then()
                .statusCode(HttpStatus.OK.value())
                .body("$", hasSize(2));
        }

        @Test
        @DisplayName("GET /api/v1/shifts - Should return empty array when no shifts exist")
        void shouldReturnEmptyArrayWhenNoShifts() {
            given()
            .when()
                .get()
            .then()
                .statusCode(HttpStatus.OK.value())
                .body("$", hasSize(0));
        }

        @Test
        @DisplayName("GET /api/v1/shifts?doctorId={id} - Should filter shifts by doctor ID")
        void shouldFilterShiftsByDoctorId() {
            // Given
            createAndSaveShift(1L, LocalTime.of(9, 0), LocalTime.of(17, 0), "Room-101");
            createAndSaveShift(2L, LocalTime.of(14, 0), LocalTime.of(22, 0), "Room-102");
            createAndSaveShift(1L, LocalTime.of(18, 0), LocalTime.of(22, 0), "Room-103");

            given()
                .queryParam("doctorId", 1)
            .when()
                .get()
            .then()
                .statusCode(HttpStatus.OK.value())
                .body("$", hasSize(2))
                .body("[0].doctorId", equalTo(1))
                .body("[1].doctorId", equalTo(1));
        }
    }

    @Nested
    @DisplayName("Shift Update API Tests")
    class ShiftUpdateApiTests {

        @Test
        @DisplayName("PUT /api/v1/shifts/{id} - Should update shift successfully")
        void shouldUpdateShiftSuccessfully() {
            // Given
            Shift shift = createAndSaveShift(1L, LocalTime.of(9, 0), LocalTime.of(17, 0), "Room-101");

            ShiftRequest updateRequest = ShiftRequest.builder()
                    .doctorId(1L)
                    .startTime(LocalTime.of(8, 0))
                    .endTime(LocalTime.of(16, 0))
                    .room("Room-102")
                    .build();

            given()
                .contentType(ContentType.JSON)
                .body(updateRequest)
            .when()
                .put("/{id}", shift.getId())
            .then()
                .statusCode(HttpStatus.OK.value())
                .body("id", equalTo(shift.getId().intValue()))
                .body("room", equalTo("Room-102"))
                .body("startTime", equalTo("08:00:00"));
        }

        @Test
        @DisplayName("PUT /api/v1/shifts/{id} - Should return 400 when update has invalid time slot")
        void shouldReturn400WhenUpdateHasInvalidTimeSlot() {
            // Given
            Shift shift = createAndSaveShift(1L, LocalTime.of(9, 0), LocalTime.of(17, 0), "Room-101");

            ShiftRequest invalidUpdateRequest = ShiftRequest.builder()
                    .doctorId(1L)
                    .startTime(LocalTime.of(10, 0))  // 10:00
                    .endTime(LocalTime.of(9, 0))     // 09:00 - invalid
                    .room("Room-102")
                    .build();

            given()
                .contentType(ContentType.JSON)
                .body(invalidUpdateRequest)
            .when()
                .put("/{id}", shift.getId())
            .then()
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .body("message", equalTo("End time must be strictly after start time"));
        }

        @Test
        @DisplayName("PUT /api/v1/shifts/{id} - Should return 404 when updating non-existent shift")
        void shouldReturn404WhenUpdatingNonExistentShift() {
            ShiftRequest updateRequest = ShiftRequest.builder()
                    .doctorId(1L)
                    .startTime(LocalTime.of(9, 0))
                    .endTime(LocalTime.of(17, 0))
                    .room("Room-101")
                    .build();

            given()
                .contentType(ContentType.JSON)
                .body(updateRequest)
            .when()
                .put("/{id}", 999)
            .then()
                .statusCode(HttpStatus.NOT_FOUND.value());
        }
    }

    @Nested
    @DisplayName("Shift Deletion API Tests")
    class ShiftDeletionApiTests {

        @Test
        @DisplayName("DELETE /api/v1/shifts/{id} - Should delete shift with 204 No Content")
        void shouldDeleteShiftSuccessfully() {
            // Given
            Shift shift = createAndSaveShift(1L, LocalTime.of(9, 0), LocalTime.of(17, 0), "Room-101");

            given()
            .when()
                .delete("/{id}", shift.getId())
            .then()
                .statusCode(HttpStatus.NO_CONTENT.value());

            // Verify deletion
            given()
            .when()
                .get("/{id}", shift.getId())
            .then()
                .statusCode(HttpStatus.NOT_FOUND.value());
        }

        @Test
        @DisplayName("DELETE /api/v1/shifts/{id} - Should return 404 when deleting non-existent shift")
        void shouldReturn404WhenDeletingNonExistentShift() {
            given()
            .when()
                .delete("/{id}", 999)
            .then()
                .statusCode(HttpStatus.NOT_FOUND.value());
        }
    }

    @Nested
    @DisplayName("Edge Cases and Boundary Tests")
    class EdgeCaseTests {

        @Test
        @DisplayName("Should create shift with minimum valid time difference (1 minute)")
        void shouldCreateShiftWithMinimumTimeDifference() {
            ShiftRequest request = ShiftRequest.builder()
                    .doctorId(1L)
                    .startTime(LocalTime.of(9, 0))
                    .endTime(LocalTime.of(9, 1))  // Just 1 minute later
                    .room("Room-101")
                    .build();

            given()
                .contentType(ContentType.JSON)
                .body(request)
            .when()
                .post()
            .then()
                .statusCode(HttpStatus.CREATED.value())
                .body("id", greaterThan(0));
        }

        @Test
        @DisplayName("Should create shift spanning midnight boundary")
        void shouldCreateShiftSpanningMidnight() {
            // This tests if endTime (23:59) is after startTime (00:00)
            ShiftRequest request = ShiftRequest.builder()
                    .doctorId(1L)
                    .startTime(LocalTime.of(0, 0))
                    .endTime(LocalTime.of(23, 59))
                    .room("Room-101")
                    .build();

            given()
                .contentType(ContentType.JSON)
                .body(request)
            .when()
                .post()
            .then()
                .statusCode(HttpStatus.CREATED.value());
        }

        @Test
        @DisplayName("Should validate room length constraint")
        void shouldValidateRoomLengthConstraint() {
            ShiftRequest request = ShiftRequest.builder()
                    .doctorId(1L)
                    .startTime(LocalTime.of(9, 0))
                    .endTime(LocalTime.of(17, 0))
                    .room("A".repeat(51))  // Exceeds 50 char limit
                    .build();

            given()
                .contentType(ContentType.JSON)
                .body(request)
            .when()
                .post()
            .then()
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .body("errors.room", notNullValue());
        }
    }

    // Helper method to create and save a shift
    private Shift createAndSaveShift(Long doctorId, LocalTime startTime, LocalTime endTime, String room) {
        Shift shift = Shift.builder()
                .doctorId(doctorId)
                .startTime(startTime)
                .endTime(endTime)
                .room(room)
                .build();
        return shiftRepository.save(shift);
    }
}
