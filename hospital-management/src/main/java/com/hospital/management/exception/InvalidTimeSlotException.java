package com.hospital.management.exception;

/**
 * Exception thrown when a shift time slot is invalid (endTime not after startTime)
 * Story SCRUM-18: Shift Definition & Time-Slot Logic
 */
public class InvalidTimeSlotException extends RuntimeException {
    
    public InvalidTimeSlotException(String message) {
        super(message);
    }
    
    public InvalidTimeSlotException() {
        super("End time must be strictly after start time");
    }
}
