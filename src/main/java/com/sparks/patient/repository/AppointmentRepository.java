package com.sparks.patient.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.sparks.patient.entity.Appointment;
import com.sparks.patient.entity.AppointmentStatus;

/**
 * Repository interface for Appointment entity
 * 
 * SCRUM-22: Appointment Completion & Status Update
 */
@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    /**
     * Find all appointments for a specific patient
     */
    List<Appointment> findByPatientId(Long patientId);

    /**
     * Find all appointments for a specific doctor
     */
    List<Appointment> findByDoctorId(Long doctorId);

    /**
     * Find all appointments for a specific shift
     */
    List<Appointment> findByShiftId(Long shiftId);

    /**
     * Find all appointments by status
     */
    List<Appointment> findByStatus(AppointmentStatus status);

    /**
     * Find all appointments for a patient with a specific status
     */
    List<Appointment> findByPatientIdAndStatus(Long patientId, AppointmentStatus status);
}
