# Patient Management Service

[![SCRUM-13](https://img.shields.io/badge/Jira-SCRUM--13-blue)](https://raghupardhu.atlassian.net/browse/SCRUM-13)

A Spring Boot REST API for Patient Management - Onboarding, Search & Profile Retrieval.

## 📋 Jira Epic: SCRUM-13 - Patient Management

This service implements the Patient Management Epic with the following stories:

| Story | Description | Status |
|-------|-------------|--------|
| **SCRUM-14** | Patient Onboarding API | ✅ Implemented |
| **SCRUM-15** | Patient Search & Profile Retrieval | ✅ Implemented |
| **SCRUM-16** | Patient Schema & Entity Mapping | ✅ Implemented |

## 🛠️ Technology Stack

- **Java**: 11
- **Framework**: Spring Boot 2.7.18
- **Database**: H2 (In-memory)
- **API Documentation**: Swagger/OpenAPI 3.0 (SpringDoc)
- **Build Tool**: Maven
- **Testing**: JUnit 5, Mockito, REST Assured

## 🚀 Getting Started

### Prerequisites

- Java 11 or higher
- Maven 3.6+

### Build the Application

```bash
mvn clean install
```

### Run the Application

```bash
mvn spring-boot:run
```

The application will start on `http://localhost:8080`

## 📚 API Documentation

Once the application is running, access:

- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **OpenAPI Spec**: http://localhost:8080/api-docs
- **H2 Console**: http://localhost:8080/h2-console

### H2 Database Connection

- **JDBC URL**: `jdbc:h2:mem:patientdb`
- **Username**: `sa`
- **Password**: (empty)

## 🔌 API Endpoints

### Patient Management API

| Method | Endpoint | Description | Story |
|--------|----------|-------------|-------|
| `POST` | `/api/v1/patients` | Create a new patient | SCRUM-14 |
| `GET` | `/api/v1/patients/{id}` | Get patient by ID | SCRUM-15 |
| `GET` | `/api/v1/patients` | Get all patients | SCRUM-15 |
| `PUT` | `/api/v1/patients/{id}` | Update patient | - |
| `DELETE` | `/api/v1/patients/{id}` | Delete patient | - |

### Example Requests

#### Create Patient (SCRUM-14)

```bash
curl -X POST http://localhost:8080/api/v1/patients \
  -H "Content-Type: application/json" \
  -d '{
    "firstName": "John",
    "lastName": "Doe",
    "dob": "1990-05-15",
    "email": "john.doe@example.com",
    "phone": "+1234567890"
  }'
```

**Response**: `201 Created`

#### Get Patient by ID (SCRUM-15)

```bash
curl http://localhost:8080/api/v1/patients/1
```

**Response**: `200 OK` or `404 Not Found`

## 🧪 Testing

### Run All Tests

```bash
mvn test
```

### Test Categories

| Category | Location | Description |
|----------|----------|-------------|
| **Unit Tests** | `src/test/java/.../service/` | Tests business logic in isolation |
| **Unit Tests** | `src/test/java/.../mapper/` | Tests DTO/Entity mapping |
| **Integration Tests** | `src/test/java/.../repository/` | Tests database operations |
| **Integration Tests** | `src/test/java/.../integration/` | Tests full application context |
| **API Tests** | `src/test/java/.../api/` | End-to-end REST API tests |

### Test Coverage

```bash
mvn test jacoco:report
```

Coverage report available at: `target/site/jacoco/index.html`

## 📁 Project Structure

```
src/
├── main/
│   ├── java/com/sparks/patient/
│   │   ├── PatientManagementApplication.java
│   │   ├── controller/
│   │   │   └── PatientController.java
│   │   ├── dto/
│   │   │   ├── PatientRequest.java
│   │   │   ├── PatientResponse.java
│   │   │   └── ErrorResponse.java
│   │   ├── entity/
│   │   │   └── Patient.java
│   │   ├── exception/
│   │   │   ├── PatientNotFoundException.java
│   │   │   ├── DuplicateEmailException.java
│   │   │   └── GlobalExceptionHandler.java
│   │   ├── mapper/
│   │   │   └── PatientMapper.java
│   │   ├── repository/
│   │   │   └── PatientRepository.java
│   │   └── service/
│   │       ├── PatientService.java
│   │       └── PatientServiceImpl.java
│   └── resources/
│       ├── application.yml
│       └── application-test.yml
└── test/
    └── java/com/sparks/patient/
        ├── PatientManagementApplicationTest.java
        ├── api/
        │   └── PatientApiTest.java
        ├── integration/
        │   └── PatientIntegrationTest.java
        ├── mapper/
        │   └── PatientMapperTest.java
        ├── repository/
        │   └── PatientRepositoryTest.java
        └── service/
            └── PatientServiceImplTest.java
```

## 📋 Patient Schema (SCRUM-16)

### Entity Fields

| Field | Type | Constraints |
|-------|------|-------------|
| `id` | Long | Primary Key, Auto-generated |
| `firstName` | String | Required, 2-100 chars |
| `lastName` | String | Required, 2-100 chars |
| `dob` | LocalDate | Required, Must be past date |
| `email` | String | Required, Valid email, **Unique** |
| `phone` | String | Required, Valid phone format |
| `createdAt` | LocalDateTime | Auto-generated |
| `updatedAt` | LocalDateTime | Auto-updated |

### Database Table

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
```

## 📄 License

This project is for internal use - Sparks Healthcare Platform.

---

**Author**: Sparks Development Team  
**Epic**: [SCRUM-13 - Patient Management](https://raghupardhu.atlassian.net/browse/SCRUM-13)