# Proposal

## Why

The current electronic billing module (`billing`) successfully manages concurrency and transactional decoupling during voucher emission via Factos (SUNAT PSE). However, four functional and multi-tenant gaps limit its production readiness in a multi-workshop environment:
1. Fiscal correlative sequences (`voucher_sequences`) are stored in a single global table by series without workshop isolation, risking sequence collisions and SUNAT rejection across workshops.
2. Quote discounts (`discountPercentage`) are not reflected in the itemized list sent to Factos, creating tax calculation discrepancies between the total paid and the items registered at SUNAT.
3. Manual voucher and checkout endpoints (`VouchersController` and `CheckoutsController`) lack customer tax document validation for Facturas (requiring 11-digit RUC) versus Boletas (DNI/CE/Passport).
4. API resources (`VoucherResource`) omit the fiscal series and correlative (e.g., `F001-00000001`), preventing clients from displaying the official voucher code.

Addressing these issues now ensures tax compliance under SUNAT rules, maintains tenant isolation across workshops, and provides accurate fiscal documents to customers.

## What Changes

- **Workshop-Scoped Fiscal Sequences**: Partition fiscal numbering by workshop (or branch) and series (`workshop_id` + `series`) so that distinct workshops have independent numbering starting at 1 as required by SUNAT.
- **Accurate Line-Item Pricing with Discounts**: Prorate quote discounts across labor tasks and products in `getDetailedBillingItems` (or send discounted unit prices) so that `sum(lineItems) == quote.totalAmount`.
- **Tax Document Validation on Voucher Generation**: Enforce that `INVOICE` vouchers require `customerDocumentType` equal to `RUC` with exactly 11 digits, and `RECEIPT` vouchers accept valid personal identification (`DNI`, `CE`, `PASSPORT`), rejecting invalid inputs before calling Factos.
- **Voucher Resource Fiscal Identifiers**: Expose `series` and `correlative` (or composite `documentNumber`) in `VoucherResource` and domain mapping so frontends can present the official voucher number.

## Capabilities

### New Capabilities
- `billing/electronic-vouchers`: Covers fiscal voucher issuance, workshop-scoped consecutive correlatives, discount prorating on invoice items, and customer tax document validation for Peruvian electronic invoicing (SUNAT/Factos).

### Modified Capabilities
<!-- None. No existing specs in openspec/specs/. -->

## Impact

- **Domain & Application**: Updates to `Voucher`, `FiscalCorrelativeService`, `FiscalCorrelativeServiceImpl`, and `VoucherExecutionService`.
- **Infrastructure & Persistence**: `VoucherSequencePersistenceEntity` updated to composite key or workshop-aware sequence (`workshop_id`, `series`), schema migration or JPA mapping adjustment.
- **REST APIs**: `VouchersController`, `CheckoutsController`, `VoucherResource`, and assemblers updated to include `series` and `correlative`, plus document validation on creation.
- **Tests**: Unit and integration tests in `billing` context updated to verify multi-tenant sequence isolation, discount calculations, and validation rules.
