# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Patient Management Service — a Spring Boot 2.7.18 REST API (Java 11) for the Sparks Healthcare Platform. Manages patients, doctors, shifts, and appointments using an H2 in-memory database. Uses Lombok, SpringDoc OpenAPI, and JPA/Hibernate.

## Build & Run Commands

```bash
mvn clean install              # Build with all tests
mvn spring-boot:run            # Run on localhost:8080
mvn test                       # Run all tests (~193 tests)
mvn test -Pfast                # Unit tests only, 4 parallel threads (~45s)
mvn test -Punit                # Unit tests only (sequential)
mvn test -Pintegration         # Integration tests only
mvn test -Pservice             # Service layer tests only
mvn test -Prepository          # Repository tests only
mvn test -Papi                 # API/controller tests only
mvn test -Dtest=PatientServiceImplTest                          # Single test class
mvn test -Dtest=PatientServiceImplTest#shouldCreatePatientSuccessfully  # Single test method
mvn test jacoco:report         # Generate coverage report at target/site/jacoco/index.html
```

## Architecture

Three-layer architecture applied consistently across all four domains (Patient, Doctor, Shift, Appointment):

```
Controller (@RestController) → Service (interface + Impl) → Repository (JpaRepository) → H2
     ↕                              ↕                             ↕
  Request/Response DTOs          Mapper (@Component)           Entity (@Entity)
```

**All domains follow the same pattern:**
- **Controller**: REST endpoints with `@Valid` request validation
- **Service**: Interface + `Impl` class. `@Transactional` on writes, `@Transactional(readOnly=true)` on reads
- **Mapper**: `@Component` with `toEntity()`, `toResponse()`, `updateEntity()` methods
- **Repository**: Extends `JpaRepository<Entity, Long>` with custom query methods where needed
- **DTOs**: Request DTOs have JSR-303 validation + `@Schema` for OpenAPI; Response DTOs include timestamps
- **Exceptions**: Domain-specific exceptions (e.g., `PatientNotFoundException`, `DuplicateEmailException`) handled by a centralized `GlobalExceptionHandler` (`@RestControllerAdvice`) returning `ErrorResponse`

**Cross-cutting conventions:**
- Entities use `@PrePersist`/`@PreUpdate` for auto-managing `createdAt`/`updatedAt` timestamps
- IDs are `Long` type throughout
- Lombok: `@Data`, `@Builder`, `@NoArgsConstructor`, `@AllArgsConstructor` on DTOs and entities
- Logging via `@Slf4j`

## API Endpoints

| Prefix | Domain |
|--------|--------|
| `/api/v1/patients` | Patient CRUD |
| `/api/v1/doctors` | Doctor CRUD |
| `/api/v1/shifts` | Shift CRUD (with time-slot conflict detection) |
| `/api/appointments` | Appointment creation and retrieval (no update/delete) |

Swagger UI: `http://localhost:8080/swagger-ui.html` | H2 Console: `http://localhost:8080/h2-console` (JDBC URL: `jdbc:h2:mem:patientdb`, user: `sa`, no password)

## Testing Conventions

- **`@UnitTest`** (custom annotation, tag=`unit`): Fast tests using `MockitoExtension`, no Spring context. Use `@Nested` classes for grouping.
- **`@IntegrationTest`** (custom annotation, tag=`integration`): Full Spring context with `@SpringBootTest`, real H2 database, `@DirtiesContext` for isolation.
- **API tests**: Use REST Assured for HTTP-level testing.
- Test config: `application-test.yml` uses random port and WARN-level logging.

## Key Domain Logic

- **Shift conflicts**: Service layer prevents overlapping shifts for the same doctor using custom JPQL queries (`findConflictingShifts`, `findConflictingShiftsExcluding`). Entity has `isValidTimeSlot()` validation.
- **Appointment**: Uses UUID (generated in `@PrePersist`), enum status (`SCHEDULED`, `CONFIRMED`, `COMPLETED`, `CANCELLED`), lazy-loaded relationships to Patient and Doctor.
- **Uniqueness constraints**: Patient email and Doctor license number must be unique (enforced at service and DB level).

## Package Structure

All source code under `com.sparks.patient` with sub-packages: `controller`, `service`, `repository`, `entity`, `dto`, `mapper`, `exception`, `config`, `test` (custom annotations).
