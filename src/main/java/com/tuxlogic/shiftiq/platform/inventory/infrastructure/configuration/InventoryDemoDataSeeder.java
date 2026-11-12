package com.tuxlogic.shiftiq.platform.inventory.infrastructure.configuration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.UUID;

/**
 * Optional demo tenant aligned with the mobile inventory UI hardcoded branch IDs.
 * Enable with: shiftiq.inventory.seed-demo=true
 *
 * Credentials: inventory.demo@shiftiq.local / Password123!
 */
@Component
@ConditionalOnProperty(name = "shiftiq.inventory.seed-demo", havingValue = "true")
public class InventoryDemoDataSeeder implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(InventoryDemoDataSeeder.class);

    public static final UUID DEMO_USER_ID = UUID.fromString("a1000000-0000-0000-0000-000000000001");
    public static final UUID DEMO_OWNER_ID = UUID.fromString("a2000000-0000-0000-0000-000000000001");
    public static final UUID DEMO_WORKSHOP_ID = UUID.fromString("a3000000-0000-0000-0000-000000000001");
    public static final UUID BRANCH_CENTRAL = UUID.fromString("b1000000-0000-0000-0000-000000000001");
    public static final UUID BRANCH_NORTH = UUID.fromString("b1000000-0000-0000-0000-000000000002");
    public static final UUID BRANCH_SOUTH = UUID.fromString("b1000000-0000-0000-0000-000000000003");

    private static final UUID PRODUCT_BRAKE = UUID.fromString("c1000000-0000-0000-0000-000000000001");
    private static final UUID PRODUCT_FILTER = UUID.fromString("c1000000-0000-0000-0000-000000000002");
    private static final UUID PRODUCT_OIL = UUID.fromString("c1000000-0000-0000-0000-000000000003");
    private static final UUID BATCH_BRAKE = UUID.fromString("d1000000-0000-0000-0000-000000000001");
    private static final UUID BATCH_FILTER = UUID.fromString("d1000000-0000-0000-0000-000000000002");

    private final JdbcTemplate jdbcTemplate;
    private final PasswordEncoder passwordEncoder;

    public InventoryDemoDataSeeder(JdbcTemplate jdbcTemplate, PasswordEncoder passwordEncoder) {
        this.jdbcTemplate = jdbcTemplate;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (exists("SELECT 1 FROM branches WHERE id = ?", BRANCH_CENTRAL)) {
            log.info("Inventory demo tenant already present; skipping seed");
            ensureSampleProducts();
            return;
        }

        Timestamp now = Timestamp.from(Instant.now());
        String passwordHash = passwordEncoder.encode("Password123!");

        jdbcTemplate.update("""
                INSERT INTO users (id, email, password_hash, status, role, created_at, updated_at, version)
                VALUES (?, ?, ?, 'ACTIVE', 'ROLE_OWNER', ?, ?, 0)
                ON CONFLICT (id) DO NOTHING
                """, DEMO_USER_ID, "inventory.demo@shiftiq.local", passwordHash, now, now);

        jdbcTemplate.update("""
                INSERT INTO owners (id, user_id, first_name, last_name, document_type, document_number, phone, created_at, updated_at, version)
                VALUES (?, ?, 'Inventory', 'Demo', 'DNI', '12345678', '999888777', ?, ?, 0)
                ON CONFLICT (id) DO NOTHING
                """, DEMO_OWNER_ID, DEMO_USER_ID, now, now);

        jdbcTemplate.update("""
                INSERT INTO workshops (id, owner_id, business_name, brand_name, tax_id, mileage_interval_config, created_at, updated_at, version)
                VALUES (?, ?, 'Taller Demo ShiftIQ SAC', 'ShiftIQ Demo', '20123456789', 1, ?, ?, 0)
                ON CONFLICT (id) DO NOTHING
                """, DEMO_WORKSHOP_ID, DEMO_OWNER_ID, now, now);

        insertBranch(BRANCH_CENTRAL, "SED-CEN-01", "Sede Central - ShiftIQ", "Av. Demo 100", "911111111", now);
        insertBranch(BRANCH_NORTH, "SED-NOR-01", "Sede Norte - Taller Express", "Av. Norte 200", "922222222", now);
        insertBranch(BRANCH_SOUTH, "SED-SUR-01", "Sede Sur - Mantenimiento Pesado", "Av. Sur 300", "933333333", now);

        linkUserBranch(DEMO_USER_ID, BRANCH_CENTRAL);
        linkUserBranch(DEMO_USER_ID, BRANCH_NORTH);
        linkUserBranch(DEMO_USER_ID, BRANCH_SOUTH);

        ensureSampleProducts();
        log.info("Inventory demo tenant seeded (inventory.demo@shiftiq.local / Password123!)");
    }

    private void ensureSampleProducts() {
        Timestamp now = Timestamp.from(Instant.now());
        if (!exists("SELECT 1 FROM products WHERE id = ?", PRODUCT_BRAKE)) {
            insertProduct(PRODUCT_BRAKE, BRANCH_CENTRAL, "Frenos", "Pastillas de Freno Cerámicas Delanteras",
                    "REP-HILUX-04", "Juego delantero cerámico", new BigDecimal("140.00"), 5, 14, false, now);
            insertBatch(BATCH_BRAKE, PRODUCT_BRAKE, BRANCH_CENTRAL, 14, 14, new BigDecimal("85.00"), now);
        }
        if (!exists("SELECT 1 FROM products WHERE id = ?", PRODUCT_FILTER)) {
            insertProduct(PRODUCT_FILTER, BRANCH_CENTRAL, "Filtros", "Filtro de Aceite Sintético Premium",
                    "FLT-OIL-001", "Filtro de alto flujo", new BigDecimal("45.00"), 5, 3, true, now);
            insertBatch(BATCH_FILTER, PRODUCT_FILTER, BRANCH_CENTRAL, 3, 3, new BigDecimal("22.50"), now);
        }
        if (!exists("SELECT 1 FROM products WHERE id = ?", PRODUCT_OIL)) {
            insertProduct(PRODUCT_OIL, BRANCH_NORTH, "Aceites", "Aceite Sintético 5W-30 4L",
                    "OIL-5W30-4L", "Bidón 4 litros", new BigDecimal("89.90"), 4, 10, false, now);
        }
    }

    private void insertBranch(UUID id, String code, String name, String address, String phone, Timestamp now) {
        jdbcTemplate.update("""
                INSERT INTO branches (id, workshop_id, code, name, address, phone, created_at, updated_at, created_by, version)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, 0)
                ON CONFLICT (id) DO NOTHING
                """, id, DEMO_WORKSHOP_ID, code, name, address, phone, now, now, DEMO_USER_ID);
    }

    private void linkUserBranch(UUID userId, UUID branchId) {
        jdbcTemplate.update("""
                INSERT INTO user_branches (user_id, branch_id)
                VALUES (?, ?)
                ON CONFLICT DO NOTHING
                """, userId, branchId);
    }

    private void insertProduct(UUID id, UUID branchId, String category, String name, String sku,
                               String description, BigDecimal price, int minStock, int currentStock,
                               boolean lowStockAlert, Timestamp now) {
        jdbcTemplate.update("""
                INSERT INTO products (
                    id, branch_id, category, name, sku, description,
                    current_selling_price, current_stock, minimum_stock, low_stock_alert,
                    created_at, updated_at, created_by, version
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 0)
                ON CONFLICT (id) DO NOTHING
                """, id, branchId, category, name, sku, description,
                price, currentStock, minStock, lowStockAlert, now, now, DEMO_USER_ID);
    }

    private void insertBatch(UUID id, UUID productId, UUID branchId, int initial, int available,
                             BigDecimal cost, Timestamp now) {
        jdbcTemplate.update("""
                INSERT INTO product_batches (
                    id, product_id, branch_id, initial_quantity, available_quantity, acquisition_cost,
                    created_at, updated_at, created_by, version
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, 0)
                ON CONFLICT (id) DO NOTHING
                """, id, productId, branchId, initial, available, cost, now, now, DEMO_USER_ID);
    }

    private boolean exists(String sql, Object arg) {
        Boolean found = jdbcTemplate.query(sql, rs -> rs.next() ? Boolean.TRUE : Boolean.FALSE, arg);
        return Boolean.TRUE.equals(found);
    }
}
