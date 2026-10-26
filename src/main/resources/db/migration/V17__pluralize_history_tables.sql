-- V17: Align history table names with the SnakeCaseWithPluralizedTablePhysicalNamingStrategy
-- Hibernate expects physical table names in plural. V16 created 'staff_career_history' and
-- 'workshop_specialty_history' in singular, which made schema validation fail with
-- "missing table [staff_career_histories]". Rename both tables (data + constraints travel
-- with the table), then recreate indexes and comments under the plural names.

-- 1) staff_career_history -> staff_career_histories
ALTER TABLE IF EXISTS staff_career_history RENAME TO staff_career_histories;

DROP INDEX IF EXISTS idx_staff_career_history_employee_branch;
CREATE INDEX IF NOT EXISTS idx_staff_career_histories_employee_branch
    ON staff_career_histories (employee_id, branch_id, recorded_at DESC);

DROP INDEX IF EXISTS idx_staff_career_history_registration;
CREATE INDEX IF NOT EXISTS idx_staff_career_histories_registration
    ON staff_career_histories (registration_id, recorded_at DESC);

COMMENT ON TABLE staff_career_histories IS 'Chronological audit snapshots of staff branch career assignments, specialties, and compensation';

-- 2) workshop_specialty_history -> workshop_specialty_histories
ALTER TABLE IF EXISTS workshop_specialty_history RENAME TO workshop_specialty_histories;

DROP INDEX IF EXISTS idx_workshop_specialty_history_specialty;
CREATE INDEX IF NOT EXISTS idx_workshop_specialty_histories_specialty
    ON workshop_specialty_histories (specialty_id, recorded_at DESC);

DROP INDEX IF EXISTS idx_workshop_specialty_history_workshop;
CREATE INDEX IF NOT EXISTS idx_workshop_specialty_histories_workshop
    ON workshop_specialty_histories (workshop_id, recorded_at DESC);

COMMENT ON TABLE workshop_specialty_histories IS 'Chronological revision history for workshop specialty catalog definitions';
