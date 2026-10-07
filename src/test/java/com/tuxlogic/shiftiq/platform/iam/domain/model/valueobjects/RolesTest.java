package com.tuxlogic.shiftiq.platform.iam.domain.model.valueobjects;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RolesTest {

    @Test
    @DisplayName("hierarchy ranks staff below managers and owners below admins")
    void hierarchyRankOrdersRolesByPrivilege() {
        assertThat(Roles.ROLE_EMPLOYEE.hierarchyRank()).isZero();
        assertThat(Roles.ROLE_USER.hierarchyRank()).isZero();
        assertThat(Roles.ROLE_ASSISTANT.hierarchyRank()).isEqualTo(1);
        assertThat(Roles.ROLE_BRANCH_MANAGER.hierarchyRank()).isEqualTo(2);
        assertThat(Roles.ROLE_OWNER.hierarchyRank()).isEqualTo(3);
        assertThat(Roles.ROLE_ADMIN.hierarchyRank()).isEqualTo(4);

        assertThat(Roles.ROLE_EMPLOYEE.hierarchyRank())
                .isLessThan(Roles.ROLE_ASSISTANT.hierarchyRank())
                .isLessThan(Roles.ROLE_BRANCH_MANAGER.hierarchyRank())
                .isLessThan(Roles.ROLE_OWNER.hierarchyRank())
                .isLessThan(Roles.ROLE_ADMIN.hierarchyRank());
    }

    @Test
    @DisplayName("a role can never be replaced by a less privileged one")
    void cannotBeReplacedByDemotes() {
        assertThat(Roles.ROLE_BRANCH_MANAGER.canBeReplacedBy(Roles.ROLE_EMPLOYEE)).isFalse();
        assertThat(Roles.ROLE_ASSISTANT.canBeReplacedBy(Roles.ROLE_EMPLOYEE)).isFalse();
        assertThat(Roles.ROLE_OWNER.canBeReplacedBy(Roles.ROLE_BRANCH_MANAGER)).isFalse();
        assertThat(Roles.ROLE_ADMIN.canBeReplacedBy(Roles.ROLE_OWNER)).isFalse();
        assertThat(Roles.ROLE_ADMIN.canBeReplacedBy(Roles.ROLE_ADMIN)).isTrue();
        assertThat(Roles.ROLE_ASSISTANT.canBeReplacedBy(Roles.ROLE_BRANCH_MANAGER)).isTrue();
        assertThat(Roles.ROLE_BRANCH_MANAGER.canBeReplacedBy(null)).isFalse();
    }

    @Test
    @DisplayName("fromName normalizes the ROLE_ prefix and rejects unknown values")
    void fromNameNormalizesPrefix() {
        assertThat(Roles.fromName("assistant")).isEqualTo(Roles.ROLE_ASSISTANT);
        assertThat(Roles.fromName("ASSISTANT")).isEqualTo(Roles.ROLE_ASSISTANT);
        assertThat(Roles.fromName("ROLE_BRANCH_MANAGER")).isEqualTo(Roles.ROLE_BRANCH_MANAGER);
        assertThat(Roles.fromName("branch_manager")).isEqualTo(Roles.ROLE_BRANCH_MANAGER);
        assertThat(Roles.fromName("ROLE_DOES_NOT_EXIST")).isNull();
        assertThat(Roles.fromName("")).isNull();
        assertThat(Roles.fromName("   ")).isNull();
        assertThat(Roles.fromName(null)).isNull();
    }
}
