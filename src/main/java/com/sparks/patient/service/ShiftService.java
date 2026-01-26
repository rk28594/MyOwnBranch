package com.sparks.patient.service;

import java.util.List;

import com.sparks.patient.dto.ShiftRequest;
import com.sparks.patient.dto.ShiftResponse;

/**
 * Service interface for Shift operations
 * 
 * SCRUM-18: Shift Definition & Time-Slot Logic
 */
public interface ShiftService {

    /**
     * Create a new shift
     * @param request the shift request data
     * @return the created shift response
     */
    ShiftResponse createShift(ShiftRequest request);

    /**
     * Get shift by ID
     * @param id the shift ID
     * @return the shift response
     */
    ShiftResponse getShiftById(Long id);

    /**
     * Get all shifts
     * @return list of all shift responses
     */
    List<ShiftResponse> getAllShifts();

    /**
     * Get shifts by doctor ID
     * @param doctorId the doctor's ID
     * @return list of shifts for the doctor
     */
    List<ShiftResponse> getShiftsByDoctorId(Long doctorId);

    /**
     * Update an existing shift
     * @param id the shift ID
     * @param request the shift update request
     * @return the updated shift response
     */
    ShiftResponse updateShift(Long id, ShiftRequest request);

    /**
     * Delete a shift
     * @param id the shift ID
     */
    void deleteShift(Long id);
}
