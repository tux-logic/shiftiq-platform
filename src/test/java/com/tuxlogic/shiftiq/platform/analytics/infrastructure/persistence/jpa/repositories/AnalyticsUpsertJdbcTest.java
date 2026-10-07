package com.tuxlogic.shiftiq.platform.analytics.infrastructure.persistence.jpa.repositories;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Validates the native upsert SQL of {@link JpaBranchAnalyticsSnapshotRepository} and the
 * V13 schema against a real PostgreSQL database. Runs only when {@code DATABASE_URL} is
 * exported (e.g. {@code set -a; source .env; set +a}); skipped otherwise.
 */
@EnabledIfEnvironmentVariable(named = "DATABASE_URL", matches = ".+")
class AnalyticsUpsertJdbcTest {

    private static final String APPLY_DELTA = """
            INSERT INTO branch_analytics_snapshots
                (id, branch_id, snapshot_date, total_revenue, completed_work_orders_count,
                 total_appointments_count, low_stock_alerts_count, dtc_alerts_count,
                 created_at, updated_at)
            VALUES (?, ?, ?, ?, ?, ?, 0, ?, ?, ?)
            ON CONFLICT (branch_id, snapshot_date) DO UPDATE SET
                total_revenue = GREATEST(branch_analytics_snapshots.total_revenue + EXCLUDED.total_revenue, 0),
                completed_work_orders_count = GREATEST(branch_analytics_snapshots.completed_work_orders_count + EXCLUDED.completed_work_orders_count, 0),
                total_appointments_count = GREATEST(branch_analytics_snapshots.total_appointments_count + EXCLUDED.total_appointments_count, 0),
                dtc_alerts_count = GREATEST(branch_analytics_snapshots.dtc_alerts_count + EXCLUDED.dtc_alerts_count, 0),
                updated_at = EXCLUDED.updated_at
            """;

    private static final String UPSERT_LOW_STOCK = """
            INSERT INTO branch_analytics_snapshots
                (id, branch_id, snapshot_date, total_revenue, completed_work_orders_count,
                 total_appointments_count, low_stock_alerts_count, dtc_alerts_count,
                 created_at, updated_at)
            VALUES (?, ?, ?, 0, 0, 0, ?, 0, ?, ?)
            ON CONFLICT (branch_id, snapshot_date) DO UPDATE SET
                low_stock_alerts_count = GREATEST(EXCLUDED.low_stock_alerts_count, 0),
                updated_at = EXCLUDED.updated_at
            """;

    private static final String JDBC_URL = "jdbc:postgresql://" + System.getenv("DATABASE_URL") + ":"
            + System.getenv("DATABASE_PORT") + "/" + System.getenv("DATABASE_NAME") + "?sslmode=prefer";

    private final List<UUID> touchedBranches = new ArrayList<>();

    @AfterEach
    void cleanUp() throws Exception {
        try (Connection conn = open(); PreparedStatement ps =
                conn.prepareStatement("DELETE FROM branch_analytics_snapshots WHERE branch_id = ?")) {
            for (UUID branch : touchedBranches) {
                ps.setObject(1, branch);
                ps.executeUpdate();
            }
        }
        touchedBranches.clear();
    }

    @Test
    @DisplayName("V13 drops the redundant index and creates the pending deltas table")
    void v13SchemaIsApplied() throws Exception {
        try (Connection conn = open()) {
            assertThat(exists(conn,
                    "SELECT 1 FROM pg_indexes WHERE indexname = 'idx_branch_analytics_branch_date'"))
                    .as("redundant index dropped by V13")
                    .isFalse();
            assertThat(exists(conn,
                    "SELECT 1 FROM information_schema.tables WHERE table_name = 'branch_analytics_pending_deltas'"))
                    .as("pending deltas table created by V13")
                    .isTrue();
        }
    }

    @Test
    @DisplayName("sequential deltas accumulate on the same branch and day")
    void sequentialDeltasAccumulate() throws Exception {
        UUID branch = newBranch();
        LocalDate day = LocalDate.now();

        try (Connection conn = open()) {
            applyDelta(conn, branch, day, "100.00", 1, 2, 3);
            applyDelta(conn, branch, day, "50.50", 1, 1, 1);

            var row = read(conn, branch, day);
            assertThat(row.revenue()).isEqualByComparingTo("150.50");
            assertThat(row.workOrders()).isEqualTo(2);
            assertThat(row.appointments()).isEqualTo(3);
            assertThat(row.dtcAlerts()).isEqualTo(4);
        }
    }

    @Test
    @DisplayName("negative corrective deltas are clamped at zero")
    void negativeDeltasClampAtZero() throws Exception {
        UUID branch = newBranch();
        LocalDate day = LocalDate.now();

        try (Connection conn = open()) {
            applyDelta(conn, branch, day, "10.00", 1, 0, 0);
            applyDelta(conn, branch, day, "-9999.00", -7, -1, -1);

            var row = read(conn, branch, day);
            assertThat(row.revenue().signum()).isZero();
            assertThat(row.workOrders()).isZero();
            assertThat(row.appointments()).isZero();
            assertThat(row.dtcAlerts()).isZero();
        }
    }

