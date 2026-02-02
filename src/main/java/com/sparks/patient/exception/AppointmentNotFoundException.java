package com.sparks.patient.exception;

/**
 * Exception thrown when an appointment is not found - SCRUM-23
 */
public class AppointmentNotFoundException extends RuntimeException {
    
    public AppointmentNotFoundException(String message) {
        super(message);
    }
    
    public AppointmentNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
