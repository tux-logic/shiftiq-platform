# billing/electronic-vouchers Specification

## Purpose
Enables multi-tenant fiscal voucher generation and compliance for electronic invoices (Facturas) and receipts (Boletas) with consecutive numbering per workshop, accurate discount application, and document validation under Peruvian tax regulations.

## Requirements

### Requirement: Workshop-Scoped Fiscal Sequences
The billing system SHALL maintain sequential, consecutive fiscal numbering scoped independently to each workshop and series, guaranteeing that distinct workshops have separate correlative counters starting from 1.

#### Scenario: Separate workshops receive independent correlatives
- **WHEN** Workshop A and Workshop B both generate their first Factura under series "F001"
- **THEN** both vouchers are assigned correlative "00000001" under their respective workshop scopes without collisions

#### Scenario: Consecutive increment within the same workshop
- **WHEN** Workshop A issues a second Factura under series "F001"
- **THEN** the system increments the sequence and assigns correlative "00000002" to Workshop A

### Requirement: Discount-Adjusted Item Pricing
The billing system SHALL prorate quote discounts across itemized tasks and products so that the sum of line item totals submitted to Factos matches the quote's discounted total amount.

#### Scenario: Line items prorated with quote discount
- **WHEN** an approved quote with a 10% discount is converted into a voucher with labor and product line items
- **THEN** each item's unit price or total is adjusted proportionally such that the sum of all items equals the discounted quote total amount

#### Scenario: Undiscounted quote preserves standard pricing
- **WHEN** an approved quote without discounts is converted into a voucher
- **THEN** line items retain their standard unit prices without adjustment

### Requirement: Tax Document Validation
The billing system SHALL validate the customer document type and number before issuing a voucher, requiring an 11-digit RUC for Facturas and valid personal identification documents for Boletas.

#### Scenario: Invoice generation with valid RUC succeeds
- **WHEN** a client requests an INVOICE with document type "RUC" and an 11-digit number
- **THEN** the system accepts the request and proceeds with voucher generation

#### Scenario: Invoice generation with non-RUC document fails
- **WHEN** a client requests an INVOICE with document type "DNI" or a number that does not contain exactly 11 digits
- **THEN** the system rejects the request with a validation error before calling the external invoicing authority

#### Scenario: Receipt generation with valid DNI succeeds
- **WHEN** a client requests a RECEIPT with document type "DNI" and an 8-digit number
- **THEN** the system accepts the request and proceeds with voucher generation

### Requirement: Fiscal Document Number Exposure
The billing system SHALL expose the fiscal series and sequential correlative in voucher API responses to enable clients to display the official document identifier.

#### Scenario: Querying voucher includes series and correlative
- **WHEN** a client retrieves a voucher by ID or lists vouchers by branch
- **THEN** the response includes the assigned series, correlative, and the combined fiscal voucher identifier
