package com.hospital.management.repository;

import com.hospital.management.entity.Shift;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Shift Repository - Stories SCRUM-18 & SCRUM-19
 * Provides CRUD operations for Shift entity with conflict detection
 */
@Repository
public interface ShiftRepository extends JpaRepository<Shift, Long> {
    
    /**
     * Find all shifts for a specific doctor
     * @param doctorId the doctor ID
     * @return list of shifts
     */
    List<Shift> findByDoctorId(Long doctorId);
    
    /**
     * Find overlapping shifts for a doctor - Story SCRUM-19: Shift Conflict Validator
     * Checks if there are any existing shifts that overlap with the given time range
     * 
     * Uses the standard interval overlap formula: two intervals overlap if and only if
     * (start1 < end2 AND end1 > start2). This correctly handles all overlap cases while
     * treating back-to-back shifts as non-conflicting.
     * 
     * @param doctorId the doctor ID
     * @param startTime the start time of the new shift
     * @param endTime the end time of the new shift
     * @return list of conflicting shifts
     */
    @Query("SELECT s FROM Shift s WHERE s.doctorId = :doctorId " +
           "AND (s.startTime < :endTime AND s.endTime > :startTime)")
    List<Shift> findConflictingShifts(
            @Param("doctorId") Long doctorId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime);
    
    /**
     * Find overlapping shifts excluding a specific shift (for updates)
     * Uses the standard interval overlap formula to detect conflicts
     */
    @Query("SELECT s FROM Shift s WHERE s.doctorId = :doctorId " +
           "AND s.id != :excludeShiftId " +
           "AND (s.startTime < :endTime AND s.endTime > :startTime)")
    List<Shift> findConflictingShiftsExcluding(
            @Param("doctorId") Long doctorId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime,
            @Param("excludeShiftId") Long excludeShiftId);
}
