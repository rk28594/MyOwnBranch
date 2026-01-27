package com.sparks.patient.entity;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Appointment Entity - SCRUM-22: Appointment Completion & Status Update
 * 
 * Fields: id, patientId, doctorId, shiftId, status, scheduledAt, completedAt
 * 
 * Acceptance Criteria:
 * - Updating status to COMPLETED is the trigger for the billing module
 * - When status moves from SCHEDULED to COMPLETED, the system logs the timestamp
 */
@Entity
@Table(name = "appointments")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Appointment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Patient ID is required")
    @Column(name = "patient_id", nullable = false)
    private Long patientId;

    @NotNull(message = "Doctor ID is required")
    @Column(name = "doctor_id", nullable = false)
    private Long doctorId;

    @NotNull(message = "Shift ID is required")
    @Column(name = "shift_id", nullable = false)
    private Long shiftId;

    @NotNull(message = "Status is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private AppointmentStatus status = AppointmentStatus.SCHEDULED;

    @NotNull(message = "Scheduled time is required")
    @Column(name = "scheduled_at", nullable = false)
    private LocalDateTime scheduledAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (scheduledAt == null) {
            scheduledAt = LocalDateTime.now();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
        
        // SCRUM-22: Log timestamp when status moves to COMPLETED
        if (status == AppointmentStatus.COMPLETED && completedAt == null) {
            completedAt = LocalDateTime.now();
        }
    }

    /**
     * Mark appointment as completed
     * This triggers the billing module
     */
    public void markAsCompleted() {
        this.status = AppointmentStatus.COMPLETED;
        this.completedAt = LocalDateTime.now();
    }
}
