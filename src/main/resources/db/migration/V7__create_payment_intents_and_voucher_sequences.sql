-- V7: Create payment_intents and voucher_sequences tables

CREATE TABLE IF NOT EXISTS voucher_sequences (
    series VARCHAR(10) PRIMARY KEY,
    last_correlative INT NOT NULL DEFAULT 0
);

-- Seed initial series for SUNAT (B001 for boleta, F001 for factura)
INSERT INTO voucher_sequences (series, last_correlative) 
VALUES ('B001', 0), ('F001', 0)
ON CONFLICT (series) DO NOTHING;

CREATE TABLE IF NOT EXISTS payment_intents (
    id UUID PRIMARY KEY,
    quote_id UUID NOT NULL UNIQUE,
    voucher_type VARCHAR(20) NOT NULL,
    customer_document_type VARCHAR(20) NOT NULL,
    customer_document_number VARCHAR(20) NOT NULL,
    customer_name VARCHAR(150) NOT NULL,
    amount DECIMAL(10,2) NOT NULL,
    currency VARCHAR(3) NOT NULL DEFAULT 'PEN',
    status VARCHAR(20) NOT NULL DEFAULT 'CREATED',
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);
