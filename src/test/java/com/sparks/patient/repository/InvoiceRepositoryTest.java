package com.sparks.patient.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import com.sparks.patient.entity.Appointment;
import com.sparks.patient.entity.Appointment.AppointmentStatus;
import com.sparks.patient.entity.Doctor;
import com.sparks.patient.entity.Invoice;
import com.sparks.patient.entity.Invoice.PaymentStatus;
import com.sparks.patient.entity.Patient;
import com.sparks.patient.test.UnitTest;

import java.time.LocalDate;

/**
 * Repository tests for InvoiceRepository - SCRUM-24: Automated Billing Engine
 */
@DataJpaTest
@UnitTest
@DisplayName("InvoiceRepository Tests")
class InvoiceRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private InvoiceRepository invoiceRepository;

    private Patient testPatient;
    private Doctor testDoctor;
    private Appointment testAppointment;

    @BeforeEach
    void setUp() {
        // Create and persist test patient
        testPatient = Patient.builder()
                .firstName("John")
                .lastName("Doe")
                .dob(LocalDate.of(1990, 1, 1))
                .email("john.doe@test.com")
                .phone("+1234567890")
                .build();
        entityManager.persist(testPatient);

        // Create and persist test doctor
        testDoctor = Doctor.builder()
                .fullName("Dr. Smith")
                .licenseNumber("LIC12345")
                .specialization("Cardiology")
                .deptId(1L)
                .build();
        entityManager.persist(testDoctor);

        // Create and persist test appointment
        testAppointment = Appointment.builder()
                .patient(testPatient)
                .doctor(testDoctor)
                .appointmentTime(LocalDateTime.now())
                .status(AppointmentStatus.COMPLETED)
                .build();
        entityManager.persist(testAppointment);
        entityManager.flush();
    }

    @Test
    @DisplayName("Should save invoice with all fields")
    void shouldSaveInvoice() {
        // Given
        Invoice invoice = Invoice.builder()
                .appointment(testAppointment)
                .baseAmount(new BigDecimal("100.00"))
                .specializationPremium(new BigDecimal("50.00"))
                .totalAmount(new BigDecimal("150.00"))
                .paymentStatus(PaymentStatus.PENDING)
                .build();

        // When
        Invoice savedInvoice = invoiceRepository.save(invoice);

        // Then
        assertThat(savedInvoice.getId()).isNotNull();
        assertThat(savedInvoice.getAppointment()).isEqualTo(testAppointment);
        assertThat(savedInvoice.getTotalAmount()).isEqualByComparingTo(new BigDecimal("150.00"));
        assertThat(savedInvoice.getBaseAmount()).isEqualByComparingTo(new BigDecimal("100.00"));
        assertThat(savedInvoice.getSpecializationPremium())
                .isEqualByComparingTo(new BigDecimal("50.00"));
        assertThat(savedInvoice.getPaymentStatus()).isEqualTo(PaymentStatus.PENDING);
        assertThat(savedInvoice.getCreatedAt()).isNotNull();
    }

    @Test
    @DisplayName("Should find invoice by appointment ID")
    void shouldFindByAppointmentId() {
        // Given
        Invoice invoice = createAndSaveInvoice();

        // When
        Optional<Invoice> found = invoiceRepository.findByAppointmentId(testAppointment.getId());

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getId()).isEqualTo(invoice.getId());
        assertThat(found.get().getAppointment().getId()).isEqualTo(testAppointment.getId());
    }

    @Test
    @DisplayName("Should return empty when invoice not found by appointment ID")
    void shouldReturnEmptyWhenNotFoundByAppointmentId() {
        // When
        Optional<Invoice> found = invoiceRepository.findByAppointmentId(999L);

        // Then
        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("Should find invoices by payment status")
    void shouldFindByPaymentStatus() {
        // Given
        createAndSaveInvoice();
        Invoice paidInvoice = Invoice.builder()
                .appointment(createAnotherAppointment())
                .baseAmount(new BigDecimal("100.00"))
                .specializationPremium(new BigDecimal("30.00"))
                .totalAmount(new BigDecimal("130.00"))
                .paymentStatus(PaymentStatus.PAID)
                .build();
        invoiceRepository.save(paidInvoice);

        // When
        List<Invoice> pendingInvoices = invoiceRepository.findByPaymentStatus(PaymentStatus.PENDING);
        List<Invoice> paidInvoices = invoiceRepository.findByPaymentStatus(PaymentStatus.PAID);

        // Then
        assertThat(pendingInvoices).hasSize(1);
        assertThat(pendingInvoices.get(0).getPaymentStatus()).isEqualTo(PaymentStatus.PENDING);
        assertThat(paidInvoices).hasSize(1);
        assertThat(paidInvoices.get(0).getPaymentStatus()).isEqualTo(PaymentStatus.PAID);
    }

    @Test
    @DisplayName("Should find invoices by patient ID")
    void shouldFindByPatientId() {
        // Given
        createAndSaveInvoice();

        // When
        List<Invoice> invoices = invoiceRepository.findByPatientId(testPatient.getId());

        // Then
        assertThat(invoices).hasSize(1);
        assertThat(invoices.get(0).getAppointment().getPatient().getId())
                .isEqualTo(testPatient.getId());
    }

    @Test
    @DisplayName("Should check if invoice exists by appointment ID")
    void shouldCheckExistenceByAppointmentId() {
        // Given
        createAndSaveInvoice();

        // When
        boolean exists = invoiceRepository.existsByAppointmentId(testAppointment.getId());
        boolean notExists = invoiceRepository.existsByAppointmentId(999L);

        // Then
        assertThat(exists).isTrue();
        assertThat(notExists).isFalse();
    }

    @Test
    @DisplayName("Should enforce unique constraint on appointment")
    void shouldEnforceUniqueAppointment() {
        // Given
        createAndSaveInvoice();

        // When/Then
        Invoice duplicate = Invoice.builder()
                .appointment(testAppointment)
                .baseAmount(new BigDecimal("100.00"))
                .specializationPremium(new BigDecimal("50.00"))
                .totalAmount(new BigDecimal("150.00"))
                .paymentStatus(PaymentStatus.PENDING)
                .build();

        // This should fail due to unique constraint
        try {
            invoiceRepository.save(duplicate);
            entityManager.flush();
            assertThat(false).as("Should have thrown exception").isTrue();
        } catch (Exception e) {
            // Expected - unique constraint violation
            assertThat(e).isNotNull();
        }
    }

    private Invoice createAndSaveInvoice() {
        Invoice invoice = Invoice.builder()
                .appointment(testAppointment)
                .baseAmount(new BigDecimal("100.00"))
                .specializationPremium(new BigDecimal("50.00"))
                .totalAmount(new BigDecimal("150.00"))
                .paymentStatus(PaymentStatus.PENDING)
                .build();
        return invoiceRepository.save(invoice);
    }

    private Appointment createAnotherAppointment() {
        Patient anotherPatient = Patient.builder()
                .firstName("Jane")
                .lastName("Smith")
                .dob(LocalDate.of(1985, 5, 15))
                .email("jane.smith@test.com")
                .phone("+1987654321")
                .build();
        entityManager.persist(anotherPatient);

        Appointment appointment = Appointment.builder()
                .patient(anotherPatient)
                .doctor(testDoctor)
                .appointmentTime(LocalDateTime.now().plusDays(1))
                .status(AppointmentStatus.COMPLETED)
                .build();
        entityManager.persist(appointment);
        entityManager.flush();
        return appointment;
    }
}
