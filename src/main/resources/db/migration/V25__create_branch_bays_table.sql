CREATE TABLE branch_bays (
    id UUID PRIMARY KEY,
    branch_id UUID NOT NULL REFERENCES branches(id) ON DELETE CASCADE,
    code VARCHAR(20) NOT NULL,
    name VARCHAR(100) NOT NULL,
    bay_type VARCHAR(20) NOT NULL DEFAULT 'FLOOR',
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    deleted_at TIMESTAMP WITHOUT TIME ZONE,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT uq_branch_bays_branch_code UNIQUE (branch_id, code),
    CONSTRAINT ck_branch_bays_type CHECK (bay_type IN ('LIFT', 'FLOOR', 'DIAG'))
);

CREATE INDEX idx_branch_bays_branch_id ON branch_bays(branch_id);
CREATE INDEX idx_branch_bays_branch_active ON branch_bays(branch_id, is_active);
