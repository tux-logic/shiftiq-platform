ALTER TABLE appointments
    ADD COLUMN bay_id UUID REFERENCES branch_bays(id);

CREATE INDEX idx_appointments_branch_bay_start
    ON appointments(branch_id, bay_id, scheduled_start)
    WHERE deleted_at IS NULL;
