package com.hospital.management.service;

import com.hospital.management.dto.ShiftRequest;
import com.hospital.management.dto.ShiftResponse;
import com.hospital.management.entity.Shift;
import com.hospital.management.exception.InvalidTimeSlotException;
import com.hospital.management.exception.ResourceNotFoundException;
import com.hospital.management.exception.ShiftConflictException;
import com.hospital.management.repository.ShiftRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Shift Service - Stories SCRUM-18 & SCRUM-19
 * 
 * Story SCRUM-18: Shift Definition & Time-Slot Logic
 * AC: endTime must be strictly after startTime
 * 
 * Story SCRUM-19: Shift Conflict Validator (Service Layer)
 * AC: System checks existing shifts for the same doctorId before saving new ones
 * Test Scenario: Given a doctor is busy from 1 PM to 3 PM, When adding a shift at 2 PM, Then the system rejects it
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ShiftService {

    private final ShiftRepository shiftRepository;
    private final DoctorService doctorService;

    /**
     * Create a new shift
     * Validates time slot and checks for conflicts
     * 
     * @param request the shift details
     * @return created shift response
     * @throws InvalidTimeSlotException if endTime is not after startTime
     * @throws ShiftConflictException if shift conflicts with existing shift
     */
    @Transactional
    public ShiftResponse createShift(ShiftRequest request) {
        log.info("Creating new shift for doctor ID: {}", request.getDoctorId());
        
        // Validate doctor exists
        if (!doctorService.existsById(request.getDoctorId())) {
            throw new ResourceNotFoundException("Doctor", "id", request.getDoctorId().toString());
        }
        
        // Validate time slot - Story SCRUM-18
        validateTimeSlot(request);
        
        // Check for conflicts - Story SCRUM-19
        checkForConflicts(request.getDoctorId(), request);
        
        Shift shift = Shift.builder()
                .doctorId(request.getDoctorId())
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .room(request.getRoom())
                .build();
        
        Shift savedShift = shiftRepository.save(shift);
        log.info("Shift created successfully with ID: {}", savedShift.getId());
        
        return mapToResponse(savedShift);
    }

    /**
     * Get shift by ID
     * @param id the shift ID
     * @return shift response
     * @throws ResourceNotFoundException if shift not found
     */
    @Transactional(readOnly = true)
    public ShiftResponse getShiftById(Long id) {
        log.info("Fetching shift with ID: {}", id);
        Shift shift = shiftRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Shift", "id", id.toString()));
        return mapToResponse(shift);
    }

    /**
     * Get all shifts
     * @return list of all shifts
     */
    @Transactional(readOnly = true)
    public List<ShiftResponse> getAllShifts() {
        log.info("Fetching all shifts");
        return shiftRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get shifts by doctor ID
     * @param doctorId the doctor ID
     * @return list of shifts for the doctor
     */
    @Transactional(readOnly = true)
    public List<ShiftResponse> getShiftsByDoctorId(Long doctorId) {
        log.info("Fetching shifts for doctor ID: {}", doctorId);
        return shiftRepository.findByDoctorId(doctorId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Update an existing shift
     * @param id the shift ID
     * @param request the updated shift details
     * @return updated shift response
     */
    @Transactional
    public ShiftResponse updateShift(Long id, ShiftRequest request) {
        log.info("Updating shift with ID: {}", id);
        
        Shift shift = shiftRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Shift", "id", id.toString()));
        
        // Validate doctor exists
        if (!doctorService.existsById(request.getDoctorId())) {
            throw new ResourceNotFoundException("Doctor", "id", request.getDoctorId().toString());
        }
        
        // Validate time slot
        validateTimeSlot(request);
        
        // Check for conflicts (excluding current shift)
        checkForConflictsExcluding(request.getDoctorId(), request, id);
        
        shift.setDoctorId(request.getDoctorId());
        shift.setStartTime(request.getStartTime());
        shift.setEndTime(request.getEndTime());
        shift.setRoom(request.getRoom());
        
        Shift updatedShift = shiftRepository.save(shift);
        log.info("Shift updated successfully with ID: {}", updatedShift.getId());
        
        return mapToResponse(updatedShift);
    }

    /**
     * Delete a shift by ID
     * @param id the shift ID
     */
    @Transactional
    public void deleteShift(Long id) {
        log.info("Deleting shift with ID: {}", id);
        
        if (!shiftRepository.existsById(id)) {
            throw new ResourceNotFoundException("Shift", "id", id.toString());
        }
        
        shiftRepository.deleteById(id);
        log.info("Shift deleted successfully with ID: {}", id);
    }

    /**
     * Validate that endTime is strictly after startTime - Story SCRUM-18
     */
    private void validateTimeSlot(ShiftRequest request) {
        if (request.getEndTime() == null || request.getStartTime() == null 
                || !request.getEndTime().isAfter(request.getStartTime())) {
            log.warn("Invalid time slot: startTime={}, endTime={}", 
                    request.getStartTime(), request.getEndTime());
            throw new InvalidTimeSlotException(
                    String.format("End time (%s) must be strictly after start time (%s)", 
                            request.getEndTime(), request.getStartTime()));
        }
    }

    /**
     * Check for shift conflicts - Story SCRUM-19
     */
    private void checkForConflicts(Long doctorId, ShiftRequest request) {
        List<Shift> conflicts = shiftRepository.findConflictingShifts(
                doctorId, request.getStartTime(), request.getEndTime());
        
        if (!conflicts.isEmpty()) {
            Shift conflictingShift = conflicts.get(0);
            log.warn("Shift conflict detected for doctor {}: existing shift {} to {}", 
                    doctorId, conflictingShift.getStartTime(), conflictingShift.getEndTime());
            throw new ShiftConflictException(doctorId, 
                    conflictingShift.getStartTime(), conflictingShift.getEndTime());
        }
    }

    /**
     * Check for shift conflicts excluding a specific shift (for updates)
     */
    private void checkForConflictsExcluding(Long doctorId, ShiftRequest request, Long excludeShiftId) {
        List<Shift> conflicts = shiftRepository.findConflictingShiftsExcluding(
                doctorId, request.getStartTime(), request.getEndTime(), excludeShiftId);
        
        if (!conflicts.isEmpty()) {
            Shift conflictingShift = conflicts.get(0);
            throw new ShiftConflictException(doctorId, 
                    conflictingShift.getStartTime(), conflictingShift.getEndTime());
        }
    }

    private ShiftResponse mapToResponse(Shift shift) {
        return ShiftResponse.builder()
                .id(shift.getId())
                .doctorId(shift.getDoctorId())
                .startTime(shift.getStartTime())
                .endTime(shift.getEndTime())
                .room(shift.getRoom())
                .createdAt(shift.getCreatedAt())
                .updatedAt(shift.getUpdatedAt())
                .build();
    }
}
