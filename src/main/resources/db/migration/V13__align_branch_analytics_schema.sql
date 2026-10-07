-- Drop the redundant secondary index: uk_branch_analytics_date UNIQUE (branch_id, snapshot_date)
-- already provides an index with the same leading columns.
DROP INDEX IF EXISTS idx_branch_analytics_branch_date;

-- Queue of analytics changes that could not be applied immediately and are replayed
-- by the scheduled job (see ReplayPendingAnalyticsDeltasJob).
CREATE TABLE IF NOT EXISTS branch_analytics_pending_deltas (
    id UUID PRIMARY KEY,
    branch_id UUID NOT NULL,
    delta_date DATE NOT NULL,
    revenue NUMERIC(12, 2) NOT NULL DEFAULT 0.00,
    work_orders INT NOT NULL DEFAULT 0,
    appointments INT NOT NULL DEFAULT 0,
    dtc_alerts INT NOT NULL DEFAULT 0,
    low_stock_recompute BOOLEAN NOT NULL DEFAULT FALSE,
    attempts INT NOT NULL DEFAULT 0,
    last_error VARCHAR(500),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_branch_analytics_pending_created
    ON branch_analytics_pending_deltas(created_at);
