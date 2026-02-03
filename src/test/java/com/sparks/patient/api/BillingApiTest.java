package com.sparks.patient.api;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sparks.patient.controller.BillingController;
import com.sparks.patient.dto.InvoiceResponse;
import com.sparks.patient.entity.Invoice.PaymentStatus;
import com.sparks.patient.exception.AppointmentNotFoundException;
import com.sparks.patient.exception.InvoiceAlreadyExistsException;
import com.sparks.patient.exception.InvoiceNotFoundException;
import com.sparks.patient.service.BillingService;
import com.sparks.patient.test.UnitTest;

/**
 * API tests for Billing Controller - SCRUM-24: Automated Billing Engine
 */
@WebMvcTest(BillingController.class)
@UnitTest
@DisplayName("Billing API Tests")
class BillingApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private BillingService billingService;

    private InvoiceResponse invoiceResponse;

    @BeforeEach
    void setUp() {
        invoiceResponse = InvoiceResponse.builder()
                .id(1L)
                .appointmentId("appt-123")
                .appointmentDbId(1L)
                .totalAmount(new BigDecimal("150.00"))
                .baseAmount(new BigDecimal("100.00"))
                .specializationPremium(new BigDecimal("50.00"))
                .paymentStatus(PaymentStatus.PENDING)
                .patientName("John Doe")
                .doctorName("Dr. Smith")
                .specialization("Cardiology")
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Nested
    @DisplayName("Generate Invoice Tests")
    class GenerateInvoiceTests {

        @Test
        @DisplayName("POST /api/v1/billing/appointments/{id}/invoice - Should generate invoice")
        void testGenerateInvoice_Success() throws Exception {
            // Given
            when(billingService.generateInvoiceForAppointment(anyString()))
                    .thenReturn(invoiceResponse);

            // When/Then
            mockMvc.perform(post("/api/v1/billing/appointments/appt-123/invoice")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.appointmentId").value("appt-123"))
                    .andExpect(jsonPath("$.totalAmount").value(150.00))
                    .andExpect(jsonPath("$.baseAmount").value(100.00))
                    .andExpect(jsonPath("$.specializationPremium").value(50.00))
                    .andExpect(jsonPath("$.paymentStatus").value("PENDING"))
                    .andExpect(jsonPath("$.patientName").value("John Doe"))
                    .andExpect(jsonPath("$.doctorName").value("Dr. Smith"))
                    .andExpect(jsonPath("$.specialization").value("Cardiology"));
        }

        @Test
        @DisplayName("POST /api/v1/billing/appointments/{id}/invoice - Should return 404 when appointment not found")
        void testGenerateInvoice_AppointmentNotFound() throws Exception {
            // Given
            when(billingService.generateInvoiceForAppointment(anyString()))
                    .thenThrow(new AppointmentNotFoundException("Appointment not found with id: invalid-id"));

            // When/Then
            mockMvc.perform(post("/api/v1/billing/appointments/invalid-id/invoice")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message").value("Appointment not found with id: invalid-id"));
        }

        @Test
        @DisplayName("POST /api/v1/billing/appointments/{id}/invoice - Should return 409 when invoice already exists")
        void testGenerateInvoice_InvoiceAlreadyExists() throws Exception {
            // Given
            when(billingService.generateInvoiceForAppointment(anyString()))
                    .thenThrow(new InvoiceAlreadyExistsException("Invoice already exists for appointment: appt-123"));

            // When/Then
            mockMvc.perform(post("/api/v1/billing/appointments/appt-123/invoice")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.message").value("Invoice already exists for appointment: appt-123"));
        }

        @Test
        @DisplayName("POST /api/v1/billing/appointments/{id}/invoice - Should return 400 when appointment not completed")
        void testGenerateInvoice_AppointmentNotCompleted() throws Exception {
            // Given
            when(billingService.generateInvoiceForAppointment(anyString()))
                    .thenThrow(new IllegalStateException("Cannot generate invoice for appointment that is not completed"));

            // When/Then
            mockMvc.perform(post("/api/v1/billing/appointments/appt-123/invoice")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("Cannot generate invoice for appointment that is not completed"));
        }
    }

    @Nested
    @DisplayName("Get Invoice Tests")
    class GetInvoiceTests {

        @Test
        @DisplayName("GET /api/v1/billing/appointments/{id}/invoice - Should get invoice by appointment ID")
        void testGetInvoiceByAppointment_Success() throws Exception {
            // Given
            when(billingService.getInvoiceByAppointmentId(anyString()))
                    .thenReturn(invoiceResponse);

            // When/Then
            mockMvc.perform(get("/api/v1/billing/appointments/appt-123/invoice")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.appointmentId").value("appt-123"))
                    .andExpect(jsonPath("$.totalAmount").value(150.00));
        }

        @Test
        @DisplayName("GET /api/v1/billing/appointments/{id}/invoice - Should return 404 when invoice not found")
        void testGetInvoiceByAppointment_NotFound() throws Exception {
            // Given
            when(billingService.getInvoiceByAppointmentId(anyString()))
                    .thenThrow(new InvoiceNotFoundException("Invoice not found for appointment: appt-123"));

            // When/Then
            mockMvc.perform(get("/api/v1/billing/appointments/appt-123/invoice")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message").value("Invoice not found for appointment: appt-123"));
        }

        @Test
        @DisplayName("GET /api/v1/billing/patients/{id}/invoices - Should get all invoices for patient")
        void testGetInvoicesByPatient_Success() throws Exception {
            // Given
            List<InvoiceResponse> invoices = Arrays.asList(invoiceResponse);
            when(billingService.getInvoicesByPatientId(1L)).thenReturn(invoices);

            // When/Then
            mockMvc.perform(get("/api/v1/billing/patients/1/invoices")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(1)))
                    .andExpect(jsonPath("$[0].patientName").value("John Doe"));
        }

        @Test
        @DisplayName("GET /api/v1/billing/invoices?status=PENDING - Should get invoices by status")
        void testGetInvoicesByStatus_Success() throws Exception {
            // Given
            List<InvoiceResponse> invoices = Arrays.asList(invoiceResponse);
            when(billingService.getInvoicesByPaymentStatus(PaymentStatus.PENDING))
                    .thenReturn(invoices);

            // When/Then
            mockMvc.perform(get("/api/v1/billing/invoices")
                    .param("status", "PENDING")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(1)))
                    .andExpect(jsonPath("$[0].paymentStatus").value("PENDING"));
        }
    }
}
