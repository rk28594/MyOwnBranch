package com.sparks.patient.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.sparks.patient.dto.InvoiceResponse;
import com.sparks.patient.entity.Appointment;
import com.sparks.patient.entity.Appointment.AppointmentStatus;
import com.sparks.patient.entity.Doctor;
import com.sparks.patient.entity.Invoice;
import com.sparks.patient.entity.Invoice.PaymentStatus;
import com.sparks.patient.entity.Patient;
import com.sparks.patient.exception.AppointmentNotFoundException;
import com.sparks.patient.exception.InvoiceAlreadyExistsException;
import com.sparks.patient.exception.InvoiceNotFoundException;
import com.sparks.patient.mapper.InvoiceMapper;
import com.sparks.patient.repository.AppointmentRepository;
import com.sparks.patient.repository.InvoiceRepository;
import com.sparks.patient.test.UnitTest;

/**
 * Unit tests for BillingServiceImpl - SCRUM-24: Automated Billing Engine
 * 
 * Tests the automated billing engine functionality:
 * - Invoice generation for completed appointments
 * - Specialization premium calculation
 * - Invoice retrieval operations
 */
@ExtendWith(MockitoExtension.class)
@UnitTest
@DisplayName("BillingService Unit Tests")
class BillingServiceImplTest {

    @Mock
    private InvoiceRepository invoiceRepository;

    @Mock
    private AppointmentRepository appointmentRepository;

    @Mock
    private InvoiceMapper invoiceMapper;

    @InjectMocks
    private BillingServiceImpl billingService;

    private Patient testPatient;
    private Doctor testDoctor;
    private Appointment testAppointment;
    private Invoice testInvoice;
    private InvoiceResponse testInvoiceResponse;

