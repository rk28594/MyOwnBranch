package com.sparks.patient.integration;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.TestPropertySource;

import com.sparks.patient.entity.Appointment;
import com.sparks.patient.entity.Appointment.AppointmentStatus;
import com.sparks.patient.entity.Doctor;
import com.sparks.patient.entity.Invoice;
import com.sparks.patient.entity.Invoice.PaymentStatus;
import com.sparks.patient.entity.Patient;
import com.sparks.patient.repository.AppointmentRepository;
import com.sparks.patient.repository.DoctorRepository;
import com.sparks.patient.repository.InvoiceRepository;
import com.sparks.patient.repository.PatientRepository;
import com.sparks.patient.test.IntegrationTest;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;

/**
 * Integration tests for Billing - SCRUM-24: Automated Billing Engine
 * 
 * Tests the complete billing workflow:
 * - Invoice generation for completed appointments
 * - Specialization-based premium calculation
 * - Invoice retrieval operations
 */
@IntegrationTest
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = {"spring.jpa.show-sql=false", "logging.level.org.hibernate.SQL=ERROR"})
@DisplayName("Billing Integration Tests")
class BillingIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private DoctorRepository doctorRepository;

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private InvoiceRepository invoiceRepository;

    private Patient patient;
    private Doctor cardiologyDoctor;
    private Doctor neurologyDoctor;
    private Appointment completedAppointment;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
        RestAssured.basePath = "/api/v1/billing";

        // Clean up - delete in correct order
        invoiceRepository.deleteAll();
        appointmentRepository.deleteAll();
        patientRepository.deleteAll();
        doctorRepository.deleteAll();

        // Create test patient
        patient = patientRepository.save(Patient.builder()
                .firstName("Jane")
                .lastName("Doe")
                .email("jane.doe.billing@test.com")
                .phone("+1234567890")
                .dob(LocalDate.of(1985, 5, 15))
                .build());

        // Create doctors with different specializations
        cardiologyDoctor = doctorRepository.save(Doctor.builder()
                .fullName("Dr. Cardio")
                .licenseNumber("LIC-CARDIO-001")
                .specialization("Cardiology")
                .deptId(1L)
                .build());

        neurologyDoctor = doctorRepository.save(Doctor.builder()
                .fullName("Dr. Neuro")
                .licenseNumber("LIC-NEURO-001")
                .specialization("Neurology")
                .deptId(2L)
                .build());

        // Create completed appointment
        completedAppointment = appointmentRepository.save(Appointment.builder()
                .patient(patient)
                .doctor(cardiologyDoctor)
                .appointmentTime(LocalDateTime.now())
                .status(AppointmentStatus.COMPLETED)
                .build());
    }

    @Nested
    @DisplayName("Generate Invoice Tests")
    class GenerateInvoiceTests {

        @Test
        @DisplayName("Should generate invoice for completed appointment with Cardiology premium")
        void shouldGenerateInvoiceForCompletedAppointment() {
            given()
                .contentType(ContentType.JSON)
            .when()
                .post("/appointments/" + completedAppointment.getAppointmentId() + "/invoice")
            .then()
                .statusCode(HttpStatus.CREATED.value())
                .body("appointmentId", equalTo(completedAppointment.getAppointmentId()))
                .body("totalAmount", equalTo(150.00f)) // 100 base + 50% premium for Cardiology
                .body("baseAmount", equalTo(100.00f))
                .body("specializationPremium", equalTo(50.00f))
                .body("paymentStatus", equalTo("PENDING"))
                .body("patientName", equalTo("Jane Doe"))
                .body("doctorName", equalTo("Dr. Cardio"))
                .body("specialization", equalTo("Cardiology"))
                .body("createdAt", notNullValue());
        }

        @Test
        @DisplayName("Should generate invoice with Neurology premium (45%)")
        void shouldGenerateInvoiceWithNeurologyPremium() {
            // Create neurology appointment
            Appointment neuroAppointment = appointmentRepository.save(Appointment.builder()
                    .patient(patient)
                    .doctor(neurologyDoctor)
                    .appointmentTime(LocalDateTime.now().plusHours(1))
                    .status(AppointmentStatus.COMPLETED)
                    .build());

            given()
                .contentType(ContentType.JSON)
            .when()
                .post("/appointments/" + neuroAppointment.getAppointmentId() + "/invoice")
            .then()
                .statusCode(HttpStatus.CREATED.value())
                .body("totalAmount", equalTo(145.00f)) // 100 base + 45% premium for Neurology
                .body("specializationPremium", equalTo(45.00f))
                .body("specialization", equalTo("Neurology"));
        }

        @Test
        @DisplayName("Should return 404 when appointment not found")
        void shouldReturn404WhenAppointmentNotFound() {
            given()
                .contentType(ContentType.JSON)
            .when()
                .post("/appointments/invalid-appointment-id/invoice")
            .then()
                .statusCode(HttpStatus.NOT_FOUND.value())
                .body("message", notNullValue());
        }

        @Test
        @DisplayName("Should return 400 when appointment is not completed")
        void shouldReturn400WhenAppointmentNotCompleted() {
            // Create scheduled appointment
            Appointment scheduledAppointment = appointmentRepository.save(Appointment.builder()
                    .patient(patient)
                    .doctor(cardiologyDoctor)
                    .appointmentTime(LocalDateTime.now().plusDays(1))
                    .status(AppointmentStatus.SCHEDULED)
                    .build());

            given()
                .contentType(ContentType.JSON)
            .when()
                .post("/appointments/" + scheduledAppointment.getAppointmentId() + "/invoice")
            .then()
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .body("message", notNullValue());
        }

        @Test
        @DisplayName("Should return 409 when invoice already exists")
        void shouldReturn409WhenInvoiceAlreadyExists() {
            // First generation should succeed
            given()
                .contentType(ContentType.JSON)
            .when()
                .post("/appointments/" + completedAppointment.getAppointmentId() + "/invoice")
            .then()
                .statusCode(HttpStatus.CREATED.value());

            // Second generation should fail
            given()
                .contentType(ContentType.JSON)
            .when()
                .post("/appointments/" + completedAppointment.getAppointmentId() + "/invoice")
            .then()
                .statusCode(HttpStatus.CONFLICT.value())
                .body("message", notNullValue());
        }
    }

    @Nested
    @DisplayName("Get Invoice Tests")
    class GetInvoiceTests {

        @Test
        @DisplayName("Should get invoice by appointment ID")
        void shouldGetInvoiceByAppointmentId() {
            // Create invoice first
            Invoice invoice = invoiceRepository.save(Invoice.builder()
                    .appointment(completedAppointment)
                    .baseAmount(new BigDecimal("100.00"))
                    .specializationPremium(new BigDecimal("50.00"))
                    .totalAmount(new BigDecimal("150.00"))
                    .paymentStatus(PaymentStatus.PENDING)
                    .build());

            given()
                .contentType(ContentType.JSON)
            .when()
                .get("/appointments/" + completedAppointment.getAppointmentId() + "/invoice")
            .then()
                .statusCode(HttpStatus.OK.value())
                .body("appointmentId", equalTo(completedAppointment.getAppointmentId()))
                .body("totalAmount", equalTo(150.00f));
        }

        @Test
        @DisplayName("Should return 404 when invoice not found")
        void shouldReturn404WhenInvoiceNotFound() {
            given()
                .contentType(ContentType.JSON)
            .when()
                .get("/appointments/" + completedAppointment.getAppointmentId() + "/invoice")
            .then()
                .statusCode(HttpStatus.NOT_FOUND.value());
        }

        @Test
        @DisplayName("Should get all invoices for a patient")
        void shouldGetAllInvoicesForPatient() {
            // Create multiple invoices for the patient
            invoiceRepository.save(Invoice.builder()
                    .appointment(completedAppointment)
                    .baseAmount(new BigDecimal("100.00"))
                    .specializationPremium(new BigDecimal("50.00"))
                    .totalAmount(new BigDecimal("150.00"))
                    .paymentStatus(PaymentStatus.PENDING)
                    .build());

            Appointment anotherAppointment = appointmentRepository.save(Appointment.builder()
                    .patient(patient)
                    .doctor(neurologyDoctor)
                    .appointmentTime(LocalDateTime.now().plusHours(2))
                    .status(AppointmentStatus.COMPLETED)
                    .build());

            invoiceRepository.save(Invoice.builder()
                    .appointment(anotherAppointment)
                    .baseAmount(new BigDecimal("100.00"))
                    .specializationPremium(new BigDecimal("45.00"))
                    .totalAmount(new BigDecimal("145.00"))
                    .paymentStatus(PaymentStatus.PAID)
                    .build());

            given()
                .contentType(ContentType.JSON)
            .when()
                .get("/patients/" + patient.getId() + "/invoices")
            .then()
                .statusCode(HttpStatus.OK.value())
                .body("$", hasSize(2))
                .body("[0].patientName", equalTo("Jane Doe"));
        }

        @Test
        @DisplayName("Should get invoices by payment status")
        void shouldGetInvoicesByPaymentStatus() {
            // Create invoices with different statuses
            invoiceRepository.save(Invoice.builder()
                    .appointment(completedAppointment)
                    .baseAmount(new BigDecimal("100.00"))
                    .specializationPremium(new BigDecimal("50.00"))
                    .totalAmount(new BigDecimal("150.00"))
                    .paymentStatus(PaymentStatus.PENDING)
                    .build());

            given()
                .contentType(ContentType.JSON)
                .queryParam("status", "PENDING")
            .when()
                .get("/invoices")
            .then()
                .statusCode(HttpStatus.OK.value())
                .body("$", hasSize(1))
                .body("[0].paymentStatus", equalTo("PENDING"));
        }
    }

    @Nested
    @DisplayName("End-to-End Billing Workflow Tests")
    class EndToEndWorkflowTests {

        @Test
        @DisplayName("Should complete full billing workflow from appointment to invoice")
        void shouldCompleteFullBillingWorkflow() {
            // Step 1: Create a scheduled appointment
            Appointment appointment = appointmentRepository.save(Appointment.builder()
                    .patient(patient)
                    .doctor(cardiologyDoctor)
                    .appointmentTime(LocalDateTime.now())
                    .status(AppointmentStatus.SCHEDULED)
                    .build());

            // Step 2: Mark appointment as completed
            appointment.setStatus(AppointmentStatus.COMPLETED);
            appointmentRepository.save(appointment);

            // Step 3: Generate invoice
            String invoiceId = given()
                .contentType(ContentType.JSON)
            .when()
                .post("/appointments/" + appointment.getAppointmentId() + "/invoice")
            .then()
                .statusCode(HttpStatus.CREATED.value())
                .body("totalAmount", equalTo(150.00f))
                .extract()
                .path("id")
                .toString();

            // Step 4: Verify invoice can be retrieved
            given()
                .contentType(ContentType.JSON)
            .when()
                .get("/appointments/" + appointment.getAppointmentId() + "/invoice")
            .then()
                .statusCode(HttpStatus.OK.value())
                .body("id", equalTo(Integer.parseInt(invoiceId)))
                .body("paymentStatus", equalTo("PENDING"));
        }
    }
}
