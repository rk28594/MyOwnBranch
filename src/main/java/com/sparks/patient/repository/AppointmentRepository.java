package com.sparks.patient.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.sparks.patient.entity.Appointment;

/**
 * Appointment Repository - SCRUM-23
 */
@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    Optional<Appointment> findByAppointmentId(String appointmentId);
    
    List<Appointment> findByPatientId(Long patientId);
    
    List<Appointment> findByDoctorId(Long doctorId);
    
    List<Appointment> findByPatientIdAndStatus(Long patientId, Appointment.AppointmentStatus status);
    
    List<Appointment> findByDoctorIdAndAppointmentTimeBetween(
        Long doctorId, 
        LocalDateTime startTime, 
        LocalDateTime endTime
    );
}