    @BeforeEach
    void setUp() {
        // Create test patient
        testPatient = Patient.builder()
                .id(1L)
                .firstName("John")
                .lastName("Doe")
                .build();

        // Create test doctor with Cardiology specialization
        testDoctor = Doctor.builder()
                .id(1L)
                .fullName("Dr. Smith")
                .specialization("Cardiology")
                .build();

        // Create completed appointment
        testAppointment = Appointment.builder()
                .id(1L)
                .appointmentId("appt-123")
                .patient(testPatient)
                .doctor(testDoctor)
                .appointmentTime(LocalDateTime.now())
                .status(AppointmentStatus.COMPLETED)
                .build();

        // Create test invoice
        testInvoice = Invoice.builder()
                .id(1L)
                .appointment(testAppointment)
                .baseAmount(new BigDecimal("100.00"))
                .specializationPremium(new BigDecimal("50.00"))
                .totalAmount(new BigDecimal("150.00"))
                .paymentStatus(PaymentStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .build();

        // Create test invoice response
        testInvoiceResponse = InvoiceResponse.builder()
                .id(1L)
                .appointmentId("appt-123")
                .totalAmount(new BigDecimal("150.00"))
                .baseAmount(new BigDecimal("100.00"))
                .specializationPremium(new BigDecimal("50.00"))
                .paymentStatus(PaymentStatus.PENDING)
                .patientName("John Doe")
                .doctorName("Dr. Smith")
                .specialization("Cardiology")
                .build();
    }

    @Nested
    @DisplayName("Generate Invoice Tests")
    class GenerateInvoiceTests {

        @Test
        @DisplayName("Should generate invoice for completed appointment with Cardiology specialization")
        void shouldGenerateInvoiceForCompletedAppointment() {
            // Given
            when(appointmentRepository.findByAppointmentId("appt-123"))
                    .thenReturn(Optional.of(testAppointment));
            when(invoiceRepository.existsByAppointmentId(1L)).thenReturn(false);
            when(invoiceRepository.save(any(Invoice.class))).thenReturn(testInvoice);
            when(invoiceMapper.toResponse(testInvoice)).thenReturn(testInvoiceResponse);

            // When
            InvoiceResponse result = billingService.generateInvoiceForAppointment("appt-123");

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getAppointmentId()).isEqualTo("appt-123");
            assertThat(result.getTotalAmount()).isEqualByComparingTo(new BigDecimal("150.00"));
            assertThat(result.getBaseAmount()).isEqualByComparingTo(new BigDecimal("100.00"));
            assertThat(result.getSpecializationPremium()).isEqualByComparingTo(new BigDecimal("50.00"));
            assertThat(result.getPaymentStatus()).isEqualTo(PaymentStatus.PENDING);

            verify(appointmentRepository).findByAppointmentId("appt-123");
            verify(invoiceRepository).existsByAppointmentId(1L);
            verify(invoiceRepository).save(any(Invoice.class));
            verify(invoiceMapper).toResponse(any(Invoice.class));
        }

        @Test
        @DisplayName("Should throw exception when appointment not found")
        void shouldThrowExceptionWhenAppointmentNotFound() {
            // Given
            when(appointmentRepository.findByAppointmentId("invalid-id"))
                    .thenReturn(Optional.empty());

            // When/Then
            assertThatThrownBy(() -> billingService.generateInvoiceForAppointment("invalid-id"))
                    .isInstanceOf(AppointmentNotFoundException.class)
                    .hasMessageContaining("Appointment not found with id: invalid-id");

            verify(appointmentRepository).findByAppointmentId("invalid-id");
            verify(invoiceRepository, never()).save(any(Invoice.class));
        }

        @Test
        @DisplayName("Should throw exception when appointment is not completed")
        void shouldThrowExceptionWhenAppointmentNotCompleted() {
            // Given
            testAppointment.setStatus(AppointmentStatus.SCHEDULED);
            when(appointmentRepository.findByAppointmentId("appt-123"))
                    .thenReturn(Optional.of(testAppointment));

            // When/Then
            assertThatThrownBy(() -> billingService.generateInvoiceForAppointment("appt-123"))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Cannot generate invoice for appointment that is not completed");

            verify(appointmentRepository).findByAppointmentId("appt-123");
            verify(invoiceRepository, never()).save(any(Invoice.class));
        }

        @Test
        @DisplayName("Should throw exception when invoice already exists")
        void shouldThrowExceptionWhenInvoiceAlreadyExists() {
            // Given
            when(appointmentRepository.findByAppointmentId("appt-123"))
                    .thenReturn(Optional.of(testAppointment));
            when(invoiceRepository.existsByAppointmentId(1L)).thenReturn(true);

            // When/Then
            assertThatThrownBy(() -> billingService.generateInvoiceForAppointment("appt-123"))
                    .isInstanceOf(InvoiceAlreadyExistsException.class)
                    .hasMessageContaining("Invoice already exists for appointment: appt-123");

            verify(appointmentRepository).findByAppointmentId("appt-123");
            verify(invoiceRepository).existsByAppointmentId(1L);
            verify(invoiceRepository, never()).save(any(Invoice.class));
        }

        @Test
        @DisplayName("Should calculate correct premium for Neurology specialization")
        void shouldCalculateCorrectPremiumForNeurology() {
            // Given
            testDoctor.setSpecialization("Neurology");
            when(appointmentRepository.findByAppointmentId("appt-123"))
                    .thenReturn(Optional.of(testAppointment));
            when(invoiceRepository.existsByAppointmentId(1L)).thenReturn(false);
            when(invoiceRepository.save(any(Invoice.class))).thenAnswer(invocation -> {
                Invoice invoice = invocation.getArgument(0);
                // Verify Neurology premium is 45%
                assertThat(invoice.getSpecializationPremium())
                        .isEqualByComparingTo(new BigDecimal("45.00"));
                assertThat(invoice.getTotalAmount())
                        .isEqualByComparingTo(new BigDecimal("145.00"));
                return invoice;
            });
            when(invoiceMapper.toResponse(any(Invoice.class))).thenReturn(testInvoiceResponse);

            // When
            billingService.generateInvoiceForAppointment("appt-123");

            // Then
            verify(invoiceRepository).save(any(Invoice.class));
        }
    }

    @Nested
    @DisplayName("Calculate Specialization Premium Tests")
    class CalculateSpecializationPremiumTests {

        @Test
        @DisplayName("Should calculate 50% premium for Cardiology")
        void shouldCalculate50PercentPremiumForCardiology() {
            // When
            BigDecimal premium = billingService.calculateSpecializationPremium(
                    "Cardiology", new BigDecimal("100.00"));

            // Then
            assertThat(premium).isEqualByComparingTo(new BigDecimal("50.00"));
        }

