# identity/document-validation Specification

## Purpose
Enables verification of Peruvian official identity documents (DNI, RUC, CE) and auto-completion of personal and business profiles through the Factiliza REST API service.

## Requirements

### Requirement: DNI Identity Lookup
The system SHALL provide an identity lookup endpoint for 8-digit Peruvian DNI numbers via Factiliza, returning standardized personal name and address data.

#### Scenario: Successful DNI lookup
- **WHEN** an authenticated or registration client requests validation for a valid 8-digit DNI number
- **THEN** the system queries Factiliza and returns HTTP 200 OK with `nombres`, `apellidoPaterno`, `apellidoMaterno`, `fullName`, and `address`

#### Scenario: Nonexistent DNI lookup
- **WHEN** a client requests validation for a DNI number that does not exist in RENIEC
- **THEN** the system returns HTTP 404 Not Found indicating that the document was not found

#### Scenario: Malformed DNI number
- **WHEN** a client provides a DNI parameter that does not conform to exactly 8 numeric digits
- **THEN** the system returns HTTP 400 Bad Request without calling Factiliza

### Requirement: RUC Fiscal Entity Lookup
The system SHALL provide an identity and tax lookup endpoint for 11-digit Peruvian RUC numbers via Factiliza, returning business name, status, and condition.

#### Scenario: Active and habido RUC lookup
- **WHEN** a client requests validation for a valid 11-digit RUC that is active and habido
- **THEN** the system returns HTTP 200 OK with `businessName`, `status`, `condition`, and `address`

#### Scenario: Inactive or not habido RUC lookup
- **WHEN** a client requests validation for an existing RUC that is inactive or not habido
- **THEN** the system returns HTTP 200 OK containing the status and condition details flagged as not active or not habido

#### Scenario: Nonexistent RUC lookup
- **WHEN** a client requests validation for a RUC number that does not exist in SUNAT
- **THEN** the system returns HTTP 404 Not Found indicating that the company was not found

### Requirement: Carnet de Extranjería Lookup
The system SHALL provide an identity lookup endpoint for Peruvian Carnet de Extranjería (CE / CEE) numbers via Factiliza Migraciones.

#### Scenario: Successful CE lookup
- **WHEN** a client requests validation for a valid CE number
- **THEN** the system returns HTTP 200 OK with the foreigner's first name, last name, and document details

#### Scenario: Nonexistent CE lookup
- **WHEN** a client requests validation for a CE number not found in Migraciones
- **THEN** the system returns HTTP 404 Not Found

### Requirement: Profile Creation Document Verification
The system SHALL validate identity and company document numbers against Factiliza when creating customer, employee, owner, and workshop records.

#### Scenario: Natural person customer creation with matching DNI
- **WHEN** a user creates a customer profile with `isCorporate=false` and a valid DNI
- **THEN** the system verifies the document with Factiliza and creates the customer record

#### Scenario: Corporate customer creation with active RUC
- **WHEN** a user creates a customer profile with `isCorporate=true` and a valid active RUC
- **THEN** the system verifies the RUC with Factiliza and creates the customer record

#### Scenario: Profile creation with nonexistent or invalid document
- **WHEN** a client submits a profile creation command with a document that Factiliza reports as nonexistent
- **THEN** the system rejects the creation and returns HTTP 400 Bad Request with an error message
