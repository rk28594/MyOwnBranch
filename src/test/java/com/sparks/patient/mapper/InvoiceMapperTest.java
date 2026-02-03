package com.sparks.patient.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.sparks.patient.dto.InvoiceResponse;
import com.sparks.patient.entity.Appointment;
import com.sparks.patient.entity.Doctor;
import com.sparks.patient.entity.Invoice;
import com.sparks.patient.entity.Invoice.PaymentStatus;
import com.sparks.patient.entity.Patient;
import com.sparks.patient.test.UnitTest;

/**
 * Unit tests for InvoiceMapper - SCRUM-24: Automated Billing Engine
 */
@UnitTest
@DisplayName("InvoiceMapper Tests")
class InvoiceMapperTest {

    private InvoiceMapper invoiceMapper;
    private Patient testPatient;
    private Doctor testDoctor;
    private Appointment testAppointment;

    @BeforeEach
    void setUp() {
        invoiceMapper = new InvoiceMapper();

        testPatient = Patient.builder()
                .id(1L)
                .firstName("John")
                .lastName("Doe")
                .build();

        testDoctor = Doctor.builder()
                .id(1L)
                .fullName("Dr. Smith")
                .specialization("Cardiology")
                .build();

        testAppointment = Appointment.builder()
                .id(1L)
                .appointmentId("appt-123")
                .patient(testPatient)
                .doctor(testDoctor)
                .appointmentTime(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("Should map invoice entity to response DTO")
    void shouldMapInvoiceToResponse() {
        // Given
        LocalDateTime createdAt = LocalDateTime.now();
        Invoice invoice = Invoice.builder()
                .id(1L)
                .appointment(testAppointment)
                .baseAmount(new BigDecimal("100.00"))
                .specializationPremium(new BigDecimal("50.00"))
                .totalAmount(new BigDecimal("150.00"))
                .paymentStatus(PaymentStatus.PENDING)
                .createdAt(createdAt)
                .build();

        // When
        InvoiceResponse response = invoiceMapper.toResponse(invoice);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getAppointmentId()).isEqualTo("appt-123");
        assertThat(response.getAppointmentDbId()).isEqualTo(1L);
        assertThat(response.getTotalAmount()).isEqualByComparingTo(new BigDecimal("150.00"));
        assertThat(response.getBaseAmount()).isEqualByComparingTo(new BigDecimal("100.00"));
        assertThat(response.getSpecializationPremium())
                .isEqualByComparingTo(new BigDecimal("50.00"));
        assertThat(response.getPaymentStatus()).isEqualTo(PaymentStatus.PENDING);
        assertThat(response.getPatientName()).isEqualTo("John Doe");
        assertThat(response.getDoctorName()).isEqualTo("Dr. Smith");
        assertThat(response.getSpecialization()).isEqualTo("Cardiology");
        assertThat(response.getCreatedAt()).isEqualTo(createdAt);
    }

    @Test
    @DisplayName("Should return null when invoice is null")
    void shouldReturnNullWhenInvoiceIsNull() {
        // When
        InvoiceResponse response = invoiceMapper.toResponse(null);

        // Then
        assertThat(response).isNull();
    }

    @Test
    @DisplayName("Should correctly concatenate patient name")
    void shouldCorrectlyConcatenatePatientName() {
        // Given
        testPatient.setFirstName("Jane");
        testPatient.setLastName("Smith");
        Invoice invoice = Invoice.builder()
                .id(1L)
                .appointment(testAppointment)
                .baseAmount(new BigDecimal("100.00"))
                .specializationPremium(new BigDecimal("50.00"))
                .totalAmount(new BigDecimal("150.00"))
                .paymentStatus(PaymentStatus.PAID)
                .createdAt(LocalDateTime.now())
                .build();

        // When
        InvoiceResponse response = invoiceMapper.toResponse(invoice);

        // Then
        assertThat(response.getPatientName()).isEqualTo("Jane Smith");
    }
}
