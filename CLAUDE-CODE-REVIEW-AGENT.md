# CLAUDE-CODE-REVIEW-AGENT.md

This file provides specialized guidance for the **Code Review Agent** — responsible for ensuring code quality, architectural consistency, and adherence to project standards.

## Agent Role & Responsibilities

**Primary Mission**: Maintain code quality and architectural consistency across the codebase

**Core Tasks**:
- ✅ Review code changes for architectural compliance
- ✅ Verify naming conventions and coding standards
- ✅ Check error handling and exception patterns
- ✅ Validate proper use of annotations
- ✅ Review test coverage and quality
- ✅ Identify security vulnerabilities
- ✅ Ensure documentation completeness
- ⛔ **DO NOT**: Implement features (Coding Agent's job)
- ⛔ **DO NOT**: Write tests (Testing Agent's job)

---

## Code Review Checklist

Use this comprehensive checklist for every code review:

### 1. Architecture & Design ✅

#### Three-Layer Pattern Compliance
- [ ] Controller → Service → Repository pattern followed
- [ ] No business logic in controllers (only REST concerns)
- [ ] No repository calls directly from controllers
- [ ] Service layer handles all business logic
- [ ] DTOs used for request/response (no entities exposed)

#### Separation of Concerns
- [ ] Entities separate from DTOs
- [ ] Mapper handles all conversions
- [ ] No data access logic in service layer
- [ ] No presentation logic in service layer

#### Code Organization
- [ ] Files in correct packages (`controller`, `service`, `repository`, `entity`, `dto`, `mapper`, `exception`)
- [ ] Interface and implementation pattern for services
- [ ] Consistent naming conventions across all layers

**Red Flags**:
- ❌ Controllers calling repositories directly
- ❌ Service methods returning entities instead of DTOs
- ❌ Business logic in controllers
- ❌ Manual object mapping in services (should use mapper)

---

### 2. Annotations & Framework Usage ✅

#### Entity Annotations
- [ ] `@Entity` present
- [ ] `@Table(name = "...")` with explicit table name
- [ ] `@Id` with `@GeneratedValue(strategy = IDENTITY)`
- [ ] `@Column(nullable = false)` for required fields
- [ ] `@Column(unique = true)` for unique constraints
- [ ] `@PrePersist` and `@PreUpdate` for timestamp management
- [ ] Lombok: `@Data`, `@Builder`, `@NoArgsConstructor`, `@AllArgsConstructor`

#### Request DTO Annotations
- [ ] JSR-303 validation annotations (`@NotBlank`, `@NotNull`, `@Email`, `@Size`, `@Pattern`)
- [ ] Meaningful `message` attributes on validation annotations
- [ ] `@Schema` for OpenAPI with `description` and `example`
- [ ] Lombok: `@Data`, `@Builder`, `@NoArgsConstructor`, `@AllArgsConstructor`

#### Response DTO Annotations
- [ ] `@Schema` for OpenAPI documentation
- [ ] Lombok: `@Data`, `@Builder`, `@NoArgsConstructor`, `@AllArgsConstructor`
- [ ] No validation annotations (not needed for responses)

#### Service Annotations
- [ ] `@Service` on implementation class
- [ ] `@Transactional` on write operations (create, update, delete)
- [ ] `@Transactional(readOnly = true)` on read operations
- [ ] `@RequiredArgsConstructor` for dependency injection
- [ ] `@Slf4j` for logging

#### Controller Annotations
- [ ] `@RestController` present
- [ ] `@RequestMapping` with API path
- [ ] `@RequiredArgsConstructor` for dependency injection
- [ ] `@Validated` for method-level validation
- [ ] `@Slf4j` for logging
- [ ] `@Valid` on `@RequestBody` parameters
- [ ] OpenAPI annotations: `@Tag`, `@Operation`, `@ApiResponses`

#### Repository Annotations
- [ ] `@Repository` present
- [ ] Extends `JpaRepository<Entity, Long>`
- [ ] `@Query` with `@Param` for custom queries

**Red Flags**:
- ❌ Missing `@Transactional` on write operations
- ❌ Missing `@Valid` on controller request bodies
- ❌ Using `readOnly = true` on write operations
- ❌ Missing validation annotations on request DTOs
- ❌ Missing OpenAPI documentation

---

### 3. Error Handling & Exceptions ✅

#### Exception Design
- [ ] Domain-specific exception classes created
- [ ] Exceptions extend `RuntimeException`
- [ ] Exceptions placed in `com.sparks.patient.exception` package
- [ ] Meaningful exception names (e.g., `PatientNotFoundException`, `DuplicateEmailException`)

#### Exception Handling
- [ ] Service layer throws domain exceptions
- [ ] `GlobalExceptionHandler` handles all custom exceptions
- [ ] Proper HTTP status codes returned (404, 409, 400, 500)
- [ ] `ErrorResponse` used for error responses
- [ ] Error messages are user-friendly and informative

#### Validation Handling
- [ ] `@Valid` triggers validation in controllers
- [ ] Validation errors return 400 Bad Request
- [ ] Validation error messages are clear
- [ ] `MethodArgumentNotValidException` handled by `GlobalExceptionHandler`

**Red Flags**:
- ❌ Catching and swallowing exceptions
- ❌ Returning null instead of throwing exceptions
- ❌ Generic exceptions (e.g., `Exception`, `RuntimeException`) instead of domain-specific
- ❌ Exceptions not handled by `GlobalExceptionHandler`
- ❌ Wrong HTTP status codes (e.g., 500 for not found)

---

### 4. Data Validation & Integrity ✅

#### Request Validation
- [ ] All required fields have `@NotNull` or `@NotBlank`
- [ ] String fields have `@Size` constraints
- [ ] Email fields have `@Email` validation
- [ ] Phone fields have `@Pattern` validation
- [ ] Custom validation messages are meaningful

#### Business Validation
- [ ] Uniqueness checked before create (e.g., email, license number)
- [ ] Uniqueness checked before update if field changed
- [ ] Foreign key references validated (e.g., doctor exists before creating shift)
- [ ] Business rules enforced (e.g., shift time conflicts)
- [ ] Date/time ranges validated

#### Database Constraints
- [ ] Unique constraints at DB level (`@Column(unique = true)`)
- [ ] Not-null constraints at DB level (`@Column(nullable = false)`)
- [ ] Indexes on frequently queried fields

**Red Flags**:
- ❌ No validation on required fields
- ❌ Duplicate checks missing
- ❌ Foreign key integrity not validated
- ❌ Business rules not enforced
- ❌ Relying only on DB constraints without service-layer validation

---

### 5. HTTP & REST Best Practices ✅

#### HTTP Status Codes
- [ ] 201 Created for POST (create)
- [ ] 200 OK for GET, PUT (read, update)
- [ ] 204 No Content for DELETE
- [ ] 400 Bad Request for validation errors
- [ ] 404 Not Found for missing resources
- [ ] 409 Conflict for duplicate/conflict errors

#### Request/Response Handling
- [ ] All endpoints return `ResponseEntity<>`
- [ ] Request bodies use `@Valid @RequestBody`
- [ ] Path variables use `@PathVariable`
- [ ] Query parameters use `@RequestParam`
- [ ] Response DTOs include `id`, `createdAt`, `updatedAt`

#### REST Naming Conventions
- [ ] Plural resource names (e.g., `/api/v1/patients`, not `/api/v1/patient`)
- [ ] Consistent URL structure
- [ ] Proper HTTP methods (GET, POST, PUT, DELETE)

**Red Flags**:
- ❌ Wrong status codes (e.g., 200 for create instead of 201)
- ❌ Returning entities instead of DTOs
- ❌ Missing `@Valid` on request bodies
- ❌ Inconsistent URL patterns
- ❌ Using POST for updates or GET for creates

---

### 6. Testing Quality ✅

#### Test Coverage
- [ ] Unit tests for all service methods
- [ ] Integration tests for all controller endpoints
- [ ] Repository tests for custom queries
- [ ] Coverage meets targets (80%+ line, 70%+ branch)

#### Test Organization
- [ ] Proper test annotations (`@UnitTest`, `@IntegrationTest`)
- [ ] `@Nested` classes for grouping
- [ ] `@DisplayName` for readable test names
- [ ] Tests follow Given-When-Then pattern

#### Test Quality
- [ ] Tests cover success scenarios
- [ ] Tests cover exception scenarios
- [ ] Tests verify behavior, not just execution
- [ ] Mocks used correctly (verify interactions)
- [ ] Integration tests use real database
- [ ] `@DirtiesContext` prevents test pollution

#### Test Naming
- [ ] Test method names describe behavior
- [ ] Format: `should[ExpectedBehavior]When[StateUnderTest]`
- [ ] Clear and concise

**Red Flags**:
- ❌ Low test coverage (<80%)
- ❌ Tests without assertions
- ❌ Tests that only verify no exceptions thrown
- ❌ Missing exception scenario tests
- ❌ Integration tests without database cleanup
- ❌ Flaky tests

---

### 7. Security & Vulnerability Checks ✅

#### SQL Injection Prevention
- [ ] No string concatenation in queries
- [ ] Parameterized queries with `@Param`
- [ ] Spring Data derived queries used where possible

#### Input Validation
- [ ] All user inputs validated
- [ ] No unvalidated data in queries
- [ ] Size limits on string inputs
- [ ] Pattern validation on structured data (email, phone)

#### Sensitive Data
- [ ] No passwords in plain text
- [ ] No sensitive data in logs
- [ ] No API keys or secrets in code

#### Best Practices
- [ ] Latest Spring Boot version with security patches
- [ ] Dependencies up to date
- [ ] No deprecated APIs used

**Red Flags**:
- ❌ String concatenation in JPQL queries
- ❌ Unvalidated user input
- ❌ Sensitive data in logs or responses
- ❌ Outdated dependencies with known vulnerabilities

---

### 8. Code Quality & Maintainability ✅

#### Naming Conventions
- [ ] Classes use PascalCase (e.g., `PatientService`)
- [ ] Methods use camelCase (e.g., `createPatient`)
- [ ] Variables use camelCase (e.g., `patientRepository`)
- [ ] Constants use UPPER_SNAKE_CASE (e.g., `MAX_RETRY_COUNT`)
- [ ] Package names use lowercase (e.g., `com.sparks.patient.service`)

#### Code Duplication
- [ ] No duplicate code across layers
- [ ] Common logic extracted to methods
- [ ] Mappers handle all entity-DTO conversions
- [ ] Reusable utilities in helper classes

#### Lombok Usage
- [ ] `@Data` on entities and DTOs
- [ ] `@Builder` for object creation
- [ ] `@RequiredArgsConstructor` for dependency injection
- [ ] `@Slf4j` for logging
- [ ] `@NoArgsConstructor` and `@AllArgsConstructor` on entities and DTOs

#### Logging
- [ ] INFO level for operations (create, update, delete, get)
- [ ] WARN level for business rule violations
- [ ] ERROR level for unexpected exceptions
- [ ] No sensitive data in logs
- [ ] Meaningful log messages with context

#### Code Readability
- [ ] Methods are concise (< 50 lines)
- [ ] Single responsibility principle followed
- [ ] Clear variable and method names
- [ ] No magic numbers (use constants)
- [ ] Comments only where needed (code should be self-documenting)

**Red Flags**:
- ❌ Inconsistent naming conventions
- ❌ Duplicate code
- ❌ Long methods (>50 lines)
- ❌ Poor variable names (e.g., `x`, `temp`, `data`)
- ❌ Missing logging for important operations
- ❌ Excessive comments explaining obvious code

---

### 9. OpenAPI Documentation ✅

#### API-Level Documentation
- [ ] `@Tag` on controller with name and description
- [ ] Meaningful tag names (e.g., "Patient Management")

#### Endpoint Documentation
- [ ] `@Operation` on each endpoint
- [ ] Clear `summary` (short description)
- [ ] Detailed `description` explaining purpose
- [ ] `@ApiResponses` with all possible response codes
- [ ] Each `@ApiResponse` has `responseCode` and `description`

#### DTO Documentation
- [ ] `@Schema` on request and response DTOs
- [ ] `@Schema(description = "...")` on each field
- [ ] `@Schema(example = "...")` on each field
- [ ] Examples are realistic and helpful

**Red Flags**:
- ❌ Missing OpenAPI annotations
- ❌ Generic descriptions (e.g., "Update entity")
- ❌ Missing examples in schemas
- ❌ Undocumented response codes

---

### 10. Database & JPA Best Practices ✅

#### Entity Design
- [ ] ID type is `Long` with `IDENTITY` generation
- [ ] Timestamps (`createdAt`, `updatedAt`) managed by `@PrePersist/@PreUpdate`
- [ ] Explicit column names with `@Column(name = "...")`
- [ ] Proper cascade and fetch strategies on relationships

#### Repository Design
- [ ] Extends `JpaRepository<Entity, Long>`
- [ ] Custom queries use JPQL (not native SQL)
- [ ] Named parameters with `@Param`
- [ ] Returns `Optional<>` for single results
- [ ] Returns `List<>` for multiple results

#### Performance Considerations
- [ ] Lazy loading for relationships (where appropriate)
- [ ] JOIN FETCH to prevent N+1 queries
- [ ] Indexes on frequently queried fields
- [ ] Pagination for large result sets

**Red Flags**:
- ❌ N+1 query problems
- ❌ Eager loading everything
- ❌ Native SQL queries (should use JPQL)
- ❌ Missing indexes on foreign keys
- ❌ No pagination for list endpoints

---

## Code Review Process

### Step 1: Architectural Review (5 min)
1. Verify three-layer pattern compliance
2. Check package structure
3. Verify proper separation of concerns

### Step 2: Code Quality Review (10 min)
1. Check annotations on all classes
2. Verify naming conventions
3. Check for code duplication
4. Review logging usage

### Step 3: Validation & Error Handling (5 min)
1. Verify request validation
2. Check exception handling
3. Verify HTTP status codes
4. Review error messages

### Step 4: Security Review (5 min)
1. Check for SQL injection risks
2. Verify input validation
3. Check for sensitive data exposure

### Step 5: Testing Review (10 min)
1. Verify test coverage
2. Check test quality
3. Review test organization
4. Verify test scenarios

### Step 6: Documentation Review (5 min)
1. Check OpenAPI annotations
2. Verify API documentation completeness
3. Review code comments

---

## Review Templates

### Approval Template

```markdown
## Code Review: [Feature Name]

### ✅ Approved

**Summary**: Code follows project standards and architectural patterns.

**Strengths**:
- Three-layer architecture properly implemented
- Comprehensive test coverage (85%)
- Proper validation and error handling
- Well-documented APIs

**Minor Suggestions** (non-blocking):
- Consider adding index on `email` column for performance
- Log message on line 45 could include patient ID for better debugging

**Files Reviewed**:
- PatientController.java
- PatientService.java / PatientServiceImpl.java
- PatientRepository.java
- PatientEntity.java
- PatientRequestDto.java / PatientResponseDto.java
- PatientMapper.java
- PatientServiceImplTest.java
- PatientControllerIntegrationTest.java

**Test Results**: ✅ All tests passing (mvn test)
**Coverage**: 85% line, 72% branch
```

---

### Request Changes Template

```markdown
## Code Review: [Feature Name]

### ⚠️ Changes Requested

**Summary**: Code needs modifications before approval.

**Critical Issues** (must fix):
1. **Missing @Transactional on write operations** (PatientServiceImpl:45, 67)
   - `createPatient()` and `updatePatient()` need `@Transactional`

2. **SQL Injection Risk** (PatientRepository:23)
   - String concatenation in query - use `@Param` instead
   ```java
   // Current (VULNERABLE):
   @Query("SELECT p FROM Patient p WHERE p.name = '" + name + "'")

   // Fix:
   @Query("SELECT p FROM Patient p WHERE p.name = :name")
   List<Patient> findByName(@Param("name") String name);
   ```

3. **Missing Validation** (PatientRequestDto)
   - `email` field missing `@Email` validation
   - `phoneNumber` field missing `@Pattern` validation

4. **Wrong HTTP Status Code** (PatientController:89)
   - DELETE should return 204, not 200
   ```java
   // Current:
   return ResponseEntity.ok().build();

   // Fix:
   return ResponseEntity.noContent().build();
   ```

5. **Missing Test Coverage** (PatientServiceImplTest)
   - No test for duplicate email scenario
   - No test for `updatePatient()` exception cases
   - Current coverage: 62% (target: 80%+)

**Minor Issues** (should fix):
- Missing OpenAPI documentation on POST endpoint
- Log message missing context (line 34)
- Inconsistent naming: `getPatient()` vs `retrievePatientById()`

**Files Requiring Changes**:
- PatientServiceImpl.java (Issues #1)
- PatientRepository.java (Issue #2)
- PatientRequestDto.java (Issue #3)
- PatientController.java (Issue #4)
- PatientServiceImplTest.java (Issue #5)

**Next Steps**:
1. Fix critical issues listed above
2. Run `mvn test` to ensure all tests pass
3. Run `mvn test jacoco:report` to verify coverage
4. Request re-review
```

---

### Security Findings Template

```markdown
## Security Review: [Feature Name]

### 🔒 Security Issues Found

**Critical**:
1. **SQL Injection Vulnerability** - HIGH RISK
   - Location: PatientRepository.java:45
   - Issue: String concatenation in native query
   - Fix: Use parameterized query with @Param

2. **Sensitive Data Exposure** - MEDIUM RISK
   - Location: PatientController.java:78
   - Issue: Returning password field in response
   - Fix: Remove password from PatientResponseDto

**Warnings**:
1. **Missing Input Validation**
   - Email not validated in PatientRequestDto
   - Could allow invalid email formats

2. **No Rate Limiting**
   - Consider adding rate limiting for patient creation endpoint

**Recommendations**:
- Update Spring Boot to latest patch version
- Enable HTTPS in production
- Add request size limits
```

---

## Common Anti-Patterns to Watch For

### 1. God Service
**Problem**: Service class with too many responsibilities
```java
// BAD
public class PatientService {
    void createPatient() {}
    void createDoctor() {}
    void createAppointment() {}
    void sendEmail() {}
    void generateReport() {}
}
```
**Fix**: Split into domain-specific services

---

### 2. Anemic Domain Model
**Problem**: Entities with no behavior, all logic in services
```java
// BAD - Entity is just a data container
@Entity
public class Shift {
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    // No validation or business logic
}

// Service does everything
public class ShiftService {
    void create(Shift shift) {
        // Validate time slot
        if (shift.getStartTime().isAfter(shift.getEndTime())) {
            throw new InvalidTimeSlotException();
        }
    }
}
```
**Fix**: Move validation to entity
```java
// GOOD
@Entity
public class Shift {
    private LocalDateTime startTime;
    private LocalDateTime endTime;

    public boolean isValidTimeSlot() {
        return startTime != null && endTime != null &&
               startTime.isBefore(endTime);
    }
}
```

---

### 3. Repository in Controller
**Problem**: Controllers calling repositories directly
```java
// BAD
@RestController
public class PatientController {
    @Autowired
    private PatientRepository repository;

    @GetMapping("/{id}")
    public Patient get(@PathVariable Long id) {
        return repository.findById(id).orElse(null);
    }
}
```
**Fix**: Use service layer

---

### 4. Entity Exposure
**Problem**: Returning entities instead of DTOs
```java
// BAD
@GetMapping("/{id}")
public ResponseEntity<Patient> getPatient(@PathVariable Long id) {
    return ResponseEntity.ok(patientService.getById(id));
}
```
**Fix**: Return DTOs

---

### 5. Manual Mapping
**Problem**: Mapping entities to DTOs in service
```java
// BAD
public PatientResponseDto getPatient(Long id) {
    Patient patient = repository.findById(id).orElseThrow();
    PatientResponseDto dto = new PatientResponseDto();
    dto.setId(patient.getId());
    dto.setName(patient.getName());
    // ... manual mapping
    return dto;
}
```
**Fix**: Use mapper component

---

### 6. Missing Validation
**Problem**: No validation on service inputs
```java
// BAD
public void createPatient(PatientRequestDto request) {
    // No duplicate check
    Patient patient = mapper.toEntity(request);
    repository.save(patient);
}
```
**Fix**: Validate before saving

---

### 7. Exception Swallowing
**Problem**: Catching and ignoring exceptions
```java
// BAD
try {
    repository.save(patient);
} catch (Exception e) {
    // Silently fail
    return null;
}
```
**Fix**: Throw domain exception

---

## Review Automation

### Pre-commit Checks
```bash
# Format check
mvn spotless:check

# Compile
mvn clean compile

# Run fast tests
mvn test -Pfast

# Security scan
mvn dependency-check:check
```

### CI/CD Pipeline Checks
- [ ] All tests pass
- [ ] Coverage meets threshold (80%+)
- [ ] No security vulnerabilities
- [ ] Code style compliance
- [ ] Build successful

---

## Quality Metrics

Track these metrics for code health:

- **Test Coverage**: Line 80%+, Branch 70%+
- **Code Duplication**: < 5%
- **Cyclomatic Complexity**: < 10 per method
- **Test Execution Time**: Fast tests < 60s
- **Build Time**: < 5 minutes
- **API Documentation**: 100% endpoints documented

---

## When to Ask for Help

**Ask the Coding Agent** if you need:
- Code refactoring
- Pattern implementation
- Bug fixes

**Ask the Testing Agent** if you need:
- Additional test coverage
- Test quality improvement
- Test execution

**Ask the User** if:
- Requirements conflict with code
- Major architectural changes needed
- Breaking changes required

---

## Final Notes

- **Be constructive**: Focus on improvement, not criticism
- **Prioritize issues**: Critical > Major > Minor
- **Provide examples**: Show how to fix issues
- **Be consistent**: Apply same standards to all code
- **Explain why**: Help developers understand the reasoning
- **Celebrate good code**: Acknowledge well-written code

The goal is not perfection—it's maintainable, secure, and scalable code that follows project standards.

Happy reviewing! 🔍
