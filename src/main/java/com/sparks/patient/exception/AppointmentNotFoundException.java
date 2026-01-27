package com.sparks.patient.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception thrown when a requested appointment is not found
 * 
 * SCRUM-22: Appointment Completion & Status Update
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class AppointmentNotFoundException extends RuntimeException {

    public AppointmentNotFoundException(Long id) {
        super("Appointment not found with ID: " + id);
    }

    public AppointmentNotFoundException(String message) {
        super(message);
    }
}
