-- V14: Create workshop_specialties table and seed default specialties for existing workshops

CREATE TABLE IF NOT EXISTS workshop_specialties (
    id UUID PRIMARY KEY,
    workshop_id UUID NOT NULL REFERENCES workshops(id) ON DELETE CASCADE,
    name VARCHAR(100) NOT NULL,
    code VARCHAR(50) NOT NULL,
    description VARCHAR(255),
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    deleted_at TIMESTAMP WITHOUT TIME ZONE,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT uq_workshop_specialties_workshop_code UNIQUE (workshop_id, code)
);

CREATE INDEX IF NOT EXISTS idx_workshop_specialties_workshop_id
    ON workshop_specialties(workshop_id);

-- Seed standard default specialties for all existing workshops
INSERT INTO workshop_specialties (id, workshop_id, name, code, description, is_active, created_at, updated_at, version)
SELECT 
    gen_random_uuid(),
    w.id,
    'Mecánica General',
    'GENERAL_MECHANIC',
    'Mantenimiento preventivo, correctivo y reparación de motores y sistemas mecánicos',
    TRUE,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP,
    0
FROM workshops w
WHERE NOT EXISTS (
    SELECT 1 FROM workshop_specialties s WHERE s.workshop_id = w.id AND s.code = 'GENERAL_MECHANIC'
);

INSERT INTO workshop_specialties (id, workshop_id, name, code, description, is_active, created_at, updated_at, version)
SELECT 
    gen_random_uuid(),
    w.id,
    'Electricidad y Electrónica',
    'ELECTRICIAN',
    'Diagnóstico y reparación de circuitos eléctricos, sensores y cableado automotriz',
    TRUE,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP,
    0
FROM workshops w
WHERE NOT EXISTS (
    SELECT 1 FROM workshop_specialties s WHERE s.workshop_id = w.id AND s.code = 'ELECTRICIAN'
);

INSERT INTO workshop_specialties (id, workshop_id, name, code, description, is_active, created_at, updated_at, version)
SELECT 
    gen_random_uuid(),
    w.id,
    'Planchado y Pintura',
    'BODYWORK_PAINT',
    'Reparación de carrocería, desabollado, pintura al horno y acabados',
    TRUE,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP,
    0
FROM workshops w
WHERE NOT EXISTS (
    SELECT 1 FROM workshop_specialties s WHERE s.workshop_id = w.id AND s.code = 'BODYWORK_PAINT'
);

INSERT INTO workshop_specialties (id, workshop_id, name, code, description, is_active, created_at, updated_at, version)
SELECT 
    gen_random_uuid(),
    w.id,
    'Diagnóstico Computarizado',
    'DIAGNOSTIC',
    'Escaneo OBD-II, lectura de DTCs y reprogramación de módulos electrónicos',
    TRUE,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP,
    0
FROM workshops w
WHERE NOT EXISTS (
    SELECT 1 FROM workshop_specialties s WHERE s.workshop_id = w.id AND s.code = 'DIAGNOSTIC'
);

INSERT INTO workshop_specialties (id, workshop_id, name, code, description, is_active, created_at, updated_at, version)
SELECT 
    gen_random_uuid(),
    w.id,
    'Alineación y Suspensión',
    'TIRE_ALIGNMENT',
    'Alineación, balanceo, frenos, amortiguadores y tren delantero',
    TRUE,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP,
    0
FROM workshops w
WHERE NOT EXISTS (
    SELECT 1 FROM workshop_specialties s WHERE s.workshop_id = w.id AND s.code = 'TIRE_ALIGNMENT'
);

COMMENT ON TABLE workshop_specialties IS 'Configurable catalog of specialties per workshop for technician onboarding';
COMMENT ON COLUMN users.role IS 'enum: ROLE_USER, ROLE_ADMIN, ROLE_EMPLOYEE, ROLE_OWNER, ROLE_BRANCH_MANAGER, ROLE_ASSISTANT';
