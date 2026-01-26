package com.sparks.patient.exception;

/**
 * Exception thrown when a shift has invalid time slot configuration
 * (i.e., endTime is not strictly after startTime)
 * 
 * SCRUM-18: Shift Definition & Time-Slot Logic
 */
public class InvalidTimeSlotException extends RuntimeException {

    public InvalidTimeSlotException(String message) {
        super(message);
    }

    public InvalidTimeSlotException() {
        super("End time must be strictly after start time");
    }
}
