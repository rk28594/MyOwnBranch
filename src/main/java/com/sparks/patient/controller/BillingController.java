package com.sparks.patient.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.sparks.patient.dto.InvoiceResponse;
import com.sparks.patient.entity.Invoice.PaymentStatus;
import com.sparks.patient.service.BillingService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Billing Controller - SCRUM-24: Automated Billing Engine
 * 
 * REST API endpoints for automated billing operations.
 * Generates invoices when appointments are completed.
 */
@RestController
@RequestMapping("/api/v1/billing")
@RequiredArgsConstructor
@Slf4j
public class BillingController {

    private final BillingService billingService;

    /**
     * Generate invoice for a completed appointment
     * 
     * POST /api/v1/billing/appointments/{appointmentId}/invoice
     * 
     * @param appointmentId the appointment UUID
     * @return the generated invoice
     */
    @PostMapping("/appointments/{appointmentId}/invoice")
    public ResponseEntity<InvoiceResponse> generateInvoice(@PathVariable String appointmentId) {
        log.info("REST request to generate invoice for appointment: {}", appointmentId);
        InvoiceResponse invoice = billingService.generateInvoiceForAppointment(appointmentId);
        return ResponseEntity.status(HttpStatus.CREATED).body(invoice);
    }

    /**
     * Get invoice by appointment ID
     * 
     * GET /api/v1/billing/appointments/{appointmentId}/invoice
     * 
     * @param appointmentId the appointment UUID
     * @return the invoice
     */
    @GetMapping("/appointments/{appointmentId}/invoice")
    public ResponseEntity<InvoiceResponse> getInvoiceByAppointment(@PathVariable String appointmentId) {
        log.info("REST request to get invoice for appointment: {}", appointmentId);
        InvoiceResponse invoice = billingService.getInvoiceByAppointmentId(appointmentId);
        return ResponseEntity.ok(invoice);
    }

    /**
     * Get all invoices for a patient
     * 
     * GET /api/v1/billing/patients/{patientId}/invoices
     * 
     * @param patientId the patient ID
     * @return list of invoices
     */
    @GetMapping("/patients/{patientId}/invoices")
    public ResponseEntity<List<InvoiceResponse>> getInvoicesByPatient(@PathVariable Long patientId) {
        log.info("REST request to get invoices for patient: {}", patientId);
        List<InvoiceResponse> invoices = billingService.getInvoicesByPatientId(patientId);
        return ResponseEntity.ok(invoices);
    }

    /**
     * Get invoices by payment status
     * 
     * GET /api/v1/billing/invoices?status={status}
     * 
     * @param status the payment status
     * @return list of invoices
     */
    @GetMapping("/invoices")
    public ResponseEntity<List<InvoiceResponse>> getInvoicesByStatus(
            @RequestParam(name = "status") PaymentStatus status) {
        log.info("REST request to get invoices with status: {}", status);
        List<InvoiceResponse> invoices = billingService.getInvoicesByPaymentStatus(status);
        return ResponseEntity.ok(invoices);
    }
}
