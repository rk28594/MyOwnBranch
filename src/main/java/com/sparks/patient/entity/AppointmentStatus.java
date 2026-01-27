package com.sparks.patient.entity;

/**
 * Enumeration for Appointment Status
 * 
 * SCRUM-22: Appointment Completion & Status Update
 * Acceptance Criteria: Updating status to COMPLETED is the trigger for the billing module
 */
public enum AppointmentStatus {
    /**
     * Appointment has been scheduled
     */
    SCHEDULED,
    
    /**
     * Appointment has been completed
     * This status triggers the billing module
     */
    COMPLETED,
    
    /**
     * Appointment has been cancelled
     */
    CANCELLED
}
