# Functional Specification Document

## Patient Management Service

| Document Info | |
|--------------|---|
| **Version** | 1.0 |
| **Date** | January 26, 2026 |
| **Epic** | [SCRUM-13 - Patient Management](https://raghupardhu.atlassian.net/browse/SCRUM-13) |
| **Status** | In Review |

---

## 1. Executive Summary

The Patient Management Service is a core component of the Sparks Healthcare Platform that provides RESTful APIs for patient onboarding, profile management, and patient search capabilities. This service enables healthcare providers to register new patients, retrieve patient information, and manage patient records efficiently.

---

## 2. Business Objectives

| Objective | Description |
|-----------|-------------|
| **Patient Onboarding** | Enable seamless registration of new patients into the healthcare system |
| **Profile Management** | Provide CRUD operations for patient demographic information |
| **Data Integrity** | Ensure unique patient records with email-based deduplication |
| **API-First Approach** | Deliver well-documented REST APIs for integration with other services |

---

## 3. Scope

### 3.1 In Scope

| Story | Feature | Description |
|-------|---------|-------------|
| SCRUM-14 | Patient Onboarding API | Create new patient records via POST endpoint |
| SCRUM-15 | Patient Search & Retrieval | Fetch patient profiles by ID or list all patients |
| SCRUM-16 | Patient Schema & Entity Mapping | Define patient data model and database schema |

### 3.2 Out of Scope

- Patient authentication and authorization
- Medical records management
- Appointment scheduling (covered in SCRUM-21)
- Insurance and billing information
- Patient portal/UI

---

## 4. User Stories

### 4.1 SCRUM-14: Patient Onboarding API

**As a** healthcare administrator  
**I want to** register a new patient in the system  
**So that** they can receive healthcare services and their information is stored securely

#### Acceptance Criteria

| # | Criterion | Status |
|---|-----------|--------|
| 1 | API accepts patient demographic data (firstName, lastName, dob, email, phone) | ✅ |
| 2 | Returns HTTP 201 Created on successful registration | ✅ |
| 3 | Returns HTTP 400 Bad Request for invalid/missing fields | ✅ |
| 4 | Returns HTTP 409 Conflict if email already exists | ✅ |
| 5 | Automatically generates patient ID and timestamps | ✅ |

---

### 4.2 SCRUM-15: Patient Search & Profile Retrieval

**As a** healthcare provider  
**I want to** search for and retrieve patient profiles  
**So that** I can access patient information before consultations

#### Acceptance Criteria

| # | Criterion | Status |
|---|-----------|--------|
| 1 | API returns patient profile by ID | ✅ |
| 2 | Returns HTTP 200 OK with patient data when found | ✅ |
| 3 | Returns HTTP 404 Not Found for invalid patient ID | ✅ |
| 4 | API returns list of all patients | ✅ |
| 5 | Response includes all demographic fields and timestamps | ✅ |

---

### 4.3 SCRUM-16: Patient Schema & Entity Mapping

**As a** system architect  
**I want to** have a well-defined patient data model  
**So that** data integrity is maintained and the schema supports future extensions

#### Acceptance Criteria

| # | Criterion | Status |
|---|-----------|--------|
| 1 | Patient entity includes all required fields | ✅ |
| 2 | Email field has unique constraint | ✅ |
| 3 | All fields have appropriate validation rules | ✅ |
| 4 | Audit fields (createdAt, updatedAt) are auto-managed | ✅ |

---

## 5. Functional Requirements

### 5.1 Patient Registration (FR-001)

| Attribute | Description |
|-----------|-------------|
| **ID** | FR-001 |
| **Priority** | High |
| **Description** | System shall allow creation of new patient records |

**Input Fields:**

| Field | Required | Validation |
|-------|----------|------------|
| firstName | Yes | 2-100 characters |
| lastName | Yes | 2-100 characters |
| dob | Yes | Valid date, must be in the past |
| email | Yes | Valid email format, must be unique |
| phone | Yes | E.164 phone number format |

**Business Rules:**
- BR-001: Email addresses must be unique across all patients
- BR-002: Date of birth cannot be a future date
- BR-003: Phone numbers must follow international E.164 format

---

### 5.2 Patient Retrieval (FR-002)

| Attribute | Description |
|-----------|-------------|
| **ID** | FR-002 |
| **Priority** | High |
| **Description** | System shall allow retrieval of patient records by ID |

**Output Fields:**

| Field | Type | Description |
|-------|------|-------------|
| id | Long | Unique patient identifier |
| firstName | String | Patient's first name |
| lastName | String | Patient's last name |
| dob | Date | Date of birth |
| email | String | Email address |
| phone | String | Phone number |
| createdAt | DateTime | Record creation timestamp |
| updatedAt | DateTime | Last modification timestamp |

---

### 5.3 Patient List (FR-003)

| Attribute | Description |
|-----------|-------------|
| **ID** | FR-003 |
| **Priority** | Medium |
| **Description** | System shall return a list of all registered patients |

---

### 5.4 Patient Update (FR-004)

| Attribute | Description |
|-----------|-------------|
| **ID** | FR-004 |
| **Priority** | Medium |
| **Description** | System shall allow updating existing patient records |

**Business Rules:**
- BR-004: Cannot change email to one that already exists for another patient
- BR-005: updatedAt timestamp is automatically updated

---

### 5.5 Patient Deletion (FR-005)

| Attribute | Description |
|-----------|-------------|
| **ID** | FR-005 |
| **Priority** | Low |
| **Description** | System shall allow deletion of patient records |

---

## 6. Non-Functional Requirements

| ID | Category | Requirement |
|----|----------|-------------|
| NFR-001 | Performance | API response time < 200ms for single patient retrieval |
| NFR-002 | Availability | Service uptime of 99.9% |
| NFR-003 | Scalability | Support up to 10,000 patient records |
| NFR-004 | Security | All data must be validated before persistence |
| NFR-005 | Documentation | OpenAPI 3.0 specification available at `/api-docs` |

---

## 7. Error Handling

| HTTP Code | Scenario | Response |
|-----------|----------|----------|
| 201 | Patient created successfully | PatientResponse |
| 200 | Patient retrieved successfully | PatientResponse |
| 400 | Validation error (missing/invalid fields) | ErrorResponse with field details |
| 404 | Patient not found | ErrorResponse with message |
| 409 | Duplicate email | ErrorResponse with conflict message |
| 500 | Internal server error | ErrorResponse with generic message |

---

## 8. Data Dictionary

| Term | Definition |
|------|------------|
| Patient | An individual receiving healthcare services |
| Onboarding | The process of registering a new patient |
| Profile | Complete set of demographic information for a patient |
| DOB | Date of Birth |
| E.164 | International phone number format (e.g., +1234567890) |

---

## 9. Appendix

### 9.1 Related Jira Issues

| Key | Summary | Type |
|-----|---------|------|
| SCRUM-13 | Patient Management | Epic |
| SCRUM-14 | Patient Onboarding API | Story |
| SCRUM-15 | Patient Search & Profile Retrieval | Story |
| SCRUM-16 | Patient Schema & Entity Mapping | Story |

### 9.2 Document History

| Version | Date | Author | Changes |
|---------|------|--------|---------|
| 1.0 | 2026-01-26 | Sparks Team | Initial version |
