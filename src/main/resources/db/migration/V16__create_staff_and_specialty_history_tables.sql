-- V16: Create staff_career_history and workshop_specialty_history tables for audit and timeline tracking

CREATE TABLE IF NOT EXISTS staff_career_history
(
    id              UUID                        NOT NULL PRIMARY KEY,
    registration_id UUID                        NOT NULL,
    employee_id     UUID                        NOT NULL,
    branch_id       UUID                        NOT NULL,
    speciality      VARCHAR(50)                 NOT NULL,
    speciality_name VARCHAR(100),
    salary          NUMERIC(10, 2),
    status          VARCHAR(30)                 NOT NULL,
    change_type     VARCHAR(50)                 NOT NULL,
    change_reason   VARCHAR(255),
    changed_by      UUID,
    recorded_at     TIMESTAMP WITHOUT TIME ZONE NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_staff_career_history_employee_branch
    ON staff_career_history (employee_id, branch_id, recorded_at DESC);

CREATE INDEX IF NOT EXISTS idx_staff_career_history_registration
    ON staff_career_history (registration_id, recorded_at DESC);

CREATE TABLE IF NOT EXISTS workshop_specialty_history
(
    id              UUID                        NOT NULL PRIMARY KEY,
    specialty_id    UUID                        NOT NULL,
    workshop_id     UUID                        NOT NULL,
    name            VARCHAR(100)                NOT NULL,
    code            VARCHAR(50)                 NOT NULL,
    description     VARCHAR(255),
    is_active       BOOLEAN                     NOT NULL DEFAULT TRUE,
    change_type     VARCHAR(50)                 NOT NULL,
    changed_by      UUID,
    recorded_at     TIMESTAMP WITHOUT TIME ZONE NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_workshop_specialty_history_specialty
    ON workshop_specialty_history (specialty_id, recorded_at DESC);

CREATE INDEX IF NOT EXISTS idx_workshop_specialty_history_workshop
    ON workshop_specialty_history (workshop_id, recorded_at DESC);

COMMENT ON TABLE staff_career_history IS 'Chronological audit snapshots of staff branch career assignments, specialties, and compensation';
COMMENT ON TABLE workshop_specialty_history IS 'Chronological revision history for workshop specialty catalog definitions';
