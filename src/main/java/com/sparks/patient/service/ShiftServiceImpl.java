package com.sparks.patient.service;

import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sparks.patient.dto.ShiftRequest;
import com.sparks.patient.dto.ShiftResponse;
import com.sparks.patient.entity.Shift;
import com.sparks.patient.exception.InvalidTimeSlotException;
import com.sparks.patient.exception.ShiftConflictException;
import com.sparks.patient.exception.ShiftNotFoundException;
import com.sparks.patient.mapper.ShiftMapper;
import com.sparks.patient.repository.ShiftRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Implementation of ShiftService
 * Handles all shift-related business logic
 * 
 * SCRUM-18: Shift Definition & Time-Slot Logic
 * Acceptance Criteria: endTime must be strictly after startTime
 * 
 * SCRUM-19: Shift Conflict Validator (Service Layer)
 * Acceptance Criteria: System checks existing shifts for the same doctorId before saving new ones
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ShiftServiceImpl implements ShiftService {

    private final ShiftRepository shiftRepository;
    private final ShiftMapper shiftMapper;

    /**
     * Create a new shift
     * Validates that endTime is strictly after startTime (SCRUM-18)
     * Validates that there are no conflicting shifts for the same doctor (SCRUM-19)
     */
    @Override
    public ShiftResponse createShift(ShiftRequest request) {
        log.info("Creating new shift for doctor ID: {} in room: {}", request.getDoctorId(), request.getRoom());
        
        Shift shift = shiftMapper.toEntity(request);
        
        // Validate time slot - SCRUM-18 Acceptance Criteria
        if (!shift.isValidTimeSlot()) {
            log.warn("Invalid time slot: startTime={}, endTime={}", request.getStartTime(), request.getEndTime());
            throw new InvalidTimeSlotException();
        }
        
        // SCRUM-19: Check for conflicting shifts for the same doctor
        validateNoConflictingShifts(request.getDoctorId(), request.getStartTime(), request.getEndTime(), null);
        
        Shift savedShift = shiftRepository.save(shift);
        
        log.info("Shift created successfully with ID: {}", savedShift.getId());
        return shiftMapper.toResponse(savedShift);
    }

    /**
     * Get shift by ID
     */
    @Override
    @Transactional(readOnly = true)
    public ShiftResponse getShiftById(Long id) {
        log.info("Fetching shift with ID: {}", id);
        
        Shift shift = shiftRepository.findById(id)
                .orElseThrow(() -> new ShiftNotFoundException(id));
        
        return shiftMapper.toResponse(shift);
    }

    /**
     * Get all shifts
     */
    @Override
    @Transactional(readOnly = true)
    public List<ShiftResponse> getAllShifts() {
        log.info("Fetching all shifts");
        
        return shiftRepository.findAll().stream()
                .map(shiftMapper::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get shifts by doctor ID
     */
    @Override
    @Transactional(readOnly = true)
    public List<ShiftResponse> getShiftsByDoctorId(Long doctorId) {
        log.info("Fetching shifts for doctor ID: {}", doctorId);
        
        return shiftRepository.findByDoctorId(doctorId).stream()
                .map(shiftMapper::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Update an existing shift
     * Validates that endTime is strictly after startTime (SCRUM-18)
     * Validates that there are no conflicting shifts for the same doctor (SCRUM-19)
     */
    @Override
    public ShiftResponse updateShift(Long id, ShiftRequest request) {
        log.info("Updating shift with ID: {}", id);
        
        Shift shift = shiftRepository.findById(id)
                .orElseThrow(() -> new ShiftNotFoundException(id));
        
        shiftMapper.updateEntity(shift, request);
        
        // Validate time slot - SCRUM-18 Acceptance Criteria
        if (!shift.isValidTimeSlot()) {
            log.warn("Invalid time slot: startTime={}, endTime={}", request.getStartTime(), request.getEndTime());
            throw new InvalidTimeSlotException();
        }
        
        // SCRUM-19: Check for conflicting shifts for the same doctor (excluding current shift)
        validateNoConflictingShifts(request.getDoctorId(), request.getStartTime(), request.getEndTime(), id);
        
        Shift updatedShift = shiftRepository.save(shift);
        
        log.info("Shift updated successfully with ID: {}", updatedShift.getId());
        return shiftMapper.toResponse(updatedShift);
    }

    /**
     * Delete a shift
     */
    @Override
    public void deleteShift(Long id) {
        log.info("Deleting shift with ID: {}", id);
        
        if (!shiftRepository.existsById(id)) {
            throw new ShiftNotFoundException(id);
        }
        
        shiftRepository.deleteById(id);
        log.info("Shift deleted successfully with ID: {}", id);
    }

    /**
     * SCRUM-19: Validate that there are no conflicting shifts for the same doctor
     * 
     * Two time slots conflict if they overlap:
     * - Existing shift starts before new shift ends, AND
     * - Existing shift ends after new shift starts
     * 
     * @param doctorId the doctor's ID
     * @param startTime the new shift's start time
     * @param endTime the new shift's end time
     * @param excludeShiftId the shift ID to exclude (null for new shifts, shift ID for updates)
     * @throws ShiftConflictException if conflicting shifts exist
     */
    private void validateNoConflictingShifts(Long doctorId, LocalTime startTime, LocalTime endTime, Long excludeShiftId) {
        log.debug("Checking for conflicting shifts for doctor ID: {} between {} and {}", 
                doctorId, startTime, endTime);
        
        List<Shift> conflictingShifts;
        
        if (excludeShiftId != null) {
            // For updates, exclude the current shift from conflict check
            conflictingShifts = shiftRepository.findConflictingShiftsExcluding(
                    doctorId, startTime, endTime, excludeShiftId);
        } else {
            // For new shifts, check all existing shifts
            conflictingShifts = shiftRepository.findConflictingShifts(doctorId, startTime, endTime);
        }
        
        if (!conflictingShifts.isEmpty()) {
            Shift firstConflict = conflictingShifts.get(0);
            log.warn("SCRUM-19: Shift conflict detected for doctor ID: {}. Conflicting shift: {} - {}", 
                    doctorId, firstConflict.getStartTime(), firstConflict.getEndTime());
            throw new ShiftConflictException(doctorId, firstConflict.getStartTime(), firstConflict.getEndTime());
        }
        
        log.debug("No conflicting shifts found for doctor ID: {}", doctorId);
    }
}
