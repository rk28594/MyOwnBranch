# Hospital Management System

A Spring Boot application for managing hospital operations including patients, doctors, appointments, and billing.

## Project Structure

- **Epic 1: Patient Management** (SCRUM-13)
  - Story 1.1: Patient Schema & Entity Mapping (SCRUM-16)
  - Story 1.2: Patient Onboarding API (SCRUM-14)
  - Story 1.3: Patient Search & Profile Retrieval (SCRUM-15)

- **Epic 2: Medical Staff & Resource Management** (SCRUM-17)
  - Story 2.1: Doctor Profile Management (SCRUM-20)
  - Story 2.2: Shift Definition & Time-Slot Logic (SCRUM-18)
  - Story 2.3: Shift Conflict Validator (SCRUM-19)

- **Epic 3: Appointment Booking & Billing** (SCRUM-21)
  - Story 3.1: Appointment Request Workflow (SCRUM-23)
  - Story 3.2: Availability Guardrail (SCRUM-25)
  - Story 3.3: Appointment Completion & Status Update (SCRUM-22)
  - Story 3.4: Automated Billing Engine (SCRUM-24)

## Tech Stack

- Java 17
- Spring Boot 3.2.1
- Spring Data JPA
- H2 Database
- Lombok
- Maven

## Running the Application

```bash
mvn spring-boot:run
```

## H2 Console

Access the H2 console at: http://localhost:8080/h2-console
- JDBC URL: jdbc:h2:mem:hospitaldb
- Username: sa
- Password: (leave empty)

## API Endpoints

### Patient Management
- POST /api/v1/patients - Create a new patient
- GET /api/v1/patients/{id} - Get patient by ID
- GET /api/v1/patients - Get all patients
- PUT /api/v1/patients/{id} - Update patient
- DELETE /api/v1/patients/{id} - Delete patient
