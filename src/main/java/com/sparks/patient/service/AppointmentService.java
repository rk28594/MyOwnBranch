package com.sparks.patient.service;

import java.util.List;

import com.sparks.patient.dto.AppointmentRequest;
import com.sparks.patient.dto.AppointmentResponse;
import com.sparks.patient.entity.AppointmentStatus;

/**
 * Service interface for Appointment operations
 * 
 * SCRUM-22: Appointment Completion & Status Update
 */
public interface AppointmentService {

    /**
     * Create a new appointment
     */
    AppointmentResponse createAppointment(AppointmentRequest request);

    /**
     * Get appointment by ID
     */
    AppointmentResponse getAppointmentById(Long id);

    /**
     * Get all appointments
     */
    List<AppointmentResponse> getAllAppointments();

    /**
     * Get appointments by patient ID
     */
    List<AppointmentResponse> getAppointmentsByPatientId(Long patientId);

    /**
     * Get appointments by doctor ID
     */
    List<AppointmentResponse> getAppointmentsByDoctorId(Long doctorId);

    /**
     * Get appointments by status
     */
    List<AppointmentResponse> getAppointmentsByStatus(AppointmentStatus status);

    /**
     * Update an existing appointment
     */
    AppointmentResponse updateAppointment(Long id, AppointmentRequest request);

    /**
     * Mark appointment as completed
     * SCRUM-22 Acceptance Criteria: This triggers the billing module
     */
    AppointmentResponse markAppointmentAsCompleted(Long id);

    /**
     * Cancel an appointment
     */
    AppointmentResponse cancelAppointment(Long id);

    /**
     * Delete an appointment
     */
    void deleteAppointment(Long id);
}
