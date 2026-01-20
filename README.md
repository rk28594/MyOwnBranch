# Patient Management System

A secure core engine to manage patient records and doctor schedules with JPA, H2 database, and automated testing.

## Features

- **Patient Management**: Full CRUD operations for patient records
- **JPA Integration**: Uses Spring Data JPA with H2 in-memory database
- **Automated Testing**: Comprehensive test suite including unit, repository, and API integration tests
- **REST API**: RESTful endpoints for patient operations
- **Data Validation**: Built-in validation for patient data integrity

## Patient Data Model

| Field Name | JPA Annotation | Data Type | Business Purpose |
|------------|----------------|-----------|------------------|
| id | @Id, @GeneratedValue | Long | Medical Record Number (MRN) |
| firstName | @Column | String | Legal first name of the patient |
| lastName | @Column | String | Legal last name of the patient |
| dateOfBirth | @Column | LocalDate | Used to calculate age and verify identity |
| gender | @Enumerated | Enum | Required for clinical protocols |
| admittedDate | @CreationTimestamp | LocalDateTime | Automatically logs when patient arrived |
| department | @Column | String | E.g., Cardiology, Pediatrics, ER |
| isCritical | @Column | Boolean | Flag to alert staff of high-priority cases |

## Project Structure

```
├── src/
│   ├── main/
│   │   ├── java/com/hospital/
│   │   │   ├── PatientManagementApplication.java
│   │   │   ├── controller/
│   │   │   │   └── PatientController.java
│   │   │   ├── model/
│   │   │   │   └── Patient.java
│   │   │   ├── repository/
│   │   │   │   └── PatientRepository.java
│   │   │   └── service/
│   │   │       └── PatientService.java
│   │   └── resources/
│   │       ├── application.properties
│   │       └── data.sql
│   └── test/
│       └── java/com/hospital/
│           ├── controller/
│           │   └── PatientControllerTest.java
│           ├── model/
│           │   └── PatientTest.java
│           └── repository/
│               └── PatientRepositoryTest.java
└── pom.xml
```

## Getting Started

### Prerequisites

- Java 17 or higher
- Maven 3.6+

### Build the Project

```bash
mvn clean install
```

### Run Tests

```bash
mvn test
```

### Run the Application

```bash
mvn spring-boot:run
```

The application will start on `http://localhost:8080`

## API Endpoints

### Patient Operations

- **GET** `/api/v1/patients` - Get all patients
- **GET** `/api/v1/patients/{id}` - Get patient by ID
- **POST** `/api/v1/patients` - Create new patient
- **PUT** `/api/v1/patients/{id}` - Update patient
- **DELETE** `/api/v1/patients/{id}` - Delete patient
- **GET** `/api/v1/patients/critical` - Get all critical patients
- **GET** `/api/v1/patients/department/{department}` - Get patients by department

### Example Request

```bash
# Create a new patient
curl -X POST http://localhost:8080/api/v1/patients \
  -H "Content-Type: application/json" \
  -d '{
    "firstName": "Jane",
    "lastName": "Wilson",
    "dateOfBirth": "1988-06-15",
    "gender": "FEMALE",
    "department": "Cardiology",
    "isCritical": false
  }'
```

## H2 Console

Access the H2 database console at: `http://localhost:8080/h2-console`

- **JDBC URL**: `jdbc:h2:mem:hospitaldb`
- **Username**: `sa`
- **Password**: (leave empty)

## Testing Suite

### Unit Tests
- Tests for Patient model validation
- Age calculation and validation
- Gender enum functionality

### Repository Tests (@DataJpaTest)
- Database CRUD operations
- Custom query methods
- H2 integration

### API Integration Tests (MockMvc)
- HTTP endpoint testing
- Request/response validation
- Error handling

## Sample Data

The application comes pre-loaded with sample patients:

1. **John Doe** - Male, Cardiology, Non-critical
2. **Sarah Smith** - Female, Emergency, Critical
3. **Michael Johnson** - Male, Pediatrics, Non-critical
4. **Emily Davis** - Female, Cardiology, Critical
5. **Robert Brown** - Male, Orthopedics, Non-critical

## Technologies Used

- **Spring Boot 3.2.1** - Application framework
- **Spring Data JPA** - Data persistence
- **H2 Database** - In-memory database
- **JUnit 5** - Testing framework
- **MockMvc** - API testing
- **Maven** - Build tool

## Development

This project follows best practices for:
- Clean code architecture
- Comprehensive testing
- RESTful API design
- Data validation
- Error handling

## License

This project is developed for educational and demonstration purposes.
