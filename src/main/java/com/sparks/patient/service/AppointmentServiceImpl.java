package com.sparks.patient.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sparks.patient.dto.AppointmentRequest;
import com.sparks.patient.dto.AppointmentResponse;
import com.sparks.patient.entity.Appointment;
import com.sparks.patient.entity.Doctor;
import com.sparks.patient.entity.Patient;
import com.sparks.patient.exception.AppointmentNotFoundException;
import com.sparks.patient.exception.DoctorNotFoundException;
import com.sparks.patient.exception.PatientNotFoundException;
import com.sparks.patient.mapper.AppointmentMapper;
import com.sparks.patient.repository.AppointmentRepository;
import com.sparks.patient.repository.DoctorRepository;
import com.sparks.patient.repository.PatientRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Appointment Service Implementation - SCRUM-23: Appointment Request Workflow
 * 
 * Creates the basic appointment record (Patient + Doctor + Time)
 * Stores appointment with status SCHEDULED
 * Generates an Appointment UUID when a valid request is sent
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AppointmentServiceImpl implements AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    private final AppointmentMapper appointmentMapper;

    @Override
    public AppointmentResponse createAppointment(AppointmentRequest request) {
        log.info("Creating appointment for patient {} with doctor {} at {}", 
                 request.getPatientId(), request.getDoctorId(), request.getAppointmentTime());
        
        // Validate patient exists
        Patient patient = patientRepository.findById(request.getPatientId())
                .orElseThrow(() -> new PatientNotFoundException(
                        "Patient not found with id: " + request.getPatientId()));
        
        // Validate doctor exists
        Doctor doctor = doctorRepository.findById(request.getDoctorId())
                .orElseThrow(() -> new DoctorNotFoundException(
                        "Doctor not found with id: " + request.getDoctorId()));
        
        // Create appointment entity
        Appointment appointment = appointmentMapper.toEntity(request, patient, doctor);
        
        // Save appointment
        Appointment savedAppointment = appointmentRepository.save(appointment);
        
        log.info("Appointment created successfully with UUID: {}", savedAppointment.getAppointmentId());
        
        return appointmentMapper.toResponse(savedAppointment);
    }

    @Override
    @Transactional(readOnly = true)
    public AppointmentResponse getAppointmentById(String appointmentId) {
        log.info("Fetching appointment with UUID: {}", appointmentId);
        
        Appointment appointment = appointmentRepository.findByAppointmentId(appointmentId)
                .orElseThrow(() -> new AppointmentNotFoundException(
                        "Appointment not found with id: " + appointmentId));
        
        return appointmentMapper.toResponse(appointment);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AppointmentResponse> getAppointmentsByPatientId(Long patientId) {
        log.info("Fetching appointments for patient: {}", patientId);
        
        // Verify patient exists
        if (!patientRepository.existsById(patientId)) {
            throw new PatientNotFoundException("Patient not found with id: " + patientId);
        }
        
        return appointmentRepository.findByPatientId(patientId).stream()
                .map(appointmentMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<AppointmentResponse> getAppointmentsByDoctorId(Long doctorId) {
        log.info("Fetching appointments for doctor: {}", doctorId);
        
        // Verify doctor exists
        if (!doctorRepository.existsById(doctorId)) {
            throw new DoctorNotFoundException("Doctor not found with id: " + doctorId);
        }
        
        return appointmentRepository.findByDoctorId(doctorId).stream()
                .map(appointmentMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<AppointmentResponse> getAllAppointments() {
        log.info("Fetching all appointments");
        
        return appointmentRepository.findAll().stream()
                .map(appointmentMapper::toResponse)
                .collect(Collectors.toList());
    }
}
