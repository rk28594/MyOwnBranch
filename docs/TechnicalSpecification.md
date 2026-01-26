# Technical Specification Document

## Patient Management Service

| Document Info | |
|--------------|---|
| **Version** | 1.0 |
| **Date** | January 26, 2026 |
| **Epic** | [SCRUM-13 - Patient Management](https://raghupardhu.atlassian.net/browse/SCRUM-13) |
| **Status** | In Review |

---

## 1. System Overview

The Patient Management Service is a Spring Boot-based microservice that provides REST APIs for managing patient records in the Sparks Healthcare Platform.

### 1.1 Technology Stack

| Component | Technology | Version |
|-----------|------------|---------|
| Language | Java | 11 |
| Framework | Spring Boot | 2.7.18 |
| Database | H2 (In-memory) | - |
| ORM | Spring Data JPA / Hibernate | - |
| API Documentation | SpringDoc OpenAPI | 3.0 |
| Build Tool | Maven | 3.6+ |
| Testing | JUnit 5, Mockito, REST Assured | - |

---

## 2. Architecture

### 2.1 Component Diagram

```
┌─────────────────────────────────────────────────────────────┐
│                    Patient Management Service                │
├─────────────────────────────────────────────────────────────┤
│  ┌─────────────┐    ┌─────────────┐    ┌─────────────┐     │
│  │  Controller │───▶│   Service   │───▶│ Repository  │     │
│  │    Layer    │    │    Layer    │    │    Layer    │     │
│  └─────────────┘    └─────────────┘    └─────────────┘     │
│         │                  │                  │             │
│         ▼                  ▼                  ▼             │
│  ┌─────────────┐    ┌─────────────┐    ┌─────────────┐     │
│  │     DTO     │    │   Mapper    │    │   Entity    │     │
│  │   (Request/ │    │  (Patient   │    │  (Patient)  │     │
│  │   Response) │    │   Mapper)   │    │             │     │
│  └─────────────┘    └─────────────┘    └─────────────┘     │
│                                               │             │
└───────────────────────────────────────────────┼─────────────┘
                                                │
                                                ▼
                                        ┌─────────────┐
                                        │  H2 Database │
                                        └─────────────┘
```

### 2.2 Package Structure

```
com.sparks.patient/
├── PatientManagementApplication.java    # Main entry point
├── controller/
│   └── PatientController.java           # REST endpoints
├── dto/
│   ├── PatientRequest.java              # Input DTO
│   ├── PatientResponse.java             # Output DTO
│   └── ErrorResponse.java               # Error DTO
├── entity/
│   └── Patient.java                     # JPA Entity
├── exception/
│   ├── PatientNotFoundException.java    # 404 exception
│   ├── DuplicateEmailException.java     # 409 exception
│   └── GlobalExceptionHandler.java      # Exception handler
├── mapper/
│   └── PatientMapper.java               # Entity ↔ DTO mapper
├── repository/
│   └── PatientRepository.java           # Data access
└── service/
    ├── PatientService.java              # Service interface
    └── PatientServiceImpl.java          # Service implementation
```

---

## 3. API Specification

### 3.1 Base URL

```
http://localhost:8080/api/v1/patients
```

### 3.2 Endpoints

#### 3.2.1 Create Patient (SCRUM-14)

```http
POST /api/v1/patients
Content-Type: application/json
```

**Request Body:**
```json
{
  "firstName": "John",
  "lastName": "Doe",
  "dob": "1990-05-15",
  "email": "john.doe@example.com",
  "phone": "+1234567890"
}
```

**Response (201 Created):**
```json
{
  "id": 1,
  "firstName": "John",
  "lastName": "Doe",
  "dob": "1990-05-15",
  "email": "john.doe@example.com",
  "phone": "+1234567890",
  "createdAt": "2026-01-26T10:30:00",
  "updatedAt": "2026-01-26T10:30:00"
}
```

**Error Response (400 Bad Request):**
```json
{
  "timestamp": "2026-01-26T10:30:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "errors": [
    {
      "field": "email",
      "message": "Email must be a valid email address"
    }
  ]
}
```

---

#### 3.2.2 Get Patient by ID (SCRUM-15)

```http
GET /api/v1/patients/{id}
```

**Response (200 OK):**
```json
{
  "id": 1,
  "firstName": "John",
  "lastName": "Doe",
  "dob": "1990-05-15",
  "email": "john.doe@example.com",
  "phone": "+1234567890",
  "createdAt": "2026-01-26T10:30:00",
  "updatedAt": "2026-01-26T10:30:00"
}
```

**Error Response (404 Not Found):**
```json
{
  "timestamp": "2026-01-26T10:30:00",
  "status": 404,
  "error": "Not Found",
  "message": "Patient not found with id: 999"
}
```

---

#### 3.2.3 Get All Patients

```http
GET /api/v1/patients
```

**Response (200 OK):**
```json
[
  {
    "id": 1,
    "firstName": "John",
    "lastName": "Doe",
    "dob": "1990-05-15",
    "email": "john.doe@example.com",
    "phone": "+1234567890",
    "createdAt": "2026-01-26T10:30:00",
    "updatedAt": "2026-01-26T10:30:00"
  }
]
```

---

#### 3.2.4 Update Patient

```http
PUT /api/v1/patients/{id}
Content-Type: application/json
```

**Request Body:** Same as Create Patient

**Response (200 OK):** PatientResponse with updated data

---

#### 3.2.5 Delete Patient

```http
DELETE /api/v1/patients/{id}
```

**Response (204 No Content)**

---

## 4. Data Model

### 4.1 Entity: Patient (SCRUM-16)

```java
@Entity
@Table(name = "patients")
public class Patient {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 100)
    private String lastName;

    @Column(name = "dob", nullable = false)
    private LocalDate dob;

    @Column(name = "email", nullable = false, unique = true, length = 255)
    private String email;

    @Column(name = "phone", nullable = false, length = 20)
    private String phone;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
```

### 4.2 Database Schema

```sql
CREATE TABLE patients (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    dob DATE NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    phone VARCHAR(20) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP
);

CREATE UNIQUE INDEX idx_patients_email ON patients(email);
```

### 4.3 Validation Rules

| Field | Annotation | Rule |
|-------|------------|------|
| firstName | `@NotBlank`, `@Size(min=2, max=100)` | Required, 2-100 chars |
| lastName | `@NotBlank`, `@Size(min=2, max=100)` | Required, 2-100 chars |
| dob | `@NotNull`, `@Past` | Required, past date |
| email | `@NotBlank`, `@Email` | Required, valid email |
| phone | `@NotBlank`, `@Pattern` | Required, E.164 format |

---

## 5. Exception Handling

### 5.1 Custom Exceptions

| Exception | HTTP Status | Scenario |
|-----------|-------------|----------|
| `PatientNotFoundException` | 404 | Patient ID not found |
| `DuplicateEmailException` | 409 | Email already registered |
| `MethodArgumentNotValidException` | 400 | Validation failure |

### 5.2 Global Exception Handler

```java
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(PatientNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(PatientNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(ErrorResponse.builder()
                .status(404)
                .error("Not Found")
                .message(ex.getMessage())
                .build());
    }
    
    @ExceptionHandler(DuplicateEmailException.class)
    public ResponseEntity<ErrorResponse> handleConflict(DuplicateEmailException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
            .body(ErrorResponse.builder()
                .status(409)
                .error("Conflict")
                .message(ex.getMessage())
                .build());
    }
}
```

---

## 6. Configuration

### 6.1 Application Properties (application.yml)

```yaml
server:
  port: 8080

spring:
  datasource:
    url: jdbc:h2:mem:patientdb
    driver-class-name: org.h2.Driver
    username: sa
    password:
  
  h2:
    console:
      enabled: true
      path: /h2-console
  
  jpa:
    hibernate:
      ddl-auto: create-drop
    show-sql: true

springdoc:
  api-docs:
    path: /api-docs
  swagger-ui:
    path: /swagger-ui.html
```

---

## 7. Testing Strategy

### 7.1 Test Pyramid

| Level | Location | Framework | Coverage |
|-------|----------|-----------|----------|
| Unit | `service/`, `mapper/` | JUnit 5, Mockito | Business logic |
| Integration | `repository/` | Spring Boot Test, H2 | Data access |
| API/E2E | `api/` | REST Assured | End-to-end |

### 7.2 Test Classes

| Class | Type | Description |
|-------|------|-------------|
| `PatientServiceImplTest` | Unit | Tests service layer logic |
| `PatientMapperTest` | Unit | Tests DTO/Entity mapping |
| `PatientRepositoryTest` | Integration | Tests JPA repository |
| `PatientIntegrationTest` | Integration | Tests full Spring context |
| `PatientApiTest` | E2E | Tests REST endpoints |

### 7.3 Running Tests

```bash
# Run all tests
mvn test

# Run with coverage report
mvn test jacoco:report

# View coverage report
open target/site/jacoco/index.html
```

---

## 8. Build & Deployment

### 8.1 Build Commands

```bash
# Clean build
mvn clean install

# Skip tests
mvn clean install -DskipTests

# Package as JAR
mvn clean package
```

### 8.2 Run Locally

```bash
# Using Maven
mvn spring-boot:run

# Using JAR
java -jar target/patient-management-*.jar
```

### 8.3 Service URLs

| URL | Description |
|-----|-------------|
| http://localhost:8080/api/v1/patients | Patient API |
| http://localhost:8080/swagger-ui.html | Swagger UI |
| http://localhost:8080/api-docs | OpenAPI Spec |
| http://localhost:8080/h2-console | H2 Database Console |

---

## 9. Dependencies

### 9.1 Key Dependencies (pom.xml)

```xml
<dependencies>
    <!-- Spring Boot Starters -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-jpa</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-validation</artifactId>
    </dependency>
    
    <!-- Database -->
    <dependency>
        <groupId>com.h2database</groupId>
        <artifactId>h2</artifactId>
        <scope>runtime</scope>
    </dependency>
    
    <!-- API Documentation -->
    <dependency>
        <groupId>org.springdoc</groupId>
        <artifactId>springdoc-openapi-ui</artifactId>
    </dependency>
    
    <!-- Utilities -->
    <dependency>
        <groupId>org.projectlombok</groupId>
        <artifactId>lombok</artifactId>
    </dependency>
</dependencies>
```

---

## 10. Future Enhancements

| Feature | Priority | Notes |
|---------|----------|-------|
| Pagination | High | Add pageable support for getAllPatients |
| Search filters | Medium | Search by name, email, phone |
| Audit logging | Medium | Track all CRUD operations |
| Database migration | High | Replace H2 with PostgreSQL |
| API versioning | Low | Support multiple API versions |

---

## 11. Appendix

### 11.1 Related Documents

- [Functional Specification](./FunctionalSpecification.md)
- [README](../README.md)
- [OpenAPI Spec](http://localhost:8080/api-docs)

### 11.2 Jira References

| Key | Summary |
|-----|---------|
| [SCRUM-13](https://raghupardhu.atlassian.net/browse/SCRUM-13) | Patient Management Epic |
| [SCRUM-14](https://raghupardhu.atlassian.net/browse/SCRUM-14) | Patient Onboarding API |
| [SCRUM-15](https://raghupardhu.atlassian.net/browse/SCRUM-15) | Patient Search & Profile Retrieval |
| [SCRUM-16](https://raghupardhu.atlassian.net/browse/SCRUM-16) | Patient Schema & Entity Mapping |

### 11.3 Document History

| Version | Date | Author | Changes |
|---------|------|--------|---------|
| 1.0 | 2026-01-26 | Sparks Team | Initial version |
