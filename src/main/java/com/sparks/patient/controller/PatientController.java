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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.sparks.patient.dto.ErrorResponse;
import com.sparks.patient.dto.PatientRequest;
import com.sparks.patient.dto.PatientResponse;
import com.sparks.patient.service.PatientService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

/**
 * REST Controller for Patient Management API
 * 
 * SCRUM-14: Patient Onboarding API - POST /api/v1/patients
 * SCRUM-15: Patient Search & Profile Retrieval - GET /api/v1/patients/{id}
 */
@RestController
@RequestMapping("/api/v1/patients")
@RequiredArgsConstructor
@Tag(name = "Patient Management", description = "APIs for patient onboarding and profile management")
public class PatientController {

    private final PatientService patientService;

    /**
     * SCRUM-14: Patient Onboarding API
     * POST /api/v1/patients returns 201 Created
     */
    @PostMapping
    @Operation(summary = "Create a new patient", description = "Onboard a new patient into the system")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Patient created successfully",
                content = @Content(schema = @Schema(implementation = PatientResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid request data",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "409", description = "Email already exists",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<PatientResponse> createPatient(
            @Valid @RequestBody PatientRequest request) {
        PatientResponse response = patientService.createPatient(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * SCRUM-15: Patient Search & Profile Retrieval
     * GET /api/v1/patients/{id} returns the profile
     * Returns 404 for invalid IDs
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get patient by ID", description = "Retrieve patient profile by their unique ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Patient found",
                content = @Content(schema = @Schema(implementation = PatientResponse.class))),
        @ApiResponse(responseCode = "404", description = "Patient not found",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<PatientResponse> getPatientById(
            @Parameter(description = "Patient ID", required = true)
            @PathVariable Long id) {
        PatientResponse response = patientService.getPatientById(id);
        return ResponseEntity.ok(response);
    }

    /**
     * Get all patients
     */
    @GetMapping
    @Operation(summary = "Get all patients", description = "Retrieve a list of all registered patients")
    @ApiResponse(responseCode = "200", description = "List of patients retrieved successfully")
    public ResponseEntity<List<PatientResponse>> getAllPatients() {
        List<PatientResponse> patients = patientService.getAllPatients();
        return ResponseEntity.ok(patients);
    }

    /**
     * Update an existing patient
     */
    @PutMapping("/{id}")
    @Operation(summary = "Update patient", description = "Update an existing patient's information")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Patient updated successfully",
                content = @Content(schema = @Schema(implementation = PatientResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid request data",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "Patient not found",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "409", description = "Email already exists",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<PatientResponse> updatePatient(
            @Parameter(description = "Patient ID", required = true)
            @PathVariable Long id,
            @Valid @RequestBody PatientRequest request) {
        PatientResponse response = patientService.updatePatient(id, request);
        return ResponseEntity.ok(response);
    }

    /**
     * Delete a patient
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete patient", description = "Remove a patient from the system")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Patient deleted successfully"),
        @ApiResponse(responseCode = "404", description = "Patient not found",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<Void> deletePatient(
            @Parameter(description = "Patient ID", required = true)
            @PathVariable Long id) {
        patientService.deletePatient(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Search for a patient by phone number
     */
    @GetMapping("/search")
    @Operation(summary = "Search patient by phone", description = "Find a patient by their phone number")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Patient found",
                content = @Content(schema = @Schema(implementation = PatientResponse.class))),
        @ApiResponse(responseCode = "404", description = "Patient not found with the given phone number",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<PatientResponse> searchPatientByPhone(
            @Parameter(description = "Phone number to search for", required = true, example = "+1234567890")
            @RequestParam String phone) {
        PatientResponse response = patientService.getPatientByPhone(phone);
        return ResponseEntity.ok(response);
    }

    /**
     * Search for patients by last name
     */
    @GetMapping("/by-lastname")
    @Operation(summary = "Search patients by last name", description = "Find all patients with a matching last name")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Patients found (may be empty list)",
                content = @Content(schema = @Schema(implementation = PatientResponse.class)))
    })
    public ResponseEntity<List<PatientResponse>> searchPatientsByLastName(
            @Parameter(description = "Last name to search for", required = true, example = "Doe")
            @RequestParam String lastname) {
        List<PatientResponse> responses = patientService.getPatientsByLastName(lastname);
        return ResponseEntity.ok(responses);
    }
}
