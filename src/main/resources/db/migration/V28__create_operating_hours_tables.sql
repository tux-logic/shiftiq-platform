CREATE TABLE IF NOT EXISTS operating_hours (
    id UUID PRIMARY KEY,
    workshop_id UUID REFERENCES workshops(id),
    branch_id UUID REFERENCES branches(id),
    day_of_week VARCHAR(15) NOT NULL,
    is_open BOOLEAN NOT NULL,
    open_time TIME,
    close_time TIME,
    slot_duration_minutes INT NOT NULL DEFAULT 60,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_operating_hours_scope CHECK (
        (workshop_id IS NOT NULL AND branch_id IS NULL)
        OR (branch_id IS NOT NULL)
    )
);

CREATE UNIQUE INDEX IF NOT EXISTS uk_operating_hours_workshop_day
    ON operating_hours (workshop_id, day_of_week)
    WHERE branch_id IS NULL;

CREATE UNIQUE INDEX IF NOT EXISTS uk_operating_hours_branch_day
    ON operating_hours (branch_id, day_of_week)
    WHERE branch_id IS NOT NULL;

CREATE TABLE IF NOT EXISTS operating_hour_exceptions (
    id UUID PRIMARY KEY,
    workshop_id UUID REFERENCES workshops(id),
    branch_id UUID REFERENCES branches(id),
    exception_date DATE NOT NULL,
    is_open BOOLEAN NOT NULL,
    open_time TIME,
    close_time TIME,
    reason VARCHAR(200),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_operating_hour_exceptions_scope CHECK (
        (workshop_id IS NOT NULL AND branch_id IS NULL)
        OR (branch_id IS NOT NULL)
    )
);

CREATE UNIQUE INDEX IF NOT EXISTS uk_operating_hour_exceptions_workshop_date
    ON operating_hour_exceptions (workshop_id, exception_date)
    WHERE branch_id IS NULL;

CREATE UNIQUE INDEX IF NOT EXISTS uk_operating_hour_exceptions_branch_date
    ON operating_hour_exceptions (branch_id, exception_date)
    WHERE branch_id IS NOT NULL;
