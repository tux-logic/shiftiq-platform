# Design

## Context

The billing bounded context handles quotes, payments, and electronic voucher issuance. In the current implementation:
- `FiscalCorrelativeServiceImpl` queries `VoucherSequenceJpaRepository` by `series` alone, without partitioning by workshop.
- `VoucherCommandServiceImpl.getDetailedBillingItems` queries tasks and products but does not adjust line item prices when a discount is applied to the quote.
- Document validation (RUC 11 digits) exists in `MercadoPagoPaymentsController`, but is missing in `GenerateVoucherCommand`, `ProcessCheckoutCommand`, and their respective REST endpoints.
- `VoucherResource` only exposes `externalInvoiceId` and `pdfUrl`, omitting `series` and `correlative`.

## Goals / Non-Goals

**Goals:**
- Isolate fiscal sequence numbering per workshop (`workshop_id` + `series`) so multi-tenant workshops maintain independent consecutive correlatives.
- Prorate quote discounts across line items sent to Factos, ensuring the line item sum matches `quote.getTotalAmount()`.
- Unify tax document validation across all voucher creation commands and REST endpoints (enforcing 11-digit RUC for Facturas and valid document formats for Boletas).
- Expose `series`, `correlative`, and formatted `documentNumber` (e.g., `F001-00000001`) in `VoucherResource` and domain entities.

**Non-Goals:**
- Implementing electronic Credit Notes (CPE 07) or Communication of Cancellation (`Comunicación de Baja`) with Factos in this change (deferred to a dedicated change).
- Modifying Mercado Pago preference checkout workflows beyond standardizing document validation.

## Decisions

### Decision 1: Workshop-Scoped Fiscal Sequence
- **Choice**: Scope `VoucherSequencePersistenceEntity` by `(workshopId, series)` using a composite primary key (`VoucherSequenceId`).
- **Rationale**: In Peruvian tax regulations (SUNAT), each workshop (tax entity / RUC) is an independent fiscal issuer. Distinct workshops must have separate counters starting at `00000001` for each series (`F001`, `B001`).
- **Alternative Considered**: Scoping by `branchId`. Rejected because multiple branches under the same workshop can share a series or use distinct series (`F001`, `F002`), but the fiscal boundary is the workshop's RUC. Scoping by `(workshopId, series)` accommodates both single-branch and multi-branch setups cleanly.

### Decision 2: Discount Prorating on Line Items
- **Choice**: In `getDetailedBillingItems`, when `quote.discountPercentage > 0`, multiply each line item's unit price by `(1.0 - discountPercentage / 100.0)` and round to 2 decimal places. Adjust any centavo rounding residual on the last line item so that `sum(lineItems) == quote.totalAmount.amount()`.
- **Rationale**: Factos computes taxes and total invoice amounts directly from line items (`TAXABLE_ONEROUS`). Factos does not support negative line items, so prorating unit prices ensures strict mathematical consistency between the quote total and the issued CPE.
- **Alternative Considered**: Sending a single consolidated service item for the discounted total. Rejected because customers and workshops require detailed breakdowns of tasks and spare parts on invoices.

### Decision 3: Domain and Command-Level Tax Document Validation
- **Choice**: Validate document types and formats in `GenerateVoucherCommand`, `ProcessCheckoutCommand`, and `Voucher`:
  - `INVOICE`: `customerDocumentType` must be `"RUC"` and `customerDocumentNumber` must match `^\d{11}$`.
  - `RECEIPT`: `customerDocumentType` must be `"DNI"`, `"CE"`, `"PASSPORT"`, or `"OTHER"`. If `"DNI"`, it must match `^\d{8}$`.
- **Rationale**: Fails fast before reserving correlatives or calling external APIs, returning clear localized errors (`billing.error.invoice.ruc.documentType`, `billing.error.invoice.ruc.number`).
- **Alternative Considered**: Validating only at the REST controller layer. Rejected because webhooks and internal commands would bypass validation.

### Decision 4: Expose Fiscal Identifiers
- **Choice**: Store `series` and `correlative` on `Voucher` and `VoucherPersistenceEntity`. Add `series`, `correlative`, and composite `documentNumber` (e.g. `F001-00000001`) to `VoucherResource`.
- **Rationale**: Frontends (mobile and web) need to display the official legal invoice code without downloading or parsing the PDF.

## Risks / Trade-offs

- **[Risk] Sequence table schema modification** → Use composite key `(workshop_id, series)` in `voucher_sequences`. If existing rows exist without `workshop_id`, handle them with a fallback or nullable column during initial table creation.
- **[Risk] Centavo rounding discrepancies when prorating discounts** → Absorb residual differences on the final line item so that `sum(items) == quote.totalAmount.amount()` with exact precision.
