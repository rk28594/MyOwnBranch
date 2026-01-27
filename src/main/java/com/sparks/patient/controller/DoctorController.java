package com.sparks.patient.controller;

import java.util.List;

import javax.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sparks.patient.dto.DoctorRequest;
import com.sparks.patient.dto.DoctorResponse;
import com.sparks.patient.dto.ErrorResponse;
import com.sparks.patient.service.DoctorService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

/**
 * REST Controller for Doctor Management API
 * SCRUM-20: Doctor Profile Management
 * 
 * Provides CRUD operations for doctors with validation against duplicate license numbers
 */
@RestController
@RequestMapping("/api/v1/doctors")
@RequiredArgsConstructor
@Tag(name = "Doctor Management", description = "APIs for doctor profile management")
public class DoctorController {

    private final DoctorService doctorService;

    /**
     * Create a new doctor
     * POST /api/v1/doctors returns 201 Created
     * Returns 409 Conflict if license number already exists
     */
    @PostMapping
    @Operation(summary = "Create a new doctor", description = "Register a new doctor into the system")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Doctor created successfully",
                content = @Content(schema = @Schema(implementation = DoctorResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid request data",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "409", description = "License number already exists",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<DoctorResponse> createDoctor(
            @Valid @RequestBody DoctorRequest request) {
        try {
            DoctorResponse response = doctorService.createDoctor(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            // Return 409 Conflict for duplicate license number
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
    }

    /**
     * Get doctor by ID
     * GET /api/v1/doctors/{id}
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get doctor by ID", description = "Retrieve doctor profile by ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Doctor found",
                content = @Content(schema = @Schema(implementation = DoctorResponse.class))),
        @ApiResponse(responseCode = "404", description = "Doctor not found",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<DoctorResponse> getDoctorById(
            @Parameter(description = "Doctor ID", required = true) @PathVariable Long id) {
        DoctorResponse response = doctorService.getDoctorById(id);
        return ResponseEntity.ok(response);
    }

    /**
     * Get doctor by license number
     * GET /api/v1/doctors/license/{licenseNumber}
     */
    @GetMapping("/license/{licenseNumber}")
    @Operation(summary = "Get doctor by license number", description = "Retrieve doctor profile by license number")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Doctor found",
                content = @Content(schema = @Schema(implementation = DoctorResponse.class))),
        @ApiResponse(responseCode = "404", description = "Doctor not found",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<DoctorResponse> getDoctorByLicenseNumber(
            @Parameter(description = "License number", required = true) @PathVariable String licenseNumber) {
        DoctorResponse response = doctorService.getDoctorByLicenseNumber(licenseNumber);
        return ResponseEntity.ok(response);
    }

    /**
     * Get all doctors
     * GET /api/v1/doctors
     */
    @GetMapping
    @Operation(summary = "Get all doctors", description = "Retrieve list of all doctors")
    @ApiResponse(responseCode = "200", description = "List of doctors",
            content = @Content(schema = @Schema(implementation = DoctorResponse.class)))
    public ResponseEntity<List<DoctorResponse>> getAllDoctors() {
        List<DoctorResponse> response = doctorService.getAllDoctors();
        return ResponseEntity.ok(response);
    }

    /**
     * Update an existing doctor
     * PUT /api/v1/doctors/{id}
     */
    @PutMapping("/{id}")
    @Operation(summary = "Update a doctor", description = "Update doctor profile information")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Doctor updated successfully",
                content = @Content(schema = @Schema(implementation = DoctorResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid request data",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "Doctor not found",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "409", description = "License number already exists",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<DoctorResponse> updateDoctor(
            @Parameter(description = "Doctor ID", required = true) @PathVariable Long id,
            @Valid @RequestBody DoctorRequest request) {
        try {
            DoctorResponse response = doctorService.updateDoctor(id, request);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            // Return 409 Conflict for duplicate license number
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
    }

    /**
     * Delete a doctor
     * DELETE /api/v1/doctors/{id}
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a doctor", description = "Remove doctor from the system")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Doctor deleted successfully"),
        @ApiResponse(responseCode = "404", description = "Doctor not found",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<Void> deleteDoctor(
            @Parameter(description = "Doctor ID", required = true) @PathVariable Long id) {
        doctorService.deleteDoctor(id);
        return ResponseEntity.noContent().build();
    }
}
