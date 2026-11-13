-- V21: Create hybrid workshop roles, role permissions, audit logs, and seed immutable system role templates

CREATE TABLE IF NOT EXISTS workshop_roles
(
    id          UUID                        NOT NULL PRIMARY KEY,
    workshop_id UUID                        NULL,
    name        VARCHAR(80)                 NOT NULL,
    description VARCHAR(255),
    is_system   BOOLEAN                     NOT NULL DEFAULT FALSE,
    is_active   BOOLEAN                     NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE UNIQUE INDEX IF NOT EXISTS uq_workshop_roles_system_name
    ON workshop_roles (UPPER(name))
    WHERE workshop_id IS NULL;

CREATE UNIQUE INDEX IF NOT EXISTS uq_workshop_roles_workshop_name
    ON workshop_roles (workshop_id, UPPER(name))
    WHERE workshop_id IS NOT NULL;

CREATE INDEX IF NOT EXISTS idx_workshop_roles_workshop_active
    ON workshop_roles (workshop_id, is_active);

CREATE TABLE IF NOT EXISTS workshop_role_permissions
(
    workshop_role_id UUID        NOT NULL REFERENCES workshop_roles (id) ON DELETE CASCADE,
    permission       VARCHAR(64) NOT NULL,
    PRIMARY KEY (workshop_role_id, permission)
);

CREATE TABLE IF NOT EXISTS workshop_role_audit_logs
(
    id                   UUID                        NOT NULL PRIMARY KEY,
    workshop_id          UUID                        NOT NULL,
    workshop_role_id     UUID                        NOT NULL,
    actor_user_id        UUID                        NOT NULL,
    target_user_id       UUID                        NULL,
    action_type          VARCHAR(40)                 NOT NULL,
    role_name            VARCHAR(80)                 NOT NULL,
    permissions_snapshot TEXT                        NOT NULL,
    recorded_at          TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_workshop_role_audit_logs_workshop
    ON workshop_role_audit_logs (workshop_id, recorded_at DESC);

ALTER TABLE users
    ADD COLUMN IF NOT EXISTS workshop_role_id UUID NULL REFERENCES workshop_roles (id);

CREATE INDEX IF NOT EXISTS idx_users_workshop_role_id
    ON users (workshop_role_id);

-- Seed immutable system default role templates (workshop_id IS NULL, is_system = TRUE)
INSERT INTO workshop_roles (id, workshop_id, name, description, is_system, is_active)
VALUES ('00000000-0000-4000-8000-000000000001', NULL, 'ROLE_OWNER', 'Workshop owner with full workshop and branch privileges', TRUE, TRUE),
       ('00000000-0000-4000-8000-000000000002', NULL, 'ROLE_BRANCH_MANAGER', 'Branch manager with full operational and staff management privileges', TRUE, TRUE),
       ('00000000-0000-4000-8000-000000000003', NULL, 'ROLE_ASSISTANT', 'Service advisor with appointment, quote, and checkout privileges', TRUE, TRUE),
       ('00000000-0000-4000-8000-000000000004', NULL, 'ROLE_EMPLOYEE', 'Technician/mechanic with work order execution privileges', TRUE, TRUE),
       ('00000000-0000-4000-8000-000000000005', NULL, 'ROLE_WAREHOUSE', 'Warehouse/inventory manager for stock and product batches', TRUE, TRUE),
       ('00000000-0000-4000-8000-000000000006', NULL, 'ROLE_CASHIER', 'Billing cashier for voucher issuance and payment collection', TRUE, TRUE),
       ('00000000-0000-4000-8000-000000000007', NULL, 'ROLE_ACCOUNTANT', 'Read-only financial auditor for quotes and vouchers', TRUE, TRUE)
ON CONFLICT (id) DO NOTHING;

-- Seed permissions for ROLE_OWNER and ROLE_BRANCH_MANAGER
INSERT INTO workshop_role_permissions (workshop_role_id, permission)
SELECT r.id, p.permission
FROM (VALUES ('00000000-0000-4000-8000-000000000001'::UUID),
             ('00000000-0000-4000-8000-000000000002'::UUID)) AS r(id)
         CROSS JOIN (VALUES ('WORKORDER_READ'),
                             ('WORKORDER_EXECUTE'),
                             ('WORKORDER_DELETE'),
                             ('INVENTORY_READ'),
                             ('INVENTORY_MANAGE'),
                             ('SERVICE_MANAGE'),
                             ('QUOTE_READ'),
                             ('QUOTE_CREATE'),
                             ('QUOTE_APPROVE'),
                             ('QUOTE_CANCEL'),
                             ('VOUCHER_READ'),
                             ('CHECKOUT_PROCESS'),
                             ('APPOINTMENT_MANAGE'),
                             ('STAFF_READ'),
                             ('STAFF_MANAGE'),
                             ('SPECIALTY_MANAGE'),
                             ('BRANCH_READ'),
                             ('BRANCH_MANAGE'),
                             ('MEDIA_UPLOAD'),
                             ('ROLE_MANAGE')) AS p(permission)
ON CONFLICT DO NOTHING;

-- Seed permissions for ROLE_ASSISTANT
INSERT INTO workshop_role_permissions (workshop_role_id, permission)
VALUES ('00000000-0000-4000-8000-000000000003', 'WORKORDER_READ'),
       ('00000000-0000-4000-8000-000000000003', 'WORKORDER_EXECUTE'),
       ('00000000-0000-4000-8000-000000000003', 'INVENTORY_READ'),
       ('00000000-0000-4000-8000-000000000003', 'QUOTE_READ'),
       ('00000000-0000-4000-8000-000000000003', 'QUOTE_CREATE'),
       ('00000000-0000-4000-8000-000000000003', 'QUOTE_APPROVE'),
       ('00000000-0000-4000-8000-000000000003', 'VOUCHER_READ'),
       ('00000000-0000-4000-8000-000000000003', 'CHECKOUT_PROCESS'),
       ('00000000-0000-4000-8000-000000000003', 'APPOINTMENT_MANAGE'),
       ('00000000-0000-4000-8000-000000000003', 'STAFF_READ'),
       ('00000000-0000-4000-8000-000000000003', 'BRANCH_READ'),
       ('00000000-0000-4000-8000-000000000003', 'MEDIA_UPLOAD')
ON CONFLICT DO NOTHING;

-- Seed permissions for ROLE_EMPLOYEE
INSERT INTO workshop_role_permissions (workshop_role_id, permission)
VALUES ('00000000-0000-4000-8000-000000000004', 'WORKORDER_READ'),
       ('00000000-0000-4000-8000-000000000004', 'WORKORDER_EXECUTE'),
       ('00000000-0000-4000-8000-000000000004', 'INVENTORY_READ'),
       ('00000000-0000-4000-8000-000000000004', 'QUOTE_READ'),
       ('00000000-0000-4000-8000-000000000004', 'VOUCHER_READ'),
       ('00000000-0000-4000-8000-000000000004', 'STAFF_READ'),
       ('00000000-0000-4000-8000-000000000004', 'BRANCH_READ'),
       ('00000000-0000-4000-8000-000000000004', 'MEDIA_UPLOAD')
ON CONFLICT DO NOTHING;

-- Seed permissions for enterprise presets: ROLE_WAREHOUSE, ROLE_CASHIER, ROLE_ACCOUNTANT
INSERT INTO workshop_role_permissions (workshop_role_id, permission)
VALUES ('00000000-0000-4000-8000-000000000005', 'INVENTORY_READ'),
       ('00000000-0000-4000-8000-000000000005', 'INVENTORY_MANAGE'),
       ('00000000-0000-4000-8000-000000000005', 'BRANCH_READ'),
       ('00000000-0000-4000-8000-000000000005', 'MEDIA_UPLOAD'),

       ('00000000-0000-4000-8000-000000000006', 'QUOTE_READ'),
       ('00000000-0000-4000-8000-000000000006', 'VOUCHER_READ'),
       ('00000000-0000-4000-8000-000000000006', 'CHECKOUT_PROCESS'),
       ('00000000-0000-4000-8000-000000000006', 'BRANCH_READ'),

       ('00000000-0000-4000-8000-000000000007', 'QUOTE_READ'),
       ('00000000-0000-4000-8000-000000000007', 'VOUCHER_READ'),
       ('00000000-0000-4000-8000-000000000007', 'BRANCH_READ')
ON CONFLICT DO NOTHING;
