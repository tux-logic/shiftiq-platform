CREATE TABLE branch_reviews (
    id UUID PRIMARY KEY,
    branch_id UUID NOT NULL REFERENCES branches(id) ON DELETE CASCADE,
    customer_id UUID NOT NULL,
    appointment_id UUID,
    rating INTEGER NOT NULL,
    comment VARCHAR(1000),
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    deleted_at TIMESTAMP WITHOUT TIME ZONE,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT ck_branch_reviews_rating CHECK (rating BETWEEN 1 AND 5),
    CONSTRAINT uq_branch_reviews_appointment UNIQUE (appointment_id)
);

CREATE INDEX idx_branch_reviews_branch_id ON branch_reviews(branch_id);
CREATE INDEX idx_branch_reviews_customer_id ON branch_reviews(customer_id);
