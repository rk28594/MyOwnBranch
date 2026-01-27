package com.sparks.patient.exception;

import java.time.LocalTime;

/**
 * Exception thrown when a shift conflicts with an existing shift for the same doctor
 * (i.e., overlapping time slots)
 * 
 * SCRUM-19: Shift Conflict Validator (Service Layer)
 */
public class ShiftConflictException extends RuntimeException {

    public ShiftConflictException(String message) {
        super(message);
    }

    public ShiftConflictException() {
        super("Shift conflicts with an existing shift for this doctor");
    }

    public ShiftConflictException(Long doctorId, LocalTime startTime, LocalTime endTime) {
        super(String.format("Doctor %d already has a conflicting shift between %s and %s", 
                doctorId, startTime, endTime));
    }
}
