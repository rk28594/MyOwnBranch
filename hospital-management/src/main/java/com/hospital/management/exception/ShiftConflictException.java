package com.hospital.management.exception;

import java.time.LocalDateTime;

/**
 * Exception thrown when a shift conflicts with an existing shift for the same doctor
 * Story SCRUM-19: Shift Conflict Validator
 */
public class ShiftConflictException extends RuntimeException {
    
    private final Long doctorId;
    private final LocalDateTime conflictStart;
    private final LocalDateTime conflictEnd;
    
    public ShiftConflictException(Long doctorId, LocalDateTime conflictStart, LocalDateTime conflictEnd) {
        super(String.format("Shift conflict: Doctor %d already has a shift from %s to %s", 
                doctorId, conflictStart, conflictEnd));
        this.doctorId = doctorId;
        this.conflictStart = conflictStart;
        this.conflictEnd = conflictEnd;
    }
    
    public ShiftConflictException(String message) {
        super(message);
        this.doctorId = null;
        this.conflictStart = null;
        this.conflictEnd = null;
    }
    
    public Long getDoctorId() {
        return doctorId;
    }
    
    public LocalDateTime getConflictStart() {
        return conflictStart;
    }
    
    public LocalDateTime getConflictEnd() {
        return conflictEnd;
    }
}
