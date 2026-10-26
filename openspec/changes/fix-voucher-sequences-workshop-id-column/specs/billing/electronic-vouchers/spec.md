# Spec Delta: billing/electronic-vouchers

## MODIFIED Requirements

### Requirement: Fiscal voucher sequences are stored with workshop association

The system SHALL persist fiscal voucher sequences with the workshop association expected by the persistence model (`voucher_sequences.workshop_id` NOT NULL), so that Hibernate schema validation (`ddl-auto=validate`) passes against any database where Flyway migrations V1–V18 have been applied.

#### Scenario: Production startup validates schema successfully
- **WHEN** the application starts against a database with migrations V1–V18 applied and `spring.jpa.hibernate.ddl-auto=validate`
- **THEN** Hibernate schema validation succeeds and no missing-column error is reported for `voucher_sequences.workshop_id`

#### Scenario: Existing seeded sequences are reconciled
- **WHEN** migrations V1–V18 are applied to a database that already contains seeded `B001`/`F001` rows without `workshop_id`
- **THEN** the rows are reconciled (associated or removed and re-seeded) so the NOT NULL constraint holds without validation errors
