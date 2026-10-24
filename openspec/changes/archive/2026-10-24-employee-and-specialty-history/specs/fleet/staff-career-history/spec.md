# Spec Delta

## Purpose

Enables tracking, auditing, and querying chronological employment snapshots for branch staff, including specialty assignments, salary revisions, and status transitions.

## ADDED Requirements

### Requirement: Record career snapshot on staff registration events
The system SHALL record an immutable career snapshot whenever an employee registration is created, updated, approved, rejected, or deactivated.

#### Scenario: Snapshot on registration approval
- **WHEN** a branch manager approves an employee registration
- **THEN** an immutable career snapshot is recorded with status ACTIVE, current specialty, salary, and the approving user ID

#### Scenario: Snapshot on specialty or salary update
- **WHEN** an authorized user updates an employee's specialty or salary in a branch
- **THEN** a new career snapshot is appended recording the previous and updated values, effective timestamp, and author of change

#### Scenario: Snapshot on deactivation
- **WHEN** an employee registration is deactivated or rejected
- **THEN** a career snapshot is appended recording the terminal status and deactivation timestamp

### Requirement: Query staff career history by employee
The system SHALL provide an authorized query endpoint to retrieve the complete chronological career history of a staff member within a branch.

#### Scenario: Retrieve employee history in branch
- **WHEN** an authorized branch manager requests the history for an employee in their branch
- **THEN** the system returns a list of career snapshots ordered chronologically by effective date descending

#### Scenario: Unauthorized branch access denied
- **WHEN** a user without authorization for the branch attempts to view staff career history
- **THEN** the system rejects the request with HTTP 403 Forbidden
