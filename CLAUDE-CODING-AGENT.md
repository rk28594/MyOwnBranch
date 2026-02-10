# CLAUDE-CODING-AGENT.md

This file provides specialized guidance for the **Coding Agent** — responsible for implementing new features, endpoints, and business logic in the Patient Management Service.

## Agent Role & Responsibilities

**Primary Mission**: Implement production-ready code following established architectural patterns

**Core Tasks**:
- ✅ Add new REST endpoints with proper validation
- ✅ Implement service layer business logic
- ✅ Create repository methods and custom queries
- ✅ Design and map entities and DTOs
- ✅ Handle exceptions with proper error responses
- ✅ Generate OpenAPI documentation
- ⛔ **DO NOT**: Write tests (Testing Agent's job)
- ⛔ **DO NOT**: Review code quality (Code Review Agent's job)

---

## Mandatory Architecture Pattern

**ALWAYS follow the three-layer pattern** used across all four domains (Patient, Doctor, Shift, Appointment):

```
1. Controller (@RestController)
   ↓
2. Service (Interface + Impl)
   ↓
3. Repository (JpaRepository)
   ↓
4. Entity (@Entity)

Cross-cutting: DTOs, Mapper, Exceptions
```

---

## Code Templates

### 1. Entity Template

```java
package com.sparks.patient.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "table_name")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EntityName {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String requiredField;

    @Column(unique = true)
    private String uniqueField;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
```

**Entity Rules**:
- Use `Long` for IDs
- Add `@Column(nullable = false)` for required fields
- Add `@Column(unique = true)` for unique constraints
- Always include `createdAt` and `updatedAt` with `@PrePersist/@PreUpdate`
- Use `@Table(name = "...")` for explicit table naming
- Lombok: `@Data`, `@Builder`, `@NoArgsConstructor`, `@AllArgsConstructor`

---

### 2. Request DTO Template

```java
package com.sparks.patient.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request to create/update entity")
public class EntityRequestDto {

    @NotBlank(message = "Field is required")
    @Size(min = 2, max = 100, message = "Field must be between 2 and 100 characters")
    @Schema(description = "Description of field", example = "Example Value")
    private String requiredField;

    @Email(message = "Email must be valid")
    @Schema(description = "Email address", example = "user@example.com")
    private String email;

    @Pattern(regexp = "^\\+?[1-9]\\d{1,14}$", message = "Phone number must be valid")
    @Schema(description = "Phone number", example = "+1234567890")
    private String phoneNumber;
}
```

**Request DTO Rules**:
- Use JSR-303 validation annotations (`@NotBlank`, `@NotNull`, `@Email`, `@Size`, `@Pattern`)
- Add meaningful `message` attributes
- Include `@Schema` for OpenAPI documentation with `description` and `example`
- Lombok: `@Data`, `@Builder`, `@NoArgsConstructor`, `@AllArgsConstructor`

---

### 3. Response DTO Template

```java
package com.sparks.patient.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Entity response")
public class EntityResponseDto {

    @Schema(description = "Unique identifier", example = "1")
    private Long id;

    @Schema(description = "Field description", example = "Example Value")
    private String field;

    @Schema(description = "Creation timestamp")
    private LocalDateTime createdAt;

    @Schema(description = "Last update timestamp")
    private LocalDateTime updatedAt;
}
```

**Response DTO Rules**:
- Include `id`, `createdAt`, `updatedAt`
- Add `@Schema` with `description` and `example`
- No validation annotations needed

---

### 4. Mapper Template

```java
package com.sparks.patient.mapper;

import com.sparks.patient.dto.EntityRequestDto;
import com.sparks.patient.dto.EntityResponseDto;
import com.sparks.patient.entity.EntityName;
import org.springframework.stereotype.Component;

@Component
public class EntityMapper {

    /**
     * Convert request DTO to entity for creation
     */
    public EntityName toEntity(EntityRequestDto request) {
        return EntityName.builder()
                .field(request.getField())
                .build();
    }

    /**
     * Convert entity to response DTO
     */
    public EntityResponseDto toResponse(EntityName entity) {
        return EntityResponseDto.builder()
                .id(entity.getId())
                .field(entity.getField())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    /**
     * Update existing entity from request DTO
     */
    public void updateEntity(EntityName entity, EntityRequestDto request) {
        entity.setField(request.getField());
        // updatedAt is handled by @PreUpdate
    }
}
```

**Mapper Rules**:
- Annotate with `@Component`
- Three methods: `toEntity()`, `toResponse()`, `updateEntity()`
- Include JavaDoc comments
- Never map `id`, `createdAt`, `updatedAt` in `toEntity()` or `updateEntity()`

---

### 5. Repository Template

```java
package com.sparks.patient.repository;

import com.sparks.patient.entity.EntityName;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

@Repository
public interface EntityRepository extends JpaRepository<EntityName, Long> {

    // Spring Data JPA derived query
    Optional<EntityName> findByUniqueField(String uniqueField);

    boolean existsByUniqueField(String uniqueField);

    // Custom JPQL query for complex logic
    @Query("SELECT e FROM EntityName e WHERE e.field = :field AND e.status = :status")
    List<EntityName> findByFieldAndStatus(@Param("field") String field,
                                          @Param("status") String status);
}
```

**Repository Rules**:
- Extend `JpaRepository<EntityName, Long>`
- Use Spring Data derived queries for simple cases
- Use `@Query` with JPQL for complex queries
- Use `@Param` for named parameters
- Return `Optional<>` for single results, `List<>` for multiple

---

### 6. Service Interface Template

```java
package com.sparks.patient.service;

import com.sparks.patient.dto.EntityRequestDto;
import com.sparks.patient.dto.EntityResponseDto;

import java.util.List;

public interface EntityService {

    EntityResponseDto createEntity(EntityRequestDto request);

    EntityResponseDto getEntityById(Long id);

    List<EntityResponseDto> getAllEntities();

    EntityResponseDto updateEntity(Long id, EntityRequestDto request);

    void deleteEntity(Long id);
}
```

---

### 7. Service Implementation Template

```java
package com.sparks.patient.service;

import com.sparks.patient.dto.EntityRequestDto;
import com.sparks.patient.dto.EntityResponseDto;
import com.sparks.patient.entity.EntityName;
import com.sparks.patient.exception.EntityNotFoundException;
import com.sparks.patient.exception.DuplicateFieldException;
import com.sparks.patient.mapper.EntityMapper;
import com.sparks.patient.repository.EntityRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class EntityServiceImpl implements EntityService {

    private final EntityRepository entityRepository;
    private final EntityMapper entityMapper;

    @Override
    @Transactional
    public EntityResponseDto createEntity(EntityRequestDto request) {
        log.info("Creating entity with field: {}", request.getField());

        // Check for duplicates if needed
        if (entityRepository.existsByUniqueField(request.getUniqueField())) {
            throw new DuplicateFieldException("Entity with this field already exists");
        }

        EntityName entity = entityMapper.toEntity(request);
        EntityName savedEntity = entityRepository.save(entity);

        log.info("Entity created with ID: {}", savedEntity.getId());
        return entityMapper.toResponse(savedEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public EntityResponseDto getEntityById(Long id) {
        log.info("Fetching entity with ID: {}", id);

        EntityName entity = entityRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Entity not found with ID: " + id));

        return entityMapper.toResponse(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EntityResponseDto> getAllEntities() {
        log.info("Fetching all entities");

        return entityRepository.findAll().stream()
                .map(entityMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public EntityResponseDto updateEntity(Long id, EntityRequestDto request) {
        log.info("Updating entity with ID: {}", id);

        EntityName entity = entityRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Entity not found with ID: " + id));

        // Check for duplicate if unique field is being updated
        if (!entity.getUniqueField().equals(request.getUniqueField()) &&
            entityRepository.existsByUniqueField(request.getUniqueField())) {
            throw new DuplicateFieldException("Entity with this field already exists");
        }

        entityMapper.updateEntity(entity, request);
        EntityName updatedEntity = entityRepository.save(entity);

        log.info("Entity updated with ID: {}", id);
        return entityMapper.toResponse(updatedEntity);
    }

    @Override
    @Transactional
    public void deleteEntity(Long id) {
        log.info("Deleting entity with ID: {}", id);

        if (!entityRepository.existsById(id)) {
            throw new EntityNotFoundException("Entity not found with ID: " + id);
        }

        entityRepository.deleteById(id);
        log.info("Entity deleted with ID: {}", id);
    }
}
```

**Service Implementation Rules**:
- Annotate with `@Service`, `@RequiredArgsConstructor`, `@Slf4j`
- Use constructor injection (final fields + Lombok)
- Add `@Transactional` on write methods (create, update, delete)
- Add `@Transactional(readOnly = true)` on read methods
- Log at INFO level for operations
- Throw domain-specific exceptions
- Always validate existence before update/delete

---

### 8. Controller Template

```java
package com.sparks.patient.controller;

import com.sparks.patient.dto.EntityRequestDto;
import com.sparks.patient.dto.EntityResponseDto;
import com.sparks.patient.service.EntityService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/v1/entities")
@RequiredArgsConstructor
@Validated
@Slf4j
@Tag(name = "Entity Management", description = "APIs for managing entities")
public class EntityController {

    private final EntityService entityService;

    @PostMapping
    @Operation(summary = "Create a new entity", description = "Creates a new entity with the provided information")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Entity created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid input"),
        @ApiResponse(responseCode = "409", description = "Duplicate field")
    })
    public ResponseEntity<EntityResponseDto> createEntity(@Valid @RequestBody EntityRequestDto request) {
        log.info("REST request to create entity");
        EntityResponseDto response = entityService.createEntity(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get entity by ID", description = "Retrieves an entity by its unique identifier")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Entity found"),
        @ApiResponse(responseCode = "404", description = "Entity not found")
    })
    public ResponseEntity<EntityResponseDto> getEntityById(@PathVariable Long id) {
        log.info("REST request to get entity with ID: {}", id);
        EntityResponseDto response = entityService.getEntityById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @Operation(summary = "Get all entities", description = "Retrieves all entities")
    @ApiResponse(responseCode = "200", description = "Entities retrieved successfully")
    public ResponseEntity<List<EntityResponseDto>> getAllEntities() {
        log.info("REST request to get all entities");
        List<EntityResponseDto> response = entityService.getAllEntities();
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update entity", description = "Updates an existing entity")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Entity updated successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid input"),
        @ApiResponse(responseCode = "404", description = "Entity not found"),
        @ApiResponse(responseCode = "409", description = "Duplicate field")
    })
    public ResponseEntity<EntityResponseDto> updateEntity(@PathVariable Long id,
                                                          @Valid @RequestBody EntityRequestDto request) {
        log.info("REST request to update entity with ID: {}", id);
        EntityResponseDto response = entityService.updateEntity(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete entity", description = "Deletes an entity by its unique identifier")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Entity deleted successfully"),
        @ApiResponse(responseCode = "404", description = "Entity not found")
    })
    public ResponseEntity<Void> deleteEntity(@PathVariable Long id) {
        log.info("REST request to delete entity with ID: {}", id);
        entityService.deleteEntity(id);
        return ResponseEntity.noContent().build();
    }
}
```

**Controller Rules**:
- Annotate with `@RestController`, `@RequestMapping`, `@RequiredArgsConstructor`, `@Validated`, `@Slf4j`
- Use `@Valid` on `@RequestBody` parameters
- Add OpenAPI annotations: `@Tag`, `@Operation`, `@ApiResponses`
- Return proper HTTP status codes (201 for create, 204 for delete, 200 for get/update)
- Log at INFO level
- Use `ResponseEntity<>` for all responses

---

### 9. Custom Exception Template

```java
package com.sparks.patient.exception;

public class EntityNotFoundException extends RuntimeException {
    public EntityNotFoundException(String message) {
        super(message);
    }
}
```

**Exception Rules**:
- Extend `RuntimeException`
- Simple constructor with message
- Place in `com.sparks.patient.exception` package
- GlobalExceptionHandler will catch it automatically

---

## HTTP Status Code Guidelines

| Operation | Success Status | Error Status |
|-----------|----------------|--------------|
| POST (Create) | 201 Created | 400 Bad Request, 409 Conflict |
| GET | 200 OK | 404 Not Found |
| PUT (Update) | 200 OK | 400 Bad Request, 404 Not Found, 409 Conflict |
| DELETE | 204 No Content | 404 Not Found |

---

## Validation Patterns

### Common Annotations

```java
@NotNull(message = "Field cannot be null")
@NotBlank(message = "Field cannot be blank")
@Size(min = 2, max = 100, message = "Field must be between 2 and 100 characters")
@Email(message = "Email must be valid")
@Pattern(regexp = "^\\+?[1-9]\\d{1,14}$", message = "Phone number must be valid")
@Min(value = 0, message = "Value must be positive")
@Max(value = 100, message = "Value must not exceed 100")
@Past(message = "Date must be in the past")
@Future(message = "Date must be in the future")
```

---

## Database Query Best Practices

### 1. Use Spring Data Derived Queries

```java
// Good - Spring generates query
Optional<Patient> findByEmail(String email);
List<Doctor> findBySpecialization(String specialization);
boolean existsByLicenseNumber(String licenseNumber);
```

### 2. Custom JPQL for Complex Logic

```java
// For complex queries with multiple conditions
@Query("SELECT s FROM Shift s WHERE s.doctor.id = :doctorId " +
       "AND ((s.startTime < :endTime AND s.endTime > :startTime))")
List<Shift> findConflictingShifts(@Param("doctorId") Long doctorId,
                                   @Param("startTime") LocalDateTime startTime,
                                   @Param("endTime") LocalDateTime endTime);
```

### 3. Prevent N+1 Queries with JOIN FETCH

```java
@Query("SELECT a FROM Appointment a JOIN FETCH a.patient JOIN FETCH a.doctor WHERE a.id = :id")
Optional<Appointment> findByIdWithDetails(@Param("id") Long id);
```

---

## Logging Standards

```java
// INFO level for operations
log.info("Creating patient with email: {}", request.getEmail());
log.info("Patient created with ID: {}", savedPatient.getId());

// WARN level for business rule violations
log.warn("Attempt to create duplicate email: {}", request.getEmail());

// ERROR level for unexpected exceptions
log.error("Failed to create patient", exception);
```

---

## Coding Checklist

Before completing any feature, verify:

- [ ] Entity has `@PrePersist` and `@PreUpdate` for timestamps
- [ ] Request DTO has validation annotations and `@Schema`
- [ ] Response DTO includes `id`, `createdAt`, `updatedAt`
- [ ] Mapper has all three methods: `toEntity()`, `toResponse()`, `updateEntity()`
- [ ] Repository extends `JpaRepository<Entity, Long>`
- [ ] Service has interface and implementation
- [ ] Service uses `@Transactional` correctly (readOnly for reads)
- [ ] Service validates duplicates before create/update
- [ ] Service throws domain-specific exceptions
- [ ] Controller has `@Valid` on request bodies
- [ ] Controller has OpenAPI annotations
- [ ] Controller returns correct HTTP status codes
- [ ] All classes have proper Lombok annotations
- [ ] Logging at INFO level for operations

---

## Common Patterns from Existing Code

### Pattern 1: Shift Conflict Detection

```java
// In repository
@Query("SELECT s FROM Shift s WHERE s.doctor.id = :doctorId " +
       "AND s.id != :excludeId " +
       "AND ((s.startTime < :endTime AND s.endTime > :startTime))")
List<Shift> findConflictingShiftsExcluding(@Param("doctorId") Long doctorId,
                                            @Param("excludeId") Long excludeId,
                                            @Param("startTime") LocalDateTime startTime,
                                            @Param("endTime") LocalDateTime endTime);

// In service
private void validateShiftConflicts(Long doctorId, LocalDateTime startTime,
                                    LocalDateTime endTime, Long excludeId) {
    List<Shift> conflicts = (excludeId != null)
        ? shiftRepository.findConflictingShiftsExcluding(doctorId, excludeId, startTime, endTime)
        : shiftRepository.findConflictingShifts(doctorId, startTime, endTime);

    if (!conflicts.isEmpty()) {
        throw new ShiftConflictException("Doctor has conflicting shift in this time slot");
    }
}
```

### Pattern 2: UUID Generation

```java
@Entity
public class Appointment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String appointmentUuid;

    @PrePersist
    protected void onCreate() {
        if (appointmentUuid == null) {
            appointmentUuid = UUID.randomUUID().toString();
        }
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
}
```

### Pattern 3: Enum Status Fields

```java
public enum AppointmentStatus {
    SCHEDULED,
    CONFIRMED,
    COMPLETED,
    CANCELLED
}

// In entity
@Enumerated(EnumType.STRING)
@Column(nullable = false)
private AppointmentStatus status;
```

---

## When to Ask for Help

**Ask the Testing Agent** if you need to:
- Write tests (not your responsibility)
- Run test suites
- Check coverage

**Ask the Code Review Agent** if you need to:
- Verify code quality
- Check architectural consistency
- Review before committing

**Ask the User** if:
- Requirements are unclear
- Multiple valid approaches exist
- Breaking changes are needed

---

## Final Notes

- **Consistency is key**: Follow existing patterns exactly
- **Read before you write**: Always check similar existing code
- **Log everything**: INFO level for all operations
- **Validate early**: Check for duplicates, nulls, conflicts before persistence
- **Use Lombok**: Reduce boilerplate with `@Data`, `@Builder`, etc.
- **Document APIs**: Always add `@Operation` and `@ApiResponses`

Happy coding! 🚀
