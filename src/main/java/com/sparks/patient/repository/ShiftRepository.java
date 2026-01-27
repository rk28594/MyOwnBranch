package com.sparks.patient.repository;

import java.time.LocalTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.sparks.patient.entity.Shift;

/**
 * Shift Repository - Data access layer for Shift entity
 * 
 * SCRUM-18: Shift Definition & Time-Slot Logic
 * SCRUM-19: Shift Conflict Validator (Service Layer)
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

    /**
     * SCRUM-19: Find overlapping shifts for a doctor
     * 
     * Two time slots overlap if:
     * - Existing shift starts before new shift ends, AND
     * - Existing shift ends after new shift starts
     * 
     * Example: Doctor busy from 1 PM to 3 PM
     * - Adding shift 2 PM to 4 PM -> Conflict (overlaps from 2 PM to 3 PM)
     * - Adding shift 12 PM to 2 PM -> Conflict (overlaps at exactly 1 PM to 2 PM)
     * - Adding shift 3 PM to 5 PM -> No conflict (starts exactly when other ends)
     * 
     * @param doctorId the doctor's ID
     * @param startTime the new shift's start time
     * @param endTime the new shift's end time
     * @return List of conflicting shifts
     */
    @Query("SELECT s FROM Shift s WHERE s.doctorId = :doctorId " +
           "AND s.startTime < :endTime AND s.endTime > :startTime")
    List<Shift> findConflictingShifts(
            @Param("doctorId") Long doctorId,
            @Param("startTime") LocalTime startTime,
            @Param("endTime") LocalTime endTime);

    /**
     * SCRUM-19: Find overlapping shifts for a doctor, excluding a specific shift (for updates)
     * 
     * @param doctorId the doctor's ID
     * @param startTime the new shift's start time
     * @param endTime the new shift's end time
     * @param excludeShiftId the shift ID to exclude (useful during update operations)
     * @return List of conflicting shifts
     */
    @Query("SELECT s FROM Shift s WHERE s.doctorId = :doctorId " +
           "AND s.startTime < :endTime AND s.endTime > :startTime " +
           "AND s.id <> :excludeShiftId")
    List<Shift> findConflictingShiftsExcluding(
            @Param("doctorId") Long doctorId,
            @Param("startTime") LocalTime startTime,
            @Param("endTime") LocalTime endTime,
            @Param("excludeShiftId") Long excludeShiftId);
}
