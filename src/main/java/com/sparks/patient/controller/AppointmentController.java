package com.sparks.patient.controller;

import java.util.List;

import javax.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.sparks.patient.dto.AppointmentRequest;
import com.sparks.patient.dto.AppointmentResponse;
import com.sparks.patient.dto.ErrorResponse;
import com.sparks.patient.entity.AppointmentStatus;
import com.sparks.patient.service.AppointmentService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

/**
 * REST Controller for Appointment Management API
 * 
 * SCRUM-22: Appointment Completion & Status Update
 * Acceptance Criteria:
 * - Allow staff to mark appointments as COMPLETED
 * - Updating status to COMPLETED is the trigger for the billing module
 * - When status moves from SCHEDULED to COMPLETED, the system logs the timestamp
 */
@RestController
@RequestMapping("/api/v1/appointments")
@RequiredArgsConstructor
@Tag(name = "Appointment Management", description = "APIs for appointment booking and status management")
public class AppointmentController {

    private final AppointmentService appointmentService;

    /**
     * Create a new appointment
     * POST /api/v1/appointments returns 201 Created
     */
    @PostMapping
    @Operation(summary = "Create a new appointment", description = "Schedule a new appointment for a patient with a doctor")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Appointment created successfully",
                content = @Content(schema = @Schema(implementation = AppointmentResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid request data",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<AppointmentResponse> createAppointment(
            @Valid @RequestBody AppointmentRequest request) {
        AppointmentResponse response = appointmentService.createAppointment(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Get appointment by ID
     * GET /api/v1/appointments/{id}
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get appointment by ID", description = "Retrieve appointment details by its unique ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Appointment found",
                content = @Content(schema = @Schema(implementation = AppointmentResponse.class))),
        @ApiResponse(responseCode = "404", description = "Appointment not found",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<AppointmentResponse> getAppointmentById(
            @Parameter(description = "Appointment ID", required = true)
            @PathVariable Long id) {
        AppointmentResponse response = appointmentService.getAppointmentById(id);
        return ResponseEntity.ok(response);
    }

    /**
     * Get all appointments or filter by patient, doctor, or status
     * GET /api/v1/appointments
     * GET /api/v1/appointments?patientId={patientId}
     * GET /api/v1/appointments?doctorId={doctorId}
     * GET /api/v1/appointments?status={status}
     */
    @GetMapping
    @Operation(summary = "Get all appointments", description = "Retrieve all appointments or filter by patient ID, doctor ID, or status")
    @ApiResponse(responseCode = "200", description = "List of appointments retrieved successfully")
    public ResponseEntity<List<AppointmentResponse>> getAllAppointments(
            @Parameter(description = "Filter by patient ID")
            @RequestParam(required = false) Long patientId,
            @Parameter(description = "Filter by doctor ID")
            @RequestParam(required = false) Long doctorId,
            @Parameter(description = "Filter by status (SCHEDULED, COMPLETED, CANCELLED)")
            @RequestParam(required = false) AppointmentStatus status) {
        
        List<AppointmentResponse> appointments;
        
        if (patientId != null) {
            appointments = appointmentService.getAppointmentsByPatientId(patientId);
        } else if (doctorId != null) {
            appointments = appointmentService.getAppointmentsByDoctorId(doctorId);
        } else if (status != null) {
            appointments = appointmentService.getAppointmentsByStatus(status);
        } else {
            appointments = appointmentService.getAllAppointments();
        }
        
        return ResponseEntity.ok(appointments);
    }

    /**
     * SCRUM-22: Mark appointment as COMPLETED
     * PATCH /api/v1/appointments/{id}/complete
     * 
     * Acceptance Criteria:
     * - Allow staff to mark appointments as COMPLETED
     * - Updating status to COMPLETED is the trigger for the billing module
     * - System logs the timestamp when status moves to COMPLETED
     */
    @PatchMapping("/{id}/complete")
    @Operation(summary = "Mark appointment as completed", 
               description = "Update appointment status to COMPLETED. This triggers the billing module and logs the completion timestamp.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Appointment marked as completed successfully",
                content = @Content(schema = @Schema(implementation = AppointmentResponse.class))),
        @ApiResponse(responseCode = "404", description = "Appointment not found",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<AppointmentResponse> markAppointmentAsCompleted(
            @Parameter(description = "Appointment ID", required = true)
            @PathVariable Long id) {
        AppointmentResponse response = appointmentService.markAppointmentAsCompleted(id);
        return ResponseEntity.ok(response);
    }

    /**
     * Cancel an appointment
     * PATCH /api/v1/appointments/{id}/cancel
     */
    @PatchMapping("/{id}/cancel")
    @Operation(summary = "Cancel appointment", description = "Update appointment status to CANCELLED")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Appointment cancelled successfully",
                content = @Content(schema = @Schema(implementation = AppointmentResponse.class))),
        @ApiResponse(responseCode = "404", description = "Appointment not found",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<AppointmentResponse> cancelAppointment(
            @Parameter(description = "Appointment ID", required = true)
            @PathVariable Long id) {
        AppointmentResponse response = appointmentService.cancelAppointment(id);
        return ResponseEntity.ok(response);
    }

    /**
     * Update an existing appointment
     * PUT /api/v1/appointments/{id}
     */
    @PutMapping("/{id}")
    @Operation(summary = "Update appointment", description = "Update an existing appointment's information")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Appointment updated successfully",
                content = @Content(schema = @Schema(implementation = AppointmentResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid request data",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "Appointment not found",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<AppointmentResponse> updateAppointment(
            @Parameter(description = "Appointment ID", required = true)
            @PathVariable Long id,
            @Valid @RequestBody AppointmentRequest request) {
        AppointmentResponse response = appointmentService.updateAppointment(id, request);
        return ResponseEntity.ok(response);
    }

    /**
     * Delete an appointment
     * DELETE /api/v1/appointments/{id}
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete appointment", description = "Remove an appointment from the system")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Appointment deleted successfully"),
        @ApiResponse(responseCode = "404", description = "Appointment not found",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<Void> deleteAppointment(
            @Parameter(description = "Appointment ID", required = true)
            @PathVariable Long id) {
        appointmentService.deleteAppointment(id);
        return ResponseEntity.noContent().build();
    }
}
