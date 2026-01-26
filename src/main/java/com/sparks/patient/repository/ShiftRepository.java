package com.sparks.patient.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.sparks.patient.entity.Shift;

/**
 * Shift Repository - Data access layer for Shift entity
 * 
 * SCRUM-18: Shift Definition & Time-Slot Logic
 */
@Repository
public interface ShiftRepository extends JpaRepository<Shift, Long> {

    /**
     * Find all shifts for a specific doctor
     * @param doctorId the doctor's ID
     * @return List of shifts for the doctor
     */
    List<Shift> findByDoctorId(Long doctorId);

    /**
     * Find all shifts in a specific room
     * @param room the room name
     * @return List of shifts in the room
     */
    List<Shift> findByRoom(String room);

    /**
     * Check if a shift exists for a given doctor
     * @param doctorId the doctor's ID
     * @return true if exists, false otherwise
     */
    boolean existsByDoctorId(Long doctorId);
}
