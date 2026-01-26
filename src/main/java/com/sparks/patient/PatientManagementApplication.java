package com.sparks.patient;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;

/**
 * Main Spring Boot Application for Patient Management Service.
 * SCRUM-13: Patient Management Epic
 */
@SpringBootApplication
@OpenAPIDefinition(
    info = @Info(
        title = "Patient Management API",
        version = "1.0.0",
        description = "REST API for Patient Management - Onboarding, Search & Profile Retrieval",
        contact = @Contact(
            name = "Sparks Team",
            email = "support@sparks.com"
        )
    )
)
public class PatientManagementApplication {

    public static void main(String[] args) {
        SpringApplication.run(PatientManagementApplication.class, args);
    }
}
