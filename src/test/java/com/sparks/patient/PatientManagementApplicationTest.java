package com.sparks.patient;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

import com.sparks.patient.controller.PatientController;
import com.sparks.patient.repository.PatientRepository;
import com.sparks.patient.service.PatientService;

/**
 * Application Context Tests
 * Verifies the Spring Boot application loads correctly
 */
@SpringBootTest
@DisplayName("Patient Management Application Tests")
class PatientManagementApplicationTest {

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private PatientController patientController;

    @Autowired
    private PatientService patientService;

    @Autowired
    private PatientRepository patientRepository;

    @Test
    @DisplayName("Application context loads successfully")
    void contextLoads() {
        assertThat(applicationContext).isNotNull();
    }

    @Test
    @DisplayName("PatientController bean is loaded")
    void patientControllerLoads() {
        assertThat(patientController).isNotNull();
    }

    @Test
    @DisplayName("PatientService bean is loaded")
    void patientServiceLoads() {
        assertThat(patientService).isNotNull();
    }

    @Test
    @DisplayName("PatientRepository bean is loaded")
    void patientRepositoryLoads() {
        assertThat(patientRepository).isNotNull();
    }
}
