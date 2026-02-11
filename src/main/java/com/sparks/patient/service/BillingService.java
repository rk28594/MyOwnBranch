package com.sparks.patient.service;

import java.math.BigDecimal;
import java.util.List;

import com.sparks.patient.dto.InvoiceResponse;
import com.sparks.patient.entity.Invoice.PaymentStatus;

/**
 * Billing Service Interface - SCRUM-24: Automated Billing Engine
 * 
 * Service interface for automated billing operations.
 * Generates invoices when appointments are completed,
 * calculating total amounts based on specialization premiums.
 */
public interface BillingService {

    /**
     * Generate invoice for a completed appointment.
     * Automatically calculates totalAmount based on specialization premium.
     * 
     * @param appointmentId the appointment UUID
     * @return the generated invoice response
     */
    InvoiceResponse generateInvoiceForAppointment(String appointmentId);

    /**
     * Get invoice by appointment ID
     * 
     * @param appointmentId the appointment UUID
     * @return the invoice response
     */
    InvoiceResponse getInvoiceByAppointmentId(String appointmentId);

    /**
     * Get all invoices for a patient
     * 
     * @param patientId the patient ID
     * @return list of invoice responses
     */
    List<InvoiceResponse> getInvoicesByPatientId(Long patientId);

    /**
     * Get invoices by payment status
     * 
     * @param paymentStatus the payment status
     * @return list of invoice responses
     */
    List<InvoiceResponse> getInvoicesByPaymentStatus(PaymentStatus paymentStatus);

    /**
     * Calculate specialization premium based on doctor's specialization
     * 
     * @param specialization the doctor's specialization
     * @param baseAmount the base consultation amount
     * @return the premium amount
     */
    BigDecimal calculateSpecializationPremium(String specialization, BigDecimal baseAmount);
}
