package com.sparks.patient.exception;

/**
 * Exception thrown when a doctor is not found - SCRUM-23
 */
public class DoctorNotFoundException extends RuntimeException {
    
    public DoctorNotFoundException(String message) {
        super(message);
    }
    
    public DoctorNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
