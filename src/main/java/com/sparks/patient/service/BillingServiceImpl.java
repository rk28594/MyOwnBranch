package com.sparks.patient.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sparks.patient.dto.InvoiceResponse;
import com.sparks.patient.entity.Appointment;
import com.sparks.patient.entity.Appointment.AppointmentStatus;
import com.sparks.patient.entity.Invoice;
import com.sparks.patient.entity.Invoice.PaymentStatus;
import com.sparks.patient.exception.AppointmentNotFoundException;
import com.sparks.patient.exception.InvoiceAlreadyExistsException;
import com.sparks.patient.exception.InvoiceNotFoundException;
import com.sparks.patient.mapper.InvoiceMapper;
import com.sparks.patient.repository.AppointmentRepository;
import com.sparks.patient.repository.InvoiceRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Billing Service Implementation - SCRUM-24: Automated Billing Engine
 * 
 * Implements automated billing engine that:
 * - Generates invoices for completed appointments
 * - Calculates totalAmount based on specialization premiums
 * - Creates entries in the INVOICES table automatically
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class BillingServiceImpl implements BillingService {

    private final InvoiceRepository invoiceRepository;
    private final AppointmentRepository appointmentRepository;
    private final InvoiceMapper invoiceMapper;

    // Base consultation amount
    private static final BigDecimal BASE_CONSULTATION_AMOUNT = new BigDecimal("100.00");

    // Specialization premium rates (as percentage of base amount)
    private static final Map<String, BigDecimal> SPECIALIZATION_RATES = new HashMap<>();
    
    static {
        // Premium specializations
        SPECIALIZATION_RATES.put("Cardiology", new BigDecimal("0.50")); // 50% premium
        SPECIALIZATION_RATES.put("Neurology", new BigDecimal("0.45")); // 45% premium
        SPECIALIZATION_RATES.put("Oncology", new BigDecimal("0.40")); // 40% premium
        SPECIALIZATION_RATES.put("Orthopedics", new BigDecimal("0.35")); // 35% premium
        
        // Standard specializations
        SPECIALIZATION_RATES.put("Pediatrics", new BigDecimal("0.25")); // 25% premium
        SPECIALIZATION_RATES.put("Dermatology", new BigDecimal("0.20")); // 20% premium
        SPECIALIZATION_RATES.put("General Medicine", new BigDecimal("0.10")); // 10% premium
        SPECIALIZATION_RATES.put("Family Medicine", new BigDecimal("0.05")); // 5% premium
    }

    @Override
    public InvoiceResponse generateInvoiceForAppointment(String appointmentId) {
        log.info("Generating invoice for appointment: {}", appointmentId);
        
        // Validate appointment exists
        Appointment appointment = appointmentRepository.findByAppointmentId(appointmentId)
                .orElseThrow(() -> new AppointmentNotFoundException(
                        "Appointment not found with id: " + appointmentId));
        
        // Check if appointment is completed
        if (appointment.getStatus() != AppointmentStatus.COMPLETED) {
            throw new IllegalStateException(
                    "Cannot generate invoice for appointment that is not completed. Current status: " 
                    + appointment.getStatus());
        }
        
        // Check if invoice already exists
        if (invoiceRepository.existsByAppointmentId(appointment.getId())) {
            throw new InvoiceAlreadyExistsException(
                    "Invoice already exists for appointment: " + appointmentId);
        }
        
        // Get doctor's specialization
        String specialization = appointment.getDoctor().getSpecialization();
        
        // Calculate amounts
        BigDecimal baseAmount = BASE_CONSULTATION_AMOUNT;
        BigDecimal premium = calculateSpecializationPremium(specialization, baseAmount);
        BigDecimal totalAmount = baseAmount.add(premium);
        
        // Create invoice
        Invoice invoice = Invoice.builder()
                .appointment(appointment)
                .baseAmount(baseAmount)
                .specializationPremium(premium)
                .totalAmount(totalAmount)
                .paymentStatus(PaymentStatus.PENDING)
                .build();
        
        Invoice savedInvoice = invoiceRepository.save(invoice);
        
        log.info("Invoice generated successfully for appointment {} with total amount: {}", 
                 appointmentId, totalAmount);
        
        return invoiceMapper.toResponse(savedInvoice);
    }

    @Override
    @Transactional(readOnly = true)
    public InvoiceResponse getInvoiceByAppointmentId(String appointmentId) {
        log.info("Fetching invoice for appointment: {}", appointmentId);
        
        Appointment appointment = appointmentRepository.findByAppointmentId(appointmentId)
                .orElseThrow(() -> new AppointmentNotFoundException(
                        "Appointment not found with id: " + appointmentId));
        
        Invoice invoice = invoiceRepository.findByAppointmentId(appointment.getId())
                .orElseThrow(() -> new InvoiceNotFoundException(
                        "Invoice not found for appointment: " + appointmentId));
        
        return invoiceMapper.toResponse(invoice);
    }

    @Override
    @Transactional(readOnly = true)
    public List<InvoiceResponse> getInvoicesByPatientId(Long patientId) {
        log.info("Fetching invoices for patient: {}", patientId);
        
        return invoiceRepository.findByPatientId(patientId).stream()
                .map(invoiceMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<InvoiceResponse> getInvoicesByPaymentStatus(PaymentStatus paymentStatus) {
        log.info("Fetching invoices with payment status: {}", paymentStatus);
        
        return invoiceRepository.findByPaymentStatus(paymentStatus).stream()
                .map(invoiceMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public BigDecimal calculateSpecializationPremium(String specialization, BigDecimal baseAmount) {
        BigDecimal rate = SPECIALIZATION_RATES.getOrDefault(specialization, BigDecimal.ZERO);
        BigDecimal premium = baseAmount.multiply(rate).setScale(2, RoundingMode.HALF_UP);
        
        log.debug("Calculated premium for specialization {}: base={}, rate={}, premium={}", 
                  specialization, baseAmount, rate, premium);
        
        return premium;
    }
}
