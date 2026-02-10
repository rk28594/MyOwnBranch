# CLAUDE-TESTING-AGENT.md

This file provides specialized guidance for the **Testing Agent** — responsible for creating comprehensive unit and integration tests for the Patient Management Service.

## Agent Role & Responsibilities

**Primary Mission**: Ensure code quality through comprehensive test coverage

**Core Tasks**:
- ✅ Write unit tests for service layer with Mockito
- ✅ Write integration tests for controllers with REST Assured
- ✅ Write repository tests with Spring Data JPA
- ✅ Generate test data and fixtures
- ✅ Run test suites and analyze coverage
- ✅ Fix failing tests
- ⛔ **DO NOT**: Implement production code (Coding Agent's job)
- ⛔ **DO NOT**: Review code architecture (Code Review Agent's job)

---

## Test Architecture

The project uses **custom test annotations** to organize tests:

### @UnitTest (Tag: `unit`)
- **Purpose**: Fast, isolated tests without Spring context
- **Use**: Service layer business logic testing
- **Framework**: JUnit 5 + Mockito
- **Speed**: Very fast (~45 seconds for all unit tests with `-Pfast`)

### @IntegrationTest (Tag: `integration`)
- **Purpose**: Full Spring Boot context tests
- **Use**: Controller endpoints, repository queries, end-to-end flows
- **Framework**: JUnit 5 + Spring Boot Test + REST Assured
- **Database**: Real H2 in-memory database
- **Isolation**: `@DirtiesContext` to prevent test pollution

---

## Test Execution Commands

```bash
# Run all tests (~193 tests)
mvn test

# Fast unit tests only (parallel execution, ~45s)
mvn test -Pfast

# Unit tests only (sequential)
mvn test -Punit

# Integration tests only
mvn test -Pintegration

# Service layer tests only
mvn test -Pservice

# Repository tests only
mvn test -Prepository

# API/controller tests only
mvn test -Papi

# Single test class
mvn test -Dtest=PatientServiceImplTest

# Single test method
mvn test -Dtest=PatientServiceImplTest#shouldCreatePatientSuccessfully

# Coverage report
mvn test jacoco:report
# View at: target/site/jacoco/index.html
```

---

## Test Templates

### 1. Unit Test Template (Service Layer)

```java
package com.sparks.patient.service;

import com.sparks.patient.dto.EntityRequestDto;
import com.sparks.patient.dto.EntityResponseDto;
import com.sparks.patient.entity.EntityName;
import com.sparks.patient.exception.EntityNotFoundException;
import com.sparks.patient.exception.DuplicateFieldException;
import com.sparks.patient.mapper.EntityMapper;
import com.sparks.patient.repository.EntityRepository;
import com.sparks.patient.test.UnitTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@UnitTest
@ExtendWith(MockitoExtension.class)
@DisplayName("EntityService Unit Tests")
class EntityServiceImplTest {

    @Mock
    private EntityRepository entityRepository;

    @Mock
    private EntityMapper entityMapper;

    @InjectMocks
    private EntityServiceImpl entityService;

    private EntityRequestDto requestDto;
    private EntityName entity;
    private EntityResponseDto responseDto;

    @BeforeEach
    void setUp() {
        requestDto = EntityRequestDto.builder()
                .field("Test Value")
                .uniqueField("unique123")
                .build();

        entity = EntityName.builder()
                .id(1L)
                .field("Test Value")
                .uniqueField("unique123")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        responseDto = EntityResponseDto.builder()
                .id(1L)
                .field("Test Value")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Nested
    @DisplayName("Create Entity Tests")
    class CreateEntityTests {

        @Test
        @DisplayName("Should create entity successfully")
        void shouldCreateEntitySuccessfully() {
            // Given
            when(entityRepository.existsByUniqueField(anyString())).thenReturn(false);
            when(entityMapper.toEntity(any(EntityRequestDto.class))).thenReturn(entity);
            when(entityRepository.save(any(EntityName.class))).thenReturn(entity);
            when(entityMapper.toResponse(any(EntityName.class))).thenReturn(responseDto);

            // When
            EntityResponseDto result = entityService.createEntity(requestDto);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(1L);
            assertThat(result.getField()).isEqualTo("Test Value");

            verify(entityRepository).existsByUniqueField("unique123");
            verify(entityMapper).toEntity(requestDto);
            verify(entityRepository).save(entity);
            verify(entityMapper).toResponse(entity);
            verifyNoMoreInteractions(entityRepository, entityMapper);
        }

        @Test
        @DisplayName("Should throw exception when duplicate field exists")
        void shouldThrowExceptionWhenDuplicateFieldExists() {
            // Given
            when(entityRepository.existsByUniqueField(anyString())).thenReturn(true);

            // When & Then
            assertThatThrownBy(() -> entityService.createEntity(requestDto))
                    .isInstanceOf(DuplicateFieldException.class)
                    .hasMessageContaining("already exists");

            verify(entityRepository).existsByUniqueField("unique123");
            verifyNoMoreInteractions(entityRepository);
            verifyNoInteractions(entityMapper);
        }
    }

    @Nested
    @DisplayName("Get Entity Tests")
    class GetEntityTests {

        @Test
        @DisplayName("Should get entity by ID successfully")
        void shouldGetEntityByIdSuccessfully() {
            // Given
            when(entityRepository.findById(anyLong())).thenReturn(Optional.of(entity));
            when(entityMapper.toResponse(any(EntityName.class))).thenReturn(responseDto);

            // When
            EntityResponseDto result = entityService.getEntityById(1L);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(1L);

            verify(entityRepository).findById(1L);
            verify(entityMapper).toResponse(entity);
        }

        @Test
        @DisplayName("Should throw exception when entity not found")
        void shouldThrowExceptionWhenEntityNotFound() {
            // Given
            when(entityRepository.findById(anyLong())).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> entityService.getEntityById(999L))
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasMessageContaining("not found")
                    .hasMessageContaining("999");

            verify(entityRepository).findById(999L);
            verifyNoInteractions(entityMapper);
        }
    }

    @Nested
    @DisplayName("Get All Entities Tests")
    class GetAllEntitiesTests {

        @Test
        @DisplayName("Should get all entities successfully")
        void shouldGetAllEntitiesSuccessfully() {
            // Given
            List<EntityName> entities = Arrays.asList(entity, entity);
            when(entityRepository.findAll()).thenReturn(entities);
            when(entityMapper.toResponse(any(EntityName.class))).thenReturn(responseDto);

            // When
            List<EntityResponseDto> result = entityService.getAllEntities();

            // Then
            assertThat(result).isNotNull();
            assertThat(result).hasSize(2);

            verify(entityRepository).findAll();
            verify(entityMapper, times(2)).toResponse(any(EntityName.class));
        }

        @Test
        @DisplayName("Should return empty list when no entities exist")
        void shouldReturnEmptyListWhenNoEntitiesExist() {
            // Given
            when(entityRepository.findAll()).thenReturn(Arrays.asList());

            // When
            List<EntityResponseDto> result = entityService.getAllEntities();

            // Then
            assertThat(result).isNotNull();
            assertThat(result).isEmpty();

            verify(entityRepository).findAll();
            verifyNoInteractions(entityMapper);
        }
    }

    @Nested
    @DisplayName("Update Entity Tests")
    class UpdateEntityTests {

        @Test
        @DisplayName("Should update entity successfully")
        void shouldUpdateEntitySuccessfully() {
            // Given
            when(entityRepository.findById(anyLong())).thenReturn(Optional.of(entity));
            when(entityRepository.existsByUniqueField(anyString())).thenReturn(false);
            when(entityRepository.save(any(EntityName.class))).thenReturn(entity);
            when(entityMapper.toResponse(any(EntityName.class))).thenReturn(responseDto);
            doNothing().when(entityMapper).updateEntity(any(EntityName.class), any(EntityRequestDto.class));

            // When
            EntityResponseDto result = entityService.updateEntity(1L, requestDto);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(1L);

            verify(entityRepository).findById(1L);
            verify(entityMapper).updateEntity(entity, requestDto);
            verify(entityRepository).save(entity);
            verify(entityMapper).toResponse(entity);
        }

        @Test
        @DisplayName("Should throw exception when updating non-existent entity")
        void shouldThrowExceptionWhenUpdatingNonExistentEntity() {
            // Given
            when(entityRepository.findById(anyLong())).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> entityService.updateEntity(999L, requestDto))
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasMessageContaining("not found");

            verify(entityRepository).findById(999L);
            verifyNoMoreInteractions(entityRepository);
            verifyNoInteractions(entityMapper);
        }
    }

    @Nested
    @DisplayName("Delete Entity Tests")
    class DeleteEntityTests {

        @Test
        @DisplayName("Should delete entity successfully")
        void shouldDeleteEntitySuccessfully() {
            // Given
            when(entityRepository.existsById(anyLong())).thenReturn(true);
            doNothing().when(entityRepository).deleteById(anyLong());

            // When
            entityService.deleteEntity(1L);

            // Then
            verify(entityRepository).existsById(1L);
            verify(entityRepository).deleteById(1L);
        }

        @Test
        @DisplayName("Should throw exception when deleting non-existent entity")
        void shouldThrowExceptionWhenDeletingNonExistentEntity() {
            // Given
            when(entityRepository.existsById(anyLong())).thenReturn(false);

            // When & Then
            assertThatThrownBy(() -> entityService.deleteEntity(999L))
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasMessageContaining("not found");

            verify(entityRepository).existsById(999L);
            verify(entityRepository, never()).deleteById(anyLong());
        }
    }
}
```

**Unit Test Rules**:
- Use `@UnitTest` custom annotation
- Use `@ExtendWith(MockitoExtension.class)`
- Mock dependencies with `@Mock`
- Inject service with `@InjectMocks`
- Use `@Nested` classes to group related tests
- Use `@DisplayName` for readable test names
- Follow Given-When-Then pattern in test body
- Use AssertJ assertions (`assertThat()`)
- Verify mock interactions with `verify()`
- Use `verifyNoMoreInteractions()` to ensure no extra calls

---

### 2. Integration Test Template (Controller/API)

```java
package com.sparks.patient.controller;

import com.sparks.patient.dto.EntityRequestDto;
import com.sparks.patient.dto.EntityResponseDto;
import com.sparks.patient.entity.EntityName;
import com.sparks.patient.repository.EntityRepository;
import com.sparks.patient.test.IntegrationTest;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.annotation.DirtiesContext;

import java.time.LocalDateTime;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@IntegrationTest
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@DisplayName("Entity API Integration Tests")
class EntityControllerIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private EntityRepository entityRepository;

    private String baseUrl;
    private EntityRequestDto validRequest;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
        baseUrl = "http://localhost:" + port + "/api/v1/entities";

        validRequest = EntityRequestDto.builder()
                .field("Test Value")
                .uniqueField("unique123")
                .build();

        // Clean database
        entityRepository.deleteAll();
    }

    @Nested
    @DisplayName("POST /api/v1/entities - Create Entity")
    class CreateEntityTests {

        @Test
        @DisplayName("Should create entity successfully with 201 status")
        void shouldCreateEntitySuccessfully() {
            given()
                .contentType(ContentType.JSON)
                .body(validRequest)
            .when()
                .post(baseUrl)
            .then()
                .statusCode(201)
                .contentType(ContentType.JSON)
                .body("id", notNullValue())
                .body("field", equalTo("Test Value"))
                .body("uniqueField", equalTo("unique123"))
                .body("createdAt", notNullValue())
                .body("updatedAt", notNullValue());
        }

        @Test
        @DisplayName("Should return 400 when field is blank")
        void shouldReturn400WhenFieldIsBlank() {
            EntityRequestDto invalidRequest = EntityRequestDto.builder()
                    .field("")
                    .uniqueField("unique123")
                    .build();

            given()
                .contentType(ContentType.JSON)
                .body(invalidRequest)
            .when()
                .post(baseUrl)
            .then()
                .statusCode(400)
                .body("message", containsString("Validation failed"))
                .body("errors", not(empty()));
        }

        @Test
        @DisplayName("Should return 409 when duplicate unique field")
        void shouldReturn409WhenDuplicateUniqueField() {
            // Create first entity
            entityRepository.save(EntityName.builder()
                    .field("First")
                    .uniqueField("unique123")
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build());

            // Try to create duplicate
            given()
                .contentType(ContentType.JSON)
                .body(validRequest)
            .when()
                .post(baseUrl)
            .then()
                .statusCode(409)
                .body("message", containsString("already exists"));
        }
    }

    @Nested
    @DisplayName("GET /api/v1/entities/{id} - Get Entity by ID")
    class GetEntityByIdTests {

        @Test
        @DisplayName("Should get entity by ID successfully")
        void shouldGetEntityByIdSuccessfully() {
            // Create entity
            EntityName saved = entityRepository.save(EntityName.builder()
                    .field("Test Value")
                    .uniqueField("unique123")
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build());

            given()
            .when()
                .get(baseUrl + "/" + saved.getId())
            .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("id", equalTo(saved.getId().intValue()))
                .body("field", equalTo("Test Value"));
        }

        @Test
        @DisplayName("Should return 404 when entity not found")
        void shouldReturn404WhenEntityNotFound() {
            given()
            .when()
                .get(baseUrl + "/999")
            .then()
                .statusCode(404)
                .body("message", containsString("not found"));
        }
    }

    @Nested
    @DisplayName("GET /api/v1/entities - Get All Entities")
    class GetAllEntitiesTests {

        @Test
        @DisplayName("Should get all entities successfully")
        void shouldGetAllEntitiesSuccessfully() {
            // Create multiple entities
            entityRepository.save(EntityName.builder()
                    .field("Entity 1")
                    .uniqueField("unique1")
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build());
            entityRepository.save(EntityName.builder()
                    .field("Entity 2")
                    .uniqueField("unique2")
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build());

            given()
            .when()
                .get(baseUrl)
            .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("size()", equalTo(2));
        }

        @Test
        @DisplayName("Should return empty array when no entities exist")
        void shouldReturnEmptyArrayWhenNoEntitiesExist() {
            given()
            .when()
                .get(baseUrl)
            .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("size()", equalTo(0));
        }
    }

    @Nested
    @DisplayName("PUT /api/v1/entities/{id} - Update Entity")
    class UpdateEntityTests {

        @Test
        @DisplayName("Should update entity successfully")
        void shouldUpdateEntitySuccessfully() {
            // Create entity
            EntityName saved = entityRepository.save(EntityName.builder()
                    .field("Original")
                    .uniqueField("unique123")
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build());

            EntityRequestDto updateRequest = EntityRequestDto.builder()
                    .field("Updated")
                    .uniqueField("unique123")
                    .build();

            given()
                .contentType(ContentType.JSON)
                .body(updateRequest)
            .when()
                .put(baseUrl + "/" + saved.getId())
            .then()
                .statusCode(200)
                .body("field", equalTo("Updated"));
        }

        @Test
        @DisplayName("Should return 404 when updating non-existent entity")
        void shouldReturn404WhenUpdatingNonExistentEntity() {
            given()
                .contentType(ContentType.JSON)
                .body(validRequest)
            .when()
                .put(baseUrl + "/999")
            .then()
                .statusCode(404);
        }
    }

    @Nested
    @DisplayName("DELETE /api/v1/entities/{id} - Delete Entity")
    class DeleteEntityTests {

        @Test
        @DisplayName("Should delete entity successfully with 204 status")
        void shouldDeleteEntitySuccessfully() {
            // Create entity
            EntityName saved = entityRepository.save(EntityName.builder()
                    .field("Test")
                    .uniqueField("unique123")
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build());

            given()
            .when()
                .delete(baseUrl + "/" + saved.getId())
            .then()
                .statusCode(204);
        }

        @Test
        @DisplayName("Should return 404 when deleting non-existent entity")
        void shouldReturn404WhenDeletingNonExistentEntity() {
            given()
            .when()
                .delete(baseUrl + "/999")
            .then()
                .statusCode(404);
        }
    }
}
```

**Integration Test Rules**:
- Use `@IntegrationTest` custom annotation
- Use `@SpringBootTest(webEnvironment = RANDOM_PORT)`
- Use `@DirtiesContext` to reset database between tests
- Use `@LocalServerPort` to get random port
- Use REST Assured for HTTP testing
- Clean database in `@BeforeEach`
- Test HTTP status codes, response body, headers
- Use Hamcrest matchers (`equalTo()`, `notNullValue()`, `containsString()`)

---

### 3. Repository Test Template

```java
package com.sparks.patient.repository;

import com.sparks.patient.entity.EntityName;
import com.sparks.patient.test.IntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@IntegrationTest
@DataJpaTest
@ActiveProfiles("test")
@DisplayName("Entity Repository Tests")
class EntityRepositoryTest {

    @Autowired
    private EntityRepository entityRepository;

    private EntityName entity;

    @BeforeEach
    void setUp() {
        entityRepository.deleteAll();

        entity = EntityName.builder()
                .field("Test Value")
                .uniqueField("unique123")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("Should save entity successfully")
    void shouldSaveEntitySuccessfully() {
        // When
        EntityName saved = entityRepository.save(entity);

        // Then
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getField()).isEqualTo("Test Value");
        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(saved.getUpdatedAt()).isNotNull();
    }

    @Test
    @DisplayName("Should find entity by unique field")
    void shouldFindEntityByUniqueField() {
        // Given
        entityRepository.save(entity);

        // When
        Optional<EntityName> found = entityRepository.findByUniqueField("unique123");

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getField()).isEqualTo("Test Value");
    }

    @Test
    @DisplayName("Should return empty when entity not found by unique field")
    void shouldReturnEmptyWhenEntityNotFoundByUniqueField() {
        // When
        Optional<EntityName> found = entityRepository.findByUniqueField("nonexistent");

        // Then
        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("Should check existence by unique field")
    void shouldCheckExistenceByUniqueField() {
        // Given
        entityRepository.save(entity);

        // When & Then
        assertThat(entityRepository.existsByUniqueField("unique123")).isTrue();
        assertThat(entityRepository.existsByUniqueField("nonexistent")).isFalse();
    }

    @Test
    @DisplayName("Should find all entities")
    void shouldFindAllEntities() {
        // Given
        entityRepository.save(entity);
        entityRepository.save(EntityName.builder()
                .field("Second")
                .uniqueField("unique456")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build());

        // When
        List<EntityName> entities = entityRepository.findAll();

        // Then
        assertThat(entities).hasSize(2);
    }

    @Test
    @DisplayName("Should delete entity successfully")
    void shouldDeleteEntitySuccessfully() {
        // Given
        EntityName saved = entityRepository.save(entity);

        // When
        entityRepository.deleteById(saved.getId());

        // Then
        assertThat(entityRepository.findById(saved.getId())).isEmpty();
    }
}
```

**Repository Test Rules**:
- Use `@IntegrationTest` custom annotation
- Use `@DataJpaTest` for JPA slice testing
- Use `@ActiveProfiles("test")`
- Clean database in `@BeforeEach`
- Test CRUD operations
- Test custom query methods
- Use AssertJ assertions

---

## Testing Best Practices

### 1. Test Naming Convention

```java
// Method name format: should[ExpectedBehavior]When[StateUnderTest]
@Test
@DisplayName("Should create patient successfully")
void shouldCreatePatientSuccessfully() { }

@Test
@DisplayName("Should throw exception when email already exists")
void shouldThrowExceptionWhenEmailAlreadyExists() { }
```

### 2. Given-When-Then Pattern

```java
@Test
void testExample() {
    // Given - Set up test data and mocks
    when(repository.findById(1L)).thenReturn(Optional.of(entity));

    // When - Execute the method under test
    EntityResponseDto result = service.getEntityById(1L);

    // Then - Verify the results
    assertThat(result).isNotNull();
    verify(repository).findById(1L);
}
```

### 3. Use @Nested Classes for Organization

```java
@Nested
@DisplayName("Create Entity Tests")
class CreateEntityTests {
    // All create tests here
}

@Nested
@DisplayName("Update Entity Tests")
class UpdateEntityTests {
    // All update tests here
}
```

### 4. AssertJ Assertions (Preferred)

```java
// Good - fluent and readable
assertThat(result).isNotNull();
assertThat(result.getId()).isEqualTo(1L);
assertThat(list).hasSize(3);
assertThat(list).isEmpty();
assertThat(optional).isPresent();
assertThat(result.getField()).isEqualTo("Expected");

// Exception assertions
assertThatThrownBy(() -> service.method())
    .isInstanceOf(CustomException.class)
    .hasMessageContaining("expected message");
```

### 5. Mock Verification

```java
// Verify method was called
verify(repository).save(any(Entity.class));

// Verify with specific argument
verify(repository).findById(1L);

// Verify number of invocations
verify(repository, times(2)).save(any());

// Verify no interactions
verifyNoInteractions(mapper);

// Verify no more interactions
verifyNoMoreInteractions(repository);

// Verify never called
verify(repository, never()).deleteById(anyLong());
```

### 6. REST Assured Patterns

```java
// Basic request
given()
    .contentType(ContentType.JSON)
    .body(request)
.when()
    .post("/api/v1/entities")
.then()
    .statusCode(201)
    .body("id", notNullValue());

// With path parameters
given()
.when()
    .get("/api/v1/entities/{id}", 1L)
.then()
    .statusCode(200);

// With query parameters
given()
    .queryParam("status", "ACTIVE")
.when()
    .get("/api/v1/entities")
.then()
    .statusCode(200);
```

---

## Coverage Goals

Target coverage (measured with JaCoCo):
- **Line Coverage**: 80%+
- **Branch Coverage**: 70%+
- **Service Layer**: 90%+ (critical business logic)
- **Controller Layer**: 80%+
- **Repository Layer**: 70%+

### Generate Coverage Report

```bash
mvn test jacoco:report
open target/site/jacoco/index.html
```

---

## Test Data Builders

Create reusable test data builders for complex entities:

```java
public class EntityTestDataBuilder {

    public static EntityRequestDto buildValidRequest() {
        return EntityRequestDto.builder()
                .field("Test Value")
                .uniqueField("unique" + System.currentTimeMillis())
                .email("test@example.com")
                .build();
    }

    public static EntityName buildEntity(Long id) {
        return EntityName.builder()
                .id(id)
                .field("Test Value")
                .uniqueField("unique" + id)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    public static EntityResponseDto buildResponse(Long id) {
        return EntityResponseDto.builder()
                .id(id)
                .field("Test Value")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }
}
```

---

## Common Test Scenarios

### Test Validation

```java
@Test
@DisplayName("Should validate required fields")
void shouldValidateRequiredFields() {
    EntityRequestDto invalid = EntityRequestDto.builder().build();

    given()
        .contentType(ContentType.JSON)
        .body(invalid)
    .when()
        .post(baseUrl)
    .then()
        .statusCode(400)
        .body("errors.field", hasItem(containsString("required")));
}
```

### Test Pagination

```java
@Test
@DisplayName("Should paginate results")
void shouldPaginateResults() {
    given()
        .queryParam("page", 0)
        .queryParam("size", 10)
    .when()
        .get(baseUrl)
    .then()
        .statusCode(200)
        .body("content", hasSize(lessThanOrEqualTo(10)));
}
```

### Test Transactional Rollback

```java
@Test
@DisplayName("Should rollback on exception")
void shouldRollbackOnException() {
    // Given
    when(repository.save(any())).thenThrow(new RuntimeException("DB error"));

    // When & Then
    assertThatThrownBy(() -> service.createEntity(request))
        .isInstanceOf(RuntimeException.class);

    // Verify nothing was saved
    assertThat(repository.findAll()).isEmpty();
}
```

---

## Testing Checklist

Before completing test suite for a feature:

- [ ] Unit tests for all service methods
- [ ] Unit tests for success scenarios
- [ ] Unit tests for exception scenarios
- [ ] Unit tests verify mock interactions
- [ ] Integration tests for all controller endpoints
- [ ] Integration tests verify HTTP status codes
- [ ] Integration tests verify response body structure
- [ ] Integration tests test validation errors
- [ ] Repository tests for custom queries
- [ ] Test coverage meets targets (80%+)
- [ ] All tests pass with `mvn test`
- [ ] Fast tests pass with `mvn test -Pfast`
- [ ] Tests use proper annotations (@UnitTest, @IntegrationTest)
- [ ] Tests use @Nested for organization
- [ ] Tests use @DisplayName for clarity
- [ ] Tests follow Given-When-Then pattern

---

## Common Testing Issues & Solutions

### Issue: Flaky Tests
**Solution**: Use `@DirtiesContext`, avoid shared mutable state, clean database in `@BeforeEach`

### Issue: Slow Tests
**Solution**: Use unit tests with mocks instead of integration tests where possible, use `-Pfast` profile

### Issue: Low Coverage
**Solution**: Add tests for exception paths, edge cases, and negative scenarios

### Issue: Test Pollution
**Solution**: Use `@DirtiesContext`, clean repositories in `@BeforeEach`, avoid static state

### Issue: REST Assured Port Issues
**Solution**: Always use `@LocalServerPort` and set `RestAssured.port` in `@BeforeEach`

---

## When to Ask for Help

**Ask the Coding Agent** if you need:
- Production code implementation
- New endpoints or services
- Bug fixes in production code

**Ask the Code Review Agent** if you need:
- Test quality review
- Coverage analysis
- Test organization feedback

**Ask the User** if:
- Test requirements are unclear
- Coverage targets need adjustment
- Test execution is failing consistently

---

## Final Notes

- **Test first, ask questions later**: If unsure about behavior, write a test to verify
- **Fast tests are good tests**: Prefer unit tests over integration tests when possible
- **Comprehensive is better than quick**: Test edge cases, exceptions, validations
- **Clean tests are maintainable tests**: Use builders, helpers, and clear names
- **Coverage is a guide, not a goal**: 100% coverage with poor tests is worse than 80% with quality tests

Happy testing! 🧪