        @Test
        @DisplayName("Should calculate 45% premium for Neurology")
        void shouldCalculate45PercentPremiumForNeurology() {
            // When
            BigDecimal premium = billingService.calculateSpecializationPremium(
                    "Neurology", new BigDecimal("100.00"));

            // Then
            assertThat(premium).isEqualByComparingTo(new BigDecimal("45.00"));
        }

        @Test
        @DisplayName("Should calculate 40% premium for Oncology")
        void shouldCalculate40PercentPremiumForOncology() {
            // When
            BigDecimal premium = billingService.calculateSpecializationPremium(
                    "Oncology", new BigDecimal("100.00"));

            // Then
            assertThat(premium).isEqualByComparingTo(new BigDecimal("40.00"));
        }

        @Test
        @DisplayName("Should calculate 0% premium for unknown specialization")
        void shouldCalculateZeroPremiumForUnknownSpecialization() {
            // When
            BigDecimal premium = billingService.calculateSpecializationPremium(
                    "Unknown Specialty", new BigDecimal("100.00"));

            // Then
            assertThat(premium).isEqualByComparingTo(BigDecimal.ZERO);
        }
    }

    @Nested
    @DisplayName("Get Invoice Tests")
    class GetInvoiceTests {

        @Test
        @DisplayName("Should get invoice by appointment ID")
        void shouldGetInvoiceByAppointmentId() {
            // Given
            when(appointmentRepository.findByAppointmentId("appt-123"))
                    .thenReturn(Optional.of(testAppointment));
            when(invoiceRepository.findByAppointmentId(1L))
                    .thenReturn(Optional.of(testInvoice));
            when(invoiceMapper.toResponse(testInvoice)).thenReturn(testInvoiceResponse);

            // When
            InvoiceResponse result = billingService.getInvoiceByAppointmentId("appt-123");

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getAppointmentId()).isEqualTo("appt-123");

            verify(appointmentRepository).findByAppointmentId("appt-123");
            verify(invoiceRepository).findByAppointmentId(1L);
            verify(invoiceMapper).toResponse(testInvoice);
        }

        @Test
        @DisplayName("Should throw exception when invoice not found")
        void shouldThrowExceptionWhenInvoiceNotFound() {
            // Given
            when(appointmentRepository.findByAppointmentId("appt-123"))
                    .thenReturn(Optional.of(testAppointment));
            when(invoiceRepository.findByAppointmentId(1L))
                    .thenReturn(Optional.empty());

            // When/Then
            assertThatThrownBy(() -> billingService.getInvoiceByAppointmentId("appt-123"))
                    .isInstanceOf(InvoiceNotFoundException.class)
                    .hasMessageContaining("Invoice not found for appointment: appt-123");

            verify(appointmentRepository).findByAppointmentId("appt-123");
            verify(invoiceRepository).findByAppointmentId(1L);
        }

        @Test
        @DisplayName("Should get invoices by patient ID")
        void shouldGetInvoicesByPatientId() {
            // Given
            List<Invoice> invoices = Arrays.asList(testInvoice);
            when(invoiceRepository.findByPatientId(1L)).thenReturn(invoices);
            when(invoiceMapper.toResponse(testInvoice)).thenReturn(testInvoiceResponse);

            // When
            List<InvoiceResponse> results = billingService.getInvoicesByPatientId(1L);

            // Then
            assertThat(results).hasSize(1);
            assertThat(results.get(0).getPatientName()).isEqualTo("John Doe");

            verify(invoiceRepository).findByPatientId(1L);
            verify(invoiceMapper, times(1)).toResponse(testInvoice);
        }

        @Test
        @DisplayName("Should get invoices by payment status")
        void shouldGetInvoicesByPaymentStatus() {
            // Given
            List<Invoice> invoices = Arrays.asList(testInvoice);
            when(invoiceRepository.findByPaymentStatus(PaymentStatus.PENDING))
                    .thenReturn(invoices);
            when(invoiceMapper.toResponse(testInvoice)).thenReturn(testInvoiceResponse);

            // When
            List<InvoiceResponse> results = billingService
                    .getInvoicesByPaymentStatus(PaymentStatus.PENDING);

            // Then
            assertThat(results).hasSize(1);
            assertThat(results.get(0).getPaymentStatus()).isEqualTo(PaymentStatus.PENDING);

            verify(invoiceRepository).findByPaymentStatus(PaymentStatus.PENDING);
            verify(invoiceMapper, times(1)).toResponse(testInvoice);
        }
    }
}
