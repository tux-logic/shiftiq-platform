CREATE TABLE IF NOT EXISTS branch_analytics_snapshots (
    id UUID PRIMARY KEY,
    branch_id UUID NOT NULL,
    snapshot_date DATE NOT NULL,
    total_revenue NUMERIC(12, 2) NOT NULL DEFAULT 0.00,
    completed_work_orders_count INT NOT NULL DEFAULT 0,
    total_appointments_count INT NOT NULL DEFAULT 0,
    low_stock_alerts_count INT NOT NULL DEFAULT 0,
    dtc_alerts_count INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT uk_branch_analytics_date UNIQUE (branch_id, snapshot_date)
);

CREATE INDEX IF NOT EXISTS idx_branch_analytics_branch_date ON branch_analytics_snapshots(branch_id, snapshot_date);
