package com.hospital.management.controller;

import com.hospital.management.dto.ShiftRequest;
import com.hospital.management.dto.ShiftResponse;
import com.hospital.management.service.ShiftService;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Shift REST Controller - Stories SCRUM-18 & SCRUM-19
 * 
 * Story SCRUM-18: Shift Definition & Time-Slot Logic
 * Fields: doctorId, startTime, endTime, room
 * AC: endTime must be strictly after startTime
 * Test Scenario: When startTime is 10:00 and endTime is 09:00, Then save operation fails with validation error
 * 
 * Story SCRUM-19: Shift Conflict Validator (Service Layer)
 * AC: System checks existing shifts for the same doctorId before saving new ones
 * Test Scenario: Given a doctor is busy from 1 PM to 3 PM, When adding a shift at 2 PM, Then system rejects it
 */
@RestController
@RequestMapping("/api/v1/shifts")
@RequiredArgsConstructor
@Slf4j
public class ShiftController {

    private final ShiftService shiftService;

    /**
     * Create a new shift
     * @param request the shift details
     * @return created shift with 201 status
     */
    @PostMapping
    public ResponseEntity<ShiftResponse> createShift(@Valid @RequestBody ShiftRequest request) {
        log.info("REST request to create shift for doctor: {}", request.getDoctorId());
        ShiftResponse response = shiftService.createShift(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /**
     * Get shift by ID
     * @param id the shift ID
     * @return shift details or 404 if not found
     */
    @GetMapping("/{id}")
    public ResponseEntity<ShiftResponse> getShiftById(@PathVariable Long id) {
        log.info("REST request to get shift by ID: {}", id);
        ShiftResponse response = shiftService.getShiftById(id);
        return ResponseEntity.ok(response);
    }

    /**
     * Get all shifts
     * @return list of all shifts
     */
    @GetMapping
    public ResponseEntity<List<ShiftResponse>> getAllShifts() {
        log.info("REST request to get all shifts");
        List<ShiftResponse> responses = shiftService.getAllShifts();
        return ResponseEntity.ok(responses);
    }

    /**
     * Get shifts by doctor ID
     * @param doctorId the doctor ID
     * @return list of shifts for the doctor
     */
    @GetMapping("/doctor/{doctorId}")
    public ResponseEntity<List<ShiftResponse>> getShiftsByDoctorId(@PathVariable Long doctorId) {
        log.info("REST request to get shifts for doctor ID: {}", doctorId);
        List<ShiftResponse> responses = shiftService.getShiftsByDoctorId(doctorId);
        return ResponseEntity.ok(responses);
    }

    /**
     * Update an existing shift
     * @param id the shift ID
     * @param request the updated shift details
     * @return updated shift details
     */
    @PutMapping("/{id}")
    public ResponseEntity<ShiftResponse> updateShift(
            @PathVariable Long id,
            @Valid @RequestBody ShiftRequest request) {
        log.info("REST request to update shift with ID: {}", id);
        ShiftResponse response = shiftService.updateShift(id, request);
        return ResponseEntity.ok(response);
    }

    /**
     * Delete a shift
     * @param id the shift ID
     * @return 204 No Content on success
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteShift(@PathVariable Long id) {
        log.info("REST request to delete shift with ID: {}", id);
        shiftService.deleteShift(id);
        return ResponseEntity.noContent().build();
    }
}
