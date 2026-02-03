package com.sparks.patient.exception;

/**
 * Exception thrown when an invoice already exists for an appointment
 */
public class InvoiceAlreadyExistsException extends RuntimeException {
    
    public InvoiceAlreadyExistsException(String message) {
        super(message);
    }
    
    public InvoiceAlreadyExistsException(String message, Throwable cause) {
        super(message, cause);
    }
}
