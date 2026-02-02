package com.sparks.patient.mapper;

import org.springframework.stereotype.Component;

import com.sparks.patient.dto.AppointmentRequest;
import com.sparks.patient.dto.AppointmentResponse;
import com.sparks.patient.entity.Appointment;
import com.sparks.patient.entity.Doctor;
import com.sparks.patient.entity.Patient;

/**
 * Appointment Mapper - SCRUM-23
 */
@Component
public class AppointmentMapper {

    public Appointment toEntity(AppointmentRequest request, Patient patient, Doctor doctor) {
        return Appointment.builder()
                .patient(patient)
                .doctor(doctor)
                .appointmentTime(request.getAppointmentTime())
                .status(Appointment.AppointmentStatus.SCHEDULED)
                .build();
    }

    public AppointmentResponse toResponse(Appointment appointment) {
        return AppointmentResponse.builder()
                .id(appointment.getId())
                .appointmentId(appointment.getAppointmentId())
                .patientId(appointment.getPatient().getId())
                .patientName(appointment.getPatient().getFirstName() + " " + appointment.getPatient().getLastName())
                .doctorId(appointment.getDoctor().getId())
                .doctorName(appointment.getDoctor().getFullName())
                .appointmentTime(appointment.getAppointmentTime())
                .status(appointment.getStatus().name())
                .createdAt(appointment.getCreatedAt())
                .build();
    }
}
