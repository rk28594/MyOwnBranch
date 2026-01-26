package com.sparks.patient.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sparks.patient.dto.ShiftRequest;
import com.sparks.patient.dto.ShiftResponse;
import com.sparks.patient.entity.Shift;
import com.sparks.patient.exception.InvalidTimeSlotException;
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
     * Validates that endTime is strictly after startTime
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
     * Validates that endTime is strictly after startTime
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
}
