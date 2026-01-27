package com.sparks.patient.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sparks.patient.dto.AppointmentRequest;
import com.sparks.patient.dto.AppointmentResponse;
import com.sparks.patient.entity.Appointment;
import com.sparks.patient.entity.AppointmentStatus;
import com.sparks.patient.exception.AppointmentNotFoundException;
import com.sparks.patient.mapper.AppointmentMapper;
import com.sparks.patient.repository.AppointmentRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Implementation of AppointmentService
 * Handles all appointment-related business logic
 * 
 * SCRUM-22: Appointment Completion & Status Update
 * Acceptance Criteria:
 * - Updating status to COMPLETED is the trigger for the billing module
 * - When status moves from SCHEDULED to COMPLETED, the system logs the timestamp
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AppointmentServiceImpl implements AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final AppointmentMapper appointmentMapper;

    /**
     * Create a new appointment
     */
    @Override
    public AppointmentResponse createAppointment(AppointmentRequest request) {
        log.info("Creating new appointment for patient ID: {}, doctor ID: {}, shift ID: {}", 
                request.getPatientId(), request.getDoctorId(), request.getShiftId());
        
        Appointment appointment = appointmentMapper.toEntity(request);
        Appointment savedAppointment = appointmentRepository.save(appointment);
        
        log.info("Appointment created successfully with ID: {}", savedAppointment.getId());
        return appointmentMapper.toResponse(savedAppointment);
    }

    /**
     * Get appointment by ID
     */
    @Override
    @Transactional(readOnly = true)
    public AppointmentResponse getAppointmentById(Long id) {
        log.info("Fetching appointment with ID: {}", id);
        
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new AppointmentNotFoundException(id));
        
        return appointmentMapper.toResponse(appointment);
    }

    /**
     * Get all appointments
     */
    @Override
    @Transactional(readOnly = true)
    public List<AppointmentResponse> getAllAppointments() {
        log.info("Fetching all appointments");
        
        return appointmentRepository.findAll().stream()
                .map(appointmentMapper::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get appointments by patient ID
     */
    @Override
    @Transactional(readOnly = true)
    public List<AppointmentResponse> getAppointmentsByPatientId(Long patientId) {
        log.info("Fetching appointments for patient ID: {}", patientId);
        
        return appointmentRepository.findByPatientId(patientId).stream()
                .map(appointmentMapper::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get appointments by doctor ID
     */
    @Override
    @Transactional(readOnly = true)
    public List<AppointmentResponse> getAppointmentsByDoctorId(Long doctorId) {
        log.info("Fetching appointments for doctor ID: {}", doctorId);
        
        return appointmentRepository.findByDoctorId(doctorId).stream()
                .map(appointmentMapper::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get appointments by status
     */
    @Override
    @Transactional(readOnly = true)
    public List<AppointmentResponse> getAppointmentsByStatus(AppointmentStatus status) {
        log.info("Fetching appointments with status: {}", status);
        
        return appointmentRepository.findByStatus(status).stream()
                .map(appointmentMapper::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Update an existing appointment
     */
    @Override
    public AppointmentResponse updateAppointment(Long id, AppointmentRequest request) {
        log.info("Updating appointment with ID: {}", id);
        
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new AppointmentNotFoundException(id));
        
        appointmentMapper.updateEntity(appointment, request);
        Appointment updatedAppointment = appointmentRepository.save(appointment);
        
        log.info("Appointment updated successfully with ID: {}", updatedAppointment.getId());
        return appointmentMapper.toResponse(updatedAppointment);
    }

    /**
     * Mark appointment as completed
     * 
     * SCRUM-22 Acceptance Criteria:
     * - Updating status to COMPLETED is the trigger for the billing module
     * - When status moves from SCHEDULED to COMPLETED, the system logs the timestamp
     */
    @Override
    public AppointmentResponse markAppointmentAsCompleted(Long id) {
        log.info("Marking appointment {} as COMPLETED", id);
        
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new AppointmentNotFoundException(id));
        
        AppointmentStatus previousStatus = appointment.getStatus();
        
        // Mark as completed - this will trigger @PreUpdate which logs the timestamp
        appointment.markAsCompleted();
        Appointment completedAppointment = appointmentRepository.save(appointment);
        
        log.info("SCRUM-22: Appointment {} status changed from {} to COMPLETED at {}. Billing trigger activated.", 
                id, previousStatus, completedAppointment.getCompletedAt());
        
        return appointmentMapper.toResponse(completedAppointment);
    }

    /**
     * Cancel an appointment
     */
    @Override
    public AppointmentResponse cancelAppointment(Long id) {
        log.info("Cancelling appointment with ID: {}", id);
        
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new AppointmentNotFoundException(id));
        
        appointment.setStatus(AppointmentStatus.CANCELLED);
        Appointment cancelledAppointment = appointmentRepository.save(appointment);
        
        log.info("Appointment cancelled successfully with ID: {}", cancelledAppointment.getId());
        return appointmentMapper.toResponse(cancelledAppointment);
    }

    /**
     * Delete an appointment
     */
    @Override
    public void deleteAppointment(Long id) {
        log.info("Deleting appointment with ID: {}", id);
        
        if (!appointmentRepository.existsById(id)) {
            throw new AppointmentNotFoundException(id);
        }
        
        appointmentRepository.deleteById(id);
        log.info("Appointment deleted successfully with ID: {}", id);
    }
}