    @Test
    @DisplayName("low stock upsert stores an absolute value and creates the row when missing")
    void lowStockUpsertCreatesRowWhenMissing() throws Exception {
        UUID branch = newBranch();
        LocalDate day = LocalDate.now();

        try (Connection conn = open()) {
            upsertLowStock(conn, branch, day, 4);
            assertThat(read(conn, branch, day).lowStockAlerts()).isEqualTo(4);

            upsertLowStock(conn, branch, day, 1);
            assertThat(read(conn, branch, day).lowStockAlerts()).isEqualTo(1);
        }
    }

    @Test
    @DisplayName("concurrent upserts on the same branch and day never lose an update")
    void concurrentUpsertsDoNotLoseUpdates() throws Exception {
        UUID branch = newBranch();
        LocalDate day = LocalDate.now();
        int threads = 4;
        int perThread = 25;
        int expected = threads * perThread;
        CountDownLatch start = new CountDownLatch(1);
        AtomicInteger errors = new AtomicInteger();
        List<Thread> workers = new ArrayList<>();

        for (int t = 0; t < threads; t++) {
            Thread worker = new Thread(() -> {
                try (Connection conn = open()) {
                    start.await();
                    for (int i = 0; i < perThread; i++) {
                        applyDelta(conn, branch, day, "1.00", 1, 1, 0);
                    }
                } catch (Exception ex) {
                    errors.incrementAndGet();
                }
            });
            workers.add(worker);
            worker.start();
        }
        start.countDown();
        for (Thread worker : workers) {
            worker.join();
        }

        try (Connection conn = open()) {
            var row = read(conn, branch, day);
            assertThat(errors.get()).isZero();
            assertThat(row.revenue()).isEqualByComparingTo(BigDecimal.valueOf(expected));
            assertThat(row.workOrders()).isEqualTo(expected);
            assertThat(row.appointments()).isEqualTo(expected);
        }
    }

    private UUID newBranch() {
        UUID branch = UUID.randomUUID();
        touchedBranches.add(branch);
        return branch;
    }

    private static Connection open() throws Exception {
        return DriverManager.getConnection(JDBC_URL, System.getenv("DATABASE_USER"), System.getenv("DATABASE_PASSWORD"));
    }

    private static void applyDelta(Connection conn, UUID branch, LocalDate day,
                                   String revenue, int workOrders, int appointments, int dtc) throws Exception {
        try (PreparedStatement ps = conn.prepareStatement(APPLY_DELTA)) {
            ps.setObject(1, UUID.randomUUID());
            ps.setObject(2, branch);
            ps.setObject(3, day);
            ps.setBigDecimal(4, new BigDecimal(revenue));
            ps.setInt(5, workOrders);
            ps.setInt(6, appointments);
            ps.setInt(7, dtc);
            ps.setTimestamp(8, Timestamp.from(Instant.now()));
            ps.setTimestamp(9, Timestamp.from(Instant.now()));
            ps.executeUpdate();
        }
    }

    private static void upsertLowStock(Connection conn, UUID branch, LocalDate day, int count) throws Exception {
        try (PreparedStatement ps = conn.prepareStatement(UPSERT_LOW_STOCK)) {
            ps.setObject(1, UUID.randomUUID());
            ps.setObject(2, branch);
            ps.setObject(3, day);
            ps.setInt(4, count);
            ps.setTimestamp(5, Timestamp.from(Instant.now()));
            ps.setTimestamp(6, Timestamp.from(Instant.now()));
            ps.executeUpdate();
        }
    }

    private record SnapshotRow(BigDecimal revenue, int workOrders, int appointments,
                               int lowStockAlerts, int dtcAlerts) {}

    private static SnapshotRow read(Connection conn, UUID branch, LocalDate day) throws Exception {
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT total_revenue, completed_work_orders_count, total_appointments_count, "
                        + "low_stock_alerts_count, dtc_alerts_count "
                        + "FROM branch_analytics_snapshots WHERE branch_id = ? AND snapshot_date = ?")) {
            ps.setObject(1, branch);
            ps.setObject(2, day);
            try (ResultSet rs = ps.executeQuery()) {
                assertThat(rs.next()).as("snapshot row exists").isTrue();
                return new SnapshotRow(rs.getBigDecimal(1), rs.getInt(2), rs.getInt(3), rs.getInt(4), rs.getInt(5));
            }
        }
    }

    private static boolean exists(Connection conn, String sql) throws Exception {
        try (PreparedStatement ps = conn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            return rs.next();
        }
    }
}
