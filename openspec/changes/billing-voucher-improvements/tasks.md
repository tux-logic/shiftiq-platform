# Tasks

## 1. Customer Tax Document Validation

- [x] 1.1 Add document validation rules to `GenerateVoucherCommand`, `ProcessCheckoutCommand`, and `Voucher` (requiring 11-digit RUC for `INVOICE` and valid personal document format for `RECEIPT`) and verify with unit tests.
- [x] 1.2 Add i18n messages for voucher tax document validation and update `VouchersController` and `CheckoutsController` error mapping to return appropriate 400 Bad Request responses.

## 2. Workshop-Scoped Fiscal Sequences

- [x] 2.1 Update `VoucherSequencePersistenceEntity` and `VoucherSequenceJpaRepository` to scope sequences by `(workshopId, series)` with composite primary key `VoucherSequenceId` and verify repository query methods.
- [x] 2.2 Update `FiscalCorrelativeService` interface and `FiscalCorrelativeServiceImpl` to allocate consecutive correlatives partitioned by `workshopId` and `series` with pessimistic locking, and verify with unit tests.
- [x] 2.3 Update `VoucherExecutionService` and `VoucherCommandServiceImpl` to resolve the quote's workshop ID and reserve workshop-scoped correlatives during voucher preparation and recovery.

## 3. Discount Prorating on Billing Line Items

- [x] 3.1 Update `getDetailedBillingItems` in `VoucherCommandServiceImpl` to prorate quote discounts across tasks and products with centavo residual correction so that `sum(items) == quote.getTotalAmount().amount()`, and verify with unit tests.

## 4. Fiscal Identifiers on Domain and REST Resources

- [ ] 4.1 Update `Voucher` aggregate and `VoucherPersistenceEntity` to store `series` alongside `correlative` and expose a formatted fiscal document identifier (e.g., `F001-00000001`).
- [ ] 4.2 Update `VoucherResource`, `VoucherResourceFromAggregateAssembler`, and `VoucherPersistenceAssembler` to include `series`, `correlative`, and `documentNumber`, and verify with assembler tests.

## 5. Verification and Integration

- [ ] 5.1 Run full billing test suite and `./gradlew test` to ensure zero regressions across billing, operations, and shared contexts.
