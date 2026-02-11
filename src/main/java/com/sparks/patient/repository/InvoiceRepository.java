package com.sparks.patient.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.sparks.patient.entity.Invoice;
import com.sparks.patient.entity.Invoice.PaymentStatus;

/**
 * Invoice Repository - SCRUM-24: Automated Billing Engine
 * 
 * Manages invoice data persistence and retrieval.
 */
@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, Long> {

    /**
     * Find invoice by appointment ID
     */
    @Query("SELECT i FROM Invoice i WHERE i.appointment.id = :appointmentId")
    Optional<Invoice> findByAppointmentId(@Param("appointmentId") Long appointmentId);

    /**
     * Find invoices by payment status
     */
    List<Invoice> findByPaymentStatus(PaymentStatus paymentStatus);

    /**
     * Find invoices by patient ID
     */
    @Query("SELECT i FROM Invoice i WHERE i.appointment.patient.id = :patientId")
    List<Invoice> findByPatientId(@Param("patientId") Long patientId);

    /**
     * Check if invoice exists for appointment
     */
    @Query("SELECT CASE WHEN COUNT(i) > 0 THEN true ELSE false END FROM Invoice i WHERE i.appointment.id = :appointmentId")
    boolean existsByAppointmentId(@Param("appointmentId") Long appointmentId);
}
