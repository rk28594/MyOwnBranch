package com.sparks.patient.mapper;

import org.springframework.stereotype.Component;

import com.sparks.patient.dto.AppointmentRequest;
import com.sparks.patient.dto.AppointmentResponse;
import com.sparks.patient.entity.Appointment;
import com.sparks.patient.entity.AppointmentStatus;

/**
 * Mapper for converting between Appointment entity and DTOs
 * 
 * SCRUM-22: Appointment Completion & Status Update
 */
@Component
public class AppointmentMapper {

    /**
     * Convert AppointmentRequest DTO to Appointment entity
     */
    public Appointment toEntity(AppointmentRequest request) {
        if (request == null) {
            return null;
        }
        
        return Appointment.builder()
                .patientId(request.getPatientId())
                .doctorId(request.getDoctorId())
                .shiftId(request.getShiftId())
                .scheduledAt(request.getScheduledAt())
                .status(AppointmentStatus.SCHEDULED)
                .build();
    }

    /**
     * Convert Appointment entity to AppointmentResponse DTO
     */
    public AppointmentResponse toResponse(Appointment appointment) {
        if (appointment == null) {
            return null;
        }
        
        return AppointmentResponse.builder()
                .id(appointment.getId())
                .patientId(appointment.getPatientId())
                .doctorId(appointment.getDoctorId())
                .shiftId(appointment.getShiftId())
                .status(appointment.getStatus())
                .scheduledAt(appointment.getScheduledAt())
                .completedAt(appointment.getCompletedAt())
                .createdAt(appointment.getCreatedAt())
                .updatedAt(appointment.getUpdatedAt())
                .build();
    }

    /**
     * Update existing Appointment entity with data from AppointmentRequest
     */
    public void updateEntity(Appointment appointment, AppointmentRequest request) {
        if (appointment == null || request == null) {
            return;
        }
        
        appointment.setPatientId(request.getPatientId());
        appointment.setDoctorId(request.getDoctorId());
        appointment.setShiftId(request.getShiftId());
        if (request.getScheduledAt() != null) {
            appointment.setScheduledAt(request.getScheduledAt());
        }
    }
}
