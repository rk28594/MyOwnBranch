package com.sparks.patient.controller;

import java.util.List;

import javax.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.sparks.patient.dto.ErrorResponse;
import com.sparks.patient.dto.ShiftRequest;
import com.sparks.patient.dto.ShiftResponse;
import com.sparks.patient.service.ShiftService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

/**
 * REST Controller for Shift Management API
 * 
 * SCRUM-18: Shift Definition & Time-Slot Logic
 * Acceptance Criteria: endTime must be strictly after startTime
 */
@RestController
@RequestMapping("/api/v1/shifts")
@RequiredArgsConstructor
@Tag(name = "Shift Management", description = "APIs for shift definition and time-slot management")
public class ShiftController {

    private final ShiftService shiftService;

    /**
     * Create a new shift
     * POST /api/v1/shifts returns 201 Created
     */
    @PostMapping
    @Operation(summary = "Create a new shift", description = "Create a new shift for a doctor. EndTime must be strictly after startTime.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Shift created successfully",
                content = @Content(schema = @Schema(implementation = ShiftResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid request data or invalid time slot",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<ShiftResponse> createShift(
            @Valid @RequestBody ShiftRequest request) {
        ShiftResponse response = shiftService.createShift(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Get shift by ID
     * GET /api/v1/shifts/{id}
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get shift by ID", description = "Retrieve shift details by its unique ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Shift found",
                content = @Content(schema = @Schema(implementation = ShiftResponse.class))),
        @ApiResponse(responseCode = "404", description = "Shift not found",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<ShiftResponse> getShiftById(
            @Parameter(description = "Shift ID", required = true)
            @PathVariable Long id) {
        ShiftResponse response = shiftService.getShiftById(id);
        return ResponseEntity.ok(response);
    }

    /**
     * Get all shifts or filter by doctor ID
     * GET /api/v1/shifts
     * GET /api/v1/shifts?doctorId={doctorId}
     */
    @GetMapping
    @Operation(summary = "Get all shifts", description = "Retrieve all shifts or filter by doctor ID")
    @ApiResponse(responseCode = "200", description = "List of shifts retrieved successfully")
    public ResponseEntity<List<ShiftResponse>> getAllShifts(
            @Parameter(description = "Filter by doctor ID")
            @RequestParam(required = false) Long doctorId) {
        List<ShiftResponse> shifts;
        if (doctorId != null) {
            shifts = shiftService.getShiftsByDoctorId(doctorId);
        } else {
            shifts = shiftService.getAllShifts();
        }
        return ResponseEntity.ok(shifts);
    }

    /**
     * Update an existing shift
     * PUT /api/v1/shifts/{id}
     */
    @PutMapping("/{id}")
    @Operation(summary = "Update shift", description = "Update an existing shift's information. EndTime must be strictly after startTime.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Shift updated successfully",
                content = @Content(schema = @Schema(implementation = ShiftResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid request data or invalid time slot",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "Shift not found",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<ShiftResponse> updateShift(
            @Parameter(description = "Shift ID", required = true)
            @PathVariable Long id,
            @Valid @RequestBody ShiftRequest request) {
        ShiftResponse response = shiftService.updateShift(id, request);
        return ResponseEntity.ok(response);
    }

    /**
     * Delete a shift
     * DELETE /api/v1/shifts/{id}
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete shift", description = "Remove a shift from the system")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Shift deleted successfully"),
        @ApiResponse(responseCode = "404", description = "Shift not found",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<Void> deleteShift(
            @Parameter(description = "Shift ID", required = true)
            @PathVariable Long id) {
        shiftService.deleteShift(id);
        return ResponseEntity.noContent().build();
    }
}
