package com.tuxlogic.shiftiq.platform.iam.infrastructure.persistence.jpa;

import com.tuxlogic.shiftiq.platform.iam.domain.model.valueobjects.Permission;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;

class WorkshopRolePersistenceTest {

    @Test
    @DisplayName("V21 Flyway migration creates expected tables and seeds valid permission codes")
    void migrationCreatesTablesAndSeedsValidPermissions() throws Exception {
        Path migrationPath = Path.of("src/main/resources/db/migration/V21__create_workshop_roles_and_permissions.sql");
        assertThat(Files.exists(migrationPath)).isTrue();

        String sql = Files.readString(migrationPath);
        assertThat(sql)
                .contains("CREATE TABLE IF NOT EXISTS workshop_roles")
                .contains("CREATE TABLE IF NOT EXISTS workshop_role_permissions")
                .contains("CREATE TABLE IF NOT EXISTS workshop_role_audit_logs")
                .contains("ADD COLUMN IF NOT EXISTS workshop_role_id")
                .contains("ROLE_OWNER")
                .contains("ROLE_BRANCH_MANAGER")
                .contains("ROLE_ASSISTANT")
                .contains("ROLE_EMPLOYEE")
                .contains("ROLE_WAREHOUSE")
                .contains("ROLE_CASHIER")
                .contains("ROLE_ACCOUNTANT")
                .doesNotContain("'PLATFORM_ADMIN'");

        // Verify every seeded permission string matches an enum constant in Permission
        Pattern permissionPattern = Pattern.compile("'([A-Z]+_[A-Z_]+)'");
        Matcher matcher = permissionPattern.matcher(sql);
        int matched = 0;
        while (matcher.find()) {
            String candidate = matcher.group(1);
            if (!candidate.startsWith("ROLE_")) {
                Permission.valueOf(candidate);
                matched++;
            }
        }
        assertThat(matched).isGreaterThan(20);
    }
}
