package com.sparks.patient.exception;

/**
 * Exception thrown when a requested shift is not found
 * 
 * SCRUM-18: Shift Definition & Time-Slot Logic
 */
public class ShiftNotFoundException extends RuntimeException {

    public ShiftNotFoundException(Long id) {
        super("Shift not found with ID: " + id);
    }

    public ShiftNotFoundException(String message) {
        super(message);
    }
}
