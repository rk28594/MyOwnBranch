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
     * A shift overlaps if:
     * - The new shift starts during an existing shift (existing.startTime <= newStartTime < existing.endTime)
     * - The new shift ends during an existing shift (existing.startTime < newEndTime <= existing.endTime)
     * - The new shift completely contains an existing shift (newStartTime <= existing.startTime AND newEndTime >= existing.endTime)
     * 
     * @param doctorId the doctor ID
     * @param startTime the start time of the new shift
     * @param endTime the end time of the new shift
     * @return list of conflicting shifts
     */
    @Query("SELECT s FROM Shift s WHERE s.doctorId = :doctorId " +
           "AND ((s.startTime <= :startTime AND s.endTime > :startTime) " +
           "OR (s.startTime < :endTime AND s.endTime >= :endTime) " +
           "OR (s.startTime >= :startTime AND s.endTime <= :endTime))")
    List<Shift> findConflictingShifts(
            @Param("doctorId") Long doctorId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime);
    
    /**
     * Find overlapping shifts excluding a specific shift (for updates)
     */
    @Query("SELECT s FROM Shift s WHERE s.doctorId = :doctorId " +
           "AND s.id != :excludeShiftId " +
           "AND ((s.startTime <= :startTime AND s.endTime > :startTime) " +
           "OR (s.startTime < :endTime AND s.endTime >= :endTime) " +
           "OR (s.startTime >= :startTime AND s.endTime <= :endTime))")
    List<Shift> findConflictingShiftsExcluding(
            @Param("doctorId") Long doctorId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime,
            @Param("excludeShiftId") Long excludeShiftId);
}
