# Selective Test Execution Guide

This project supports running specific test groups to speed up development and CI/CD pipelines.

## Quick Commands

### Run ALL tests (default)
```bash
mvn test
```
**Time**: ~8 minutes (193 tests)

### Run ONLY Unit Tests (fastest)
```bash
mvn test -Punit
```
**Time**: ~1-2 minutes (53 unit tests)  
**Use when**: Working on business logic, service layer changes

### Run ONLY Integration Tests
```bash
mvn test -Pintegration
```
**Time**: ~6 minutes (140 integration tests)  
**Use when**: Testing database operations, API endpoints

### Run Unit Tests in Parallel (super fast)
```bash
mvn test -Pfast
```
**Time**: ~30-45 seconds (53 unit tests, 4 parallel threads)  
**Use when**: Quick validation during development

### Run Tests by Package

#### Service Layer Tests Only
```bash
mvn test -Pservice
```
**Time**: ~1-2 minutes  
**Use when**: Working on service implementations

#### Repository/Database Tests Only
```bash
mvn test -Prepository
```
**Time**: ~3-4 minutes  
**Use when**: Working on database queries, entities

#### API/Controller Tests Only
```bash
mvn test -Papi
```
**Time**: ~2-3 minutes  
**Use when**: Working on REST endpoints

### Run Specific Test Class
```bash
mvn test -Dtest=PatientServiceImplTest
```

### Run Specific Test Method
```bash
mvn test -Dtest=PatientServiceImplTest#shouldCreatePatientSuccessfully
```

### Run Multiple Test Classes
```bash
mvn test -Dtest=PatientServiceImplTest,ShiftServiceImplTest
```

### Run Tests Matching Pattern
```bash
mvn test -Dtest=*ServiceImplTest
```

## Test Categories

### Unit Tests (@UnitTest)
- Fast execution (<2 seconds each)
- No Spring context required
- Mock all dependencies
- Located in: `src/test/java/**/service/`
- Total: 53 tests

### Integration Tests (@IntegrationTest)
- Slower execution (with Spring context)
- Real database operations
- Full application context
- Located in: `src/test/java/**/integration/` and `src/test/java/**/repository/`
- Total: 140 tests

## Recommended Workflows

### During Active Development
```bash
# 1. Work on code
# 2. Run fast unit tests frequently
mvn test -Pfast

# 3. Before commit, run related integration tests
mvn test -Pintegration
```

### Before Pull Request
```bash
# Run all tests
mvn clean install
```

### CI/CD Pipeline
```yaml
# Example GitHub Actions workflow
stages:
  - name: Unit Tests
    run: mvn test -Punit
  
  - name: Integration Tests
    run: mvn test -Pintegration
```

## Test Execution Times (Approximate)

| Command | Tests | Time | Use Case |
|---------|-------|------|----------|
| `mvn test` | 193 | 8m | Full validation |
| `mvn test -Pfast` | 53 | 45s | Quick feedback |
| `mvn test -Punit` | 53 | 2m | Business logic |
| `mvn test -Pintegration` | 140 | 6m | Database/API |
| `mvn test -Pservice` | 53 | 2m | Service layer |
| `mvn test -Prepository` | 18 | 3m | Database |

## Skip Tests
```bash
# Skip all tests during build
mvn clean install -DskipTests

# or
mvn clean install -Dmaven.test.skip=true
```

## Coverage Reports

After running tests, view coverage at:
```
target/site/jacoco/index.html
```

## Tips

1. **For TDD**: Use `mvn test -Pfast` for rapid feedback
2. **For refactoring**: Run related unit tests first, then integration
3. **For bug fixes**: Run the specific failing test in watch mode
4. **For CI**: Run unit and integration tests separately for better error reporting
5. **Save time**: Only run integration tests when you change database/API code

## Maven Watch Mode (Bonus)

Install Maven wrapper with watch:
```bash
# In another terminal
mvn compile quarkus:dev  # or similar watch mode

# Then run tests on save
mvn test -Pfast
```
