package com.sparks.patient.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.sparks.patient.entity.Invoice.PaymentStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Invoice Response DTO - SCRUM-24: Automated Billing Engine
 * 
 * Response containing invoice details for completed appointments.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InvoiceResponse {

    private Long id;
    private String appointmentId;
    private Long appointmentDbId;
    private BigDecimal totalAmount;
    private BigDecimal baseAmount;
    private BigDecimal specializationPremium;
    private PaymentStatus paymentStatus;
    private String patientName;
    private String doctorName;
    private String specialization;
    private LocalDateTime createdAt;
}
