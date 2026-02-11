package com.sparks.patient.service;

import java.util.List;

import com.sparks.patient.dto.AppointmentRequest;
import com.sparks.patient.dto.AppointmentResponse;

/**
 * Appointment Service Interface - SCRUM-23
 */
public interface AppointmentService {
    
    /**
     * Create a new appointment
     * @param request Appointment request containing patient, doctor, and time
     * @return Appointment response with generated UUID
     */
    AppointmentResponse createAppointment(AppointmentRequest request);
    
    /**
     * Get appointment by UUID
     * @param appointmentId UUID of the appointment
     * @return Appointment response
     */
    AppointmentResponse getAppointmentById(String appointmentId);
    
    /**
     * Get all appointments for a patient
     * @param patientId Patient ID
     * @return List of appointments
     */
    List<AppointmentResponse> getAppointmentsByPatientId(Long patientId);
    
    /**
     * Get all appointments for a doctor
     * @param doctorId Doctor ID
     * @return List of appointments
     */
    List<AppointmentResponse> getAppointmentsByDoctorId(Long doctorId);
    
    /**
     * Get all appointments
     * @return List of all appointments
     */
    List<AppointmentResponse> getAllAppointments();
    
    /**
     * Mark appointment as completed (SCRUM-24: Required for automated billing)
     * @param appointmentId UUID of the appointment
     * @return Updated appointment response
     */
    AppointmentResponse completeAppointment(String appointmentId);
}
