package com.sparks.patient.mapper;

import org.springframework.stereotype.Component;

import com.sparks.patient.dto.InvoiceResponse;
import com.sparks.patient.entity.Invoice;

/**
 * Invoice Mapper - SCRUM-24: Automated Billing Engine
 * 
 * Maps between Invoice entity and InvoiceResponse DTO.
 */
@Component
public class InvoiceMapper {

    public InvoiceResponse toResponse(Invoice invoice) {
        if (invoice == null) {
            return null;
        }

        return InvoiceResponse.builder()
                .id(invoice.getId())
                .appointmentId(invoice.getAppointment().getAppointmentId())
                .appointmentDbId(invoice.getAppointment().getId())
                .totalAmount(invoice.getTotalAmount())
                .baseAmount(invoice.getBaseAmount())
                .specializationPremium(invoice.getSpecializationPremium())
                .paymentStatus(invoice.getPaymentStatus())
                .patientName(invoice.getAppointment().getPatient().getFirstName() + " " 
                           + invoice.getAppointment().getPatient().getLastName())
                .doctorName(invoice.getAppointment().getDoctor().getFullName())
                .specialization(invoice.getAppointment().getDoctor().getSpecialization())
                .createdAt(invoice.getCreatedAt())
                .build();
    }
}
