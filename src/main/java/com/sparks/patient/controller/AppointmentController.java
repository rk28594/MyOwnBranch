package com.sparks.patient.controller;

import java.util.List;

import javax.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.sparks.patient.dto.AppointmentRequest;
import com.sparks.patient.dto.AppointmentResponse;
import com.sparks.patient.service.AppointmentService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Appointment Controller - SCRUM-23: Appointment Request Workflow
 * 
 * REST API endpoints for appointment management
 */
@RestController
@RequestMapping("/api/appointments")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Appointment Management", description = "APIs for managing patient appointments")
public class AppointmentController {

    private final AppointmentService appointmentService;

    /**
     * Create a new appointment
     * 
     * @param request Appointment request containing patient, doctor, and time
     * @return Appointment response with generated UUID
     */
    @PostMapping
    @Operation(summary = "Create appointment", description = "Create a new appointment with SCHEDULED status")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Appointment created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request"),
        @ApiResponse(responseCode = "404", description = "Patient or Doctor not found")
    })
    public ResponseEntity<AppointmentResponse> createAppointment(@Valid @RequestBody AppointmentRequest request) {
        log.info("POST /api/appointments - Creating appointment");
        AppointmentResponse response = appointmentService.createAppointment(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Get appointment by UUID
     * 
     * @param appointmentId UUID of the appointment
     * @return Appointment response
     */
    @GetMapping("/{appointmentId}")
    @Operation(summary = "Get appointment by ID", description = "Retrieve appointment details by UUID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Appointment found"),
        @ApiResponse(responseCode = "404", description = "Appointment not found")
    })
    public ResponseEntity<AppointmentResponse> getAppointment(@PathVariable String appointmentId) {
        log.info("GET /api/appointments/{} - Fetching appointment", appointmentId);
        AppointmentResponse response = appointmentService.getAppointmentById(appointmentId);
        return ResponseEntity.ok(response);
    }

    /**
     * Get all appointments
     * 
     * @param patientId Optional patient ID filter
     * @param doctorId Optional doctor ID filter
     * @return List of appointments
     */
    @GetMapping
    @Operation(summary = "Get all appointments", description = "Retrieve all appointments with optional filters")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Appointments retrieved successfully")
    })
    public ResponseEntity<List<AppointmentResponse>> getAllAppointments(
            @RequestParam(required = false) Long patientId,
            @RequestParam(required = false) Long doctorId) {
        
        log.info("GET /api/appointments - Fetching appointments (patientId: {}, doctorId: {})", 
                 patientId, doctorId);
        
        List<AppointmentResponse> responses;
        
        if (patientId != null) {
            responses = appointmentService.getAppointmentsByPatientId(patientId);
        } else if (doctorId != null) {
            responses = appointmentService.getAppointmentsByDoctorId(doctorId);
        } else {
            responses = appointmentService.getAllAppointments();
        }
        
        return ResponseEntity.ok(responses);
    }
}
